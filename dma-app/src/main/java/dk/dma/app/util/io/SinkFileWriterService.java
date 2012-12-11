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
package dk.dma.app.util.io;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.app.util.concurrent.ShutdownBlockingQueue;
import dk.dma.management.ManagedAttribute;

/**
 * A service that takes elements from a queue and write them to a file. Rolling every either every minute, hour or day.
 * 
 * @author Kasper Nielsen
 */
public class SinkFileWriterService<T> extends IOInterruptableExecuteThreadService {
    private final static Logger logger = LoggerFactory.getLogger(SinkFileWriterService.class);

    private Path current;

    private final ShutdownBlockingQueue<T> queue;

    private final RollingOutputStream ros;

    private final OutputStreamSink<T> sink;

    private final Path template;

    private final TimeUnit unit;

    public SinkFileWriterService(Path template, TimeUnit wrapUnit, ShutdownBlockingQueue<T> queue,
            OutputStreamSink<T> sink, boolean zip) {
        if (wrapUnit != TimeUnit.MINUTES && wrapUnit != TimeUnit.HOURS && wrapUnit != TimeUnit.DAYS) {
            throw new IllegalArgumentException();
        }
        this.unit = requireNonNull(wrapUnit);
        this.template = requireNonNull(template);
        this.sink = requireNonNull(sink);
        ros = new RollingOutputStream(zip);
        this.queue = queue;
    }

    @ManagedAttribute
    public long getNumberOfBytesWritten() {
        return ros.getTotalBytesWritten();
    }

    @ManagedAttribute
    public long getNumberOfMegaBytesWritten() {
        return getNumberOfBytesWritten() / 1024 / 1024;
    }

    /** {@inheritDoc} */
    @Override
    protected void run0() throws Exception {
        while (state() == State.RUNNING || !queue.isTerminated()) {
            T t = super.pollInterruptable(queue, 1, unit);
            try {
                if (t == null) {
                    ros.close();
                } else {
                    Path p = IoUtil.addTimestamp(template, unit);
                    if (!p.equals(current)) {
                        ros.roll(current = p); // new file
                    }
                    sink.process(ros.getPublicStream(), t);

                }
            } catch (Exception e) {
                logger.error("something went wrong", e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void shutDown() throws Exception {
        ros.close();
    }

    public static <T> SinkFileWriterService<T> forTime(String name, TimeUnit wrapUnit, ShutdownBlockingQueue<T> queue,
            OutputStreamSink<T> sink) {
        return null;
        // return new SinkFileWriterService<>(directory, wrapUnit, queue, sink, zip);
    }

    public static <T> SinkFileWriterService<T> forSize(String name, long maxSize, ShutdownBlockingQueue<T> queue,
            OutputStreamSink<T> sink) {
        return null;
        // return new SinkFileWriterService<>(directory, wrapUnit, queue, sink, zip);
    }

    public static <T> SinkFileWriterService<T> forTime(Path directory, TimeUnit wrapUnit,
            ShutdownBlockingQueue<T> queue, OutputStreamSink<T> sink, boolean zip) {
        return new SinkFileWriterService<>(directory, wrapUnit, queue, sink, zip);
    }

    public static <T> SinkFileWriterService<T> chunkedWriterService() {
        return null;
    }
}
