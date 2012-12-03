/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.app.commandline;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.google.inject.Injector;

import dk.dma.app.AbstractCommandLineTool;
import dk.dma.app.util.IoUtil;
import dk.dma.app.util.batch.BatchProcessor;
import dk.dma.app.util.batch.ExportFunction;
import dk.dma.app.util.batch.Processor;
import dk.dma.app.util.batch.QueuePumper;
import dk.dma.management.ManagedAttribute;

/**
 * 
 * @author Kasper Nielsen
 */
public abstract class AbstractFileExporter<T> extends AbstractCommandLineTool {

    @Parameter(names = "-chunksize", description = "The maximum size of each outputfile in mb")
    int chunkSizeMB = 100;

    @Parameter(names = "-destination", description = "Destination folder for Output")
    File destination = new File(".");

    @Parameter(names = "-exporter", description = "The exporter class")
    protected String exporter;

    @ParametersDelegate
    ExportFunction<T> exporterInstance;

    @Parameter(names = "-prefix", description = "The prefix of each file")
    protected String fileprefix = getClass().getSimpleName();

    @Parameter(names = "-noZip", description = "Does not compress the exported data")
    boolean noZip;

    volatile RollingOutputStream ros;

    @ManagedAttribute
    public long getNumberOfBytesWritten() {
        return ros == null ? 0 : ros.totalWritten.get();
    }

    @ManagedAttribute
    public long getNumberOfMegaBytesWritten() {
        return ros == null ? 0 : ros.totalWritten.get() / 1024 / 1024;
    }

    @Override
    protected void run(Injector injector) throws Exception {
        IoUtil.validateFolderExist("Destination", destination);

        try (RollingOutputStream ros = this.ros = new RollingOutputStream(destination.toPath(), fileprefix,
                chunkSizeMB, !noZip)) {
            QueuePumper<T> qp = new QueuePumper<>(new Processor<T>() {
                @Override
                public void process(T message) throws Exception {
                    exporterInstance.export(message, IoUtil.notCloseable(ros));
                    ros.checkRoll();
                }
            }, 10000);

            // We use a separate single thread to write messages, this is around 70 % faster when writing zip files.
            // Than just using a single thread
            final ExecutorService es = Executors.newSingleThreadExecutor();
            es.submit(qp);
            traverseSourceData(qp);
            es.shutdown();
            es.awaitTermination(1, TimeUnit.HOURS);
        }
    }

    protected abstract void traverseSourceData(BatchProcessor<T> producer) throws Exception;
}
