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

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import com.google.inject.Injector;

import dk.dma.app.AbstractCommandLineTool;
import dk.dma.management.ManagedAttribute;

/**
 * 
 * @author Kasper Nielsen
 */
public abstract class AbstractTextFileImporter<T> extends AbstractCommandLineTool {

    private AtomicLong totalRead = new AtomicLong();

    @ManagedAttribute
    public long getNumberOfBytesRead() {
        return totalRead.get();
    }

    @ManagedAttribute
    public long getNumberOfMegaBytesRead() {
        return getNumberOfBytesRead() / 1024 / 1024;
    }

    @Override
    protected void run(Injector injector) throws Exception {
        for (Path p : selectFiles()) {
            System.out.println(p);
        }

        // IoUtil.validateFolderExist("Destination", destination);
        //
        // try (RollingOutputStream ros = this.ros = new RollingOutputStream(destination.toPath(), fileprefix,
        // chunkSizeMB, !zipped)) {
        // QueuePumper<T> qp = new QueuePumper<>(new Processor<T>() {
        // @Override
        // public void process(T message) throws Exception {
        // if (filterInstance.accept(message)) {
        // formatterInstance.process(message, IoUtil.notCloseable(ros));
        // ros.checkFileRoll();
        // }
        // }
        // }, 10000);
        //
        // // We use a separate single thread to write messages, this is around 70 % faster when writing zip files.
        // // Than just using a single thread
        // final ExecutorService es = Executors.newSingleThreadExecutor();
        // es.submit(qp);
        // traverseSourceData(qp);
        // es.shutdown();
        // es.awaitTermination(1, TimeUnit.HOURS);
        // }
    }

    protected abstract Iterable<Path> selectFiles();

}
