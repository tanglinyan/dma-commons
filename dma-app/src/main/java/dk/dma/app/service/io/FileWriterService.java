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
package dk.dma.app.service.io;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import dk.dma.app.io.OutputStreamSink;
import dk.dma.app.service.AbstractBatchedStage;
import dk.dma.app.util.function.LongFunction;

/**
 * 
 * @author Kasper Nielsen
 */
public class FileWriterService<T> extends AbstractBatchedStage<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FileWriterService.class);

    final String filename;

    final long maxSize;

    private final Path root;

    private final OutputStreamSink<T> sink;

    private final LongFunction<T> toTime;

    private Path currentPath;

    private final RollingOutputStream ros;

    private long lastTime = -1;

    private final SimpleDateFormat sdf;

    /**
     * @param queueSize
     * @param maxBatchSize
     */
    FileWriterService(Path root, String filename, OutputStreamSink<T> sink, LongFunction<T> toTime, long maxSize) {
        super(100000, 100);
        this.root = requireNonNull(root);
        this.filename = requireNonNull(filename);
        this.sink = requireNonNull(sink);
        this.toTime = toTime;
        this.maxSize = maxSize;
        sdf = toTime == null ? null : new SimpleDateFormat(filename);
        ros = new RollingOutputStream(toTime == null ? PathSuppliers.EXPLICIT_ROLL : PathSuppliers.EXPLICIT_ROLL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleMessages(List<T> messages) throws IOException {
        for (T t : messages) {
            if (toTime == null) {
                throw new UnsupportedOperationException();
            } else {
                long time = toTime.applyAsLong(t);
                if (time < lastTime) {
                    throw new IllegalStateException("Cannot go backwards, last time =" + lastTime + ", currenttime="
                            + time);
                }
                if (currentPath == null || time / 1000 == lastTime / 1000) {
                    Path p = root.resolve(sdf.format(new Date(time)));
                    if (!Objects.equal(p, currentPath)) {
                        ros.roll(p);
                        currentPath = p;
                        LOG.info("Opening file " + p + " for backup");
                    }
                }
                sink.process(ros.getPublicStream(), t);
                lastTime = time;
            }
        }
    }

    public static <T> FileWriterService<T> dateService(Path root, String filename, OutputStreamSink<T> sink) {
        return new FileWriterService<>(root, validateFilename(root, filename), sink, new LongFunction<T>() {

            @Override
            public long applyAsLong(T element) {
                return System.currentTimeMillis();
            }
        }, Long.MAX_VALUE);
    }

    public static <T> FileWriterService<T> dateService(Path root, String filename, OutputStreamSink<T> sink,
            LongFunction<T> toTime) {
        return new FileWriterService<>(root, validateFilename(root, filename), sink, requireNonNull(toTime),
                Long.MAX_VALUE);
    }

    public static <T> FileWriterService<T> chunkedService(Path root, String filename, OutputStreamSink<T> sink,
            long maxSize) {
        return new FileWriterService<>(root, filename, sink, null, maxSize);
    }

    private static String validateFilename(Path root, String filename) {
        requireNonNull(root, "root is null");
        SimpleDateFormat sdf = new SimpleDateFormat(filename);
        String format = sdf.format(new Date());
        root.resolve(format);// validates path
        return filename;
    }

    public static void main(String[] args) {
        System.out
                .println(validateFilename(Paths.get("/Users/kasperni/test"), "'a\nismessages'-YYYYd/d-H-m.'txt.zip'"));
        System.out.println("bye");
    }
}
