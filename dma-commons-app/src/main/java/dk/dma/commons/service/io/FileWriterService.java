/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.commons.service.io;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.AbstractScheduledService;

import dk.dma.commons.service.AbstractBatchedStage;
import dk.dma.commons.util.io.OutputStreamSink;
import dk.dma.enav.util.function.LongFunction;

/**
 * 
 * @author Kasper Nielsen
 */
public class FileWriterService<T> extends AbstractBatchedStage<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FileWriterService.class);

    private Path currentPath;

    final String filename;

    private long lastTime = -1;

    final ReentrantLock lock = new ReentrantLock();

    final long maxSize;

    private final Path root;

    final RollingOutputStream ros = new RollingOutputStream();

    private final SimpleDateFormat sdf;

    private final OutputStreamSink<T> sink;

    private final LongFunction<T> toTime;

    /**
     * @param queueSize
     * @param maxBatchSize
     */
    FileWriterService(Path root, String filename, OutputStreamSink<T> sink, LongFunction<T> toTime, long maxSize) {
        super(10000, 100);
        this.root = requireNonNull(root);
        this.filename = requireNonNull(filename);
        this.sink = requireNonNull(sink);
        this.toTime = toTime;
        this.maxSize = maxSize;
        sdf = toTime == null ? null : new SimpleDateFormat(filename);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleMessages(List<T> messages) throws IOException {
        lock.lock();
        try {
            for (T t : messages) {
                System.out.println("SAVINDSINSG");
                if (toTime == null) {
                    throw new UnsupportedOperationException();
                } else {
                    long time = toTime.applyAsLong(t);
                    if (time < lastTime) {
                        System.err.println("Cannot go backwards, last time =" + lastTime + ", currenttime=" + time
                                + " writing anyways");
                        time = lastTime;
                    }
                    // We check the time once every second
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
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onShutdown() {
        lock.lock();
        try {
            ros.close();
        } catch (IOException e) {
            LOG.error("Could not close stream " + currentPath + " for backup", e);
        } finally {
            lock.unlock();
        }

    }

    public static <T> FileWriterService<T> chunkedService(Path root, String filename, OutputStreamSink<T> sink,
            long maxSize) {
        return new FileWriterService<>(root, filename, sink, null, maxSize);
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

    //
    // public static void main(String[] args) {
    // System.out
    // .println(validateFilename(Paths.get("/Users/kasperni/test"), "'a\nismessages'-YYYYd/d-H-m.'txt.zip'"));
    // System.out.println("bye");
    // }

    static String validateFilename(Path root, String filename) {
        requireNonNull(root, "root is null");
        SimpleDateFormat sdf = new SimpleDateFormat(filename);
        String format = sdf.format(new Date());
        root.resolve(format);// validates path
        return filename;
    }

    class FlushThread extends AbstractScheduledService {

        protected void runOneIteration() throws Exception {
            lock.lock();
            try {
                ros.flush();
                // Hmm vi ved ikke hvornaar vi kan lukke den
                // long time = toTime.applyAsLong(t);
                // if (time > lastTime) {
                // if (currentPath != null) {
                // Path p = root.resolve(sdf.format(new Date(time)));
                // if (!Objects.equal(p, currentPath)) {
                // ros.roll(p);
                // currentPath = p;
                // }
                // }
                // }
                // We check the time once every second

            } finally {
                lock.unlock();
            }
        }

        protected Scheduler scheduler() {
            return Scheduler.newFixedRateSchedule(1, 1, TimeUnit.SECONDS);
        }
    }

}
