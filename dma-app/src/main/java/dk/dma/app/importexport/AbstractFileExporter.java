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
package dk.dma.app.importexport;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.google.inject.Injector;

import dk.dma.app.AbstractCommandLineTool;
import dk.dma.app.util.Filter;
import dk.dma.app.util.batch.QueuePumper;
import dk.dma.app.util.io.IoUtil;
import dk.dma.app.util.io.OutputStreamSink;
import dk.dma.app.util.io.PathSuppliers;
import dk.dma.app.util.io.RollingOutputStream;
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

    @Parameter(names = "-filter", description = "The filter class")
    protected String exporter;

    @ParametersDelegate
    Filter<T> filterInstance;

    @Parameter(names = "-formatter", description = "The formatter class")
    protected String formatter;

    @ParametersDelegate
    OutputStreamSink<T> formatterInstance;

    @Parameter(names = "-prefix", description = "The prefix of each file")
    protected String fileprefix = getClass().getSimpleName();

    @Parameter(names = "-noZip", description = "Does not compress the exported data")
    boolean noZip;

    volatile RollingOutputStream ros;

    @ManagedAttribute
    public long getNumberOfBytesWritten() {
        return ros == null ? 0 : ros.getTotalBytesWritten();
    }

    @ManagedAttribute
    public long getNumberOfMegaBytesWritten() {
        return getNumberOfBytesWritten() / 1024 / 1024;
    }

    @Override
    protected void run(Injector injector) throws Exception {
        IoUtil.validateFolderExist("Destination", destination);

        try (RollingOutputStream ros = this.ros = new RollingOutputStream(PathSuppliers.increasingAbsolutePaths(
                destination.toPath(), fileprefix, "txt"), !noZip)) {

            QueuePumper<T> qp = new QueuePumper<>(ros.createProcessor(formatterInstance, chunkSizeMB * 1024L * 1024L)
                    .filter(filterInstance), 10000);

            // We use a separate single thread to write messages, this is around 70 % faster when writing zip files.
            // Than just using a single thread
            qp.start();
            // traverseSourceData(qp);
            qp.shutdownAndAwaitTermination(5, TimeUnit.MINUTES);
        }
    }

    // protected abstract void traverseSourceData(Processor<T> producer) throws Exception;
}
