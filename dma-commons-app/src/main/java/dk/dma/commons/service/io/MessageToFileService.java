/* Copyright (c) 2011 Danish Maritime Authority.
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
import com.google.common.util.concurrent.Service;

import dk.dma.commons.service.AbstractBatchedStage;
import dk.dma.commons.util.io.OutputStreamSink;

/**
 * The type Message to file service.
 *
 * @param <T> the type parameter
 * @author Kasper Nielsen
 */
public class MessageToFileService<T> extends AbstractBatchedStage<T> {

    /**
     * The Log.
     */
    static final Logger LOG = LoggerFactory.getLogger(MessageToFileService.class);

    /**
     * The Current path.
     */
    Path currentPath;

    /**
     * The Filename.
     */
    final String filename;

    /**
     * The Last time.
     */
    long lastTime = -1;

    /**
     * The Lock.
     */
    final ReentrantLock lock = new ReentrantLock();

    /**
     * The Max size.
     */
    final long maxSize;

    /**
     * The Root.
     */
    final Path root;

    /**
     * The Ros.
     */
    final RollingOutputStream ros = new RollingOutputStream();

    /**
     * The Sdf.
     */
    final SimpleDateFormat sdf;

    /**
     * The Sink.
     */
    final OutputStreamSink<T> sink;

    /**
     * The Count.
     */
    long count;

    /**
     * Instantiates a new Message to file service.
     *
     * @param root     the root
     * @param filename the filename
     * @param sink     the sink
     * @param maxSize  the max size
     */
    MessageToFileService(Path root, String filename, OutputStreamSink<T> sink, long maxSize) {
        super(10000, 100);
        this.root = requireNonNull(root);
        this.filename = requireNonNull(filename);
        this.sink = requireNonNull(sink);
        this.maxSize = maxSize;
        sdf = new SimpleDateFormat(filename);
    }

    /**
     * Time long.
     *
     * @return the long
     */
    long time() {
        long time = System.currentTimeMillis();
        if (time < lastTime) {
            System.err.println("Cannot go backwards, last time =" + lastTime + ", currenttime=" + time
                    + " writing anyways");
            time = lastTime;
        }
        return time;
    }

    /** Writes every message to a file */
    @Override
    protected void handleMessages(List<T> messages) throws IOException {
        lock.lock();
        try {
            for (T t : messages) {
                long time = time();
                // If current file is null (initial), or time differs from last time by more than 1 second
                if (currentPath == null || time / 1000 != lastTime / 1000) {
                    Path p = root.resolve(sdf.format(new Date(time))); // create new path
                    if (!Objects.equal(p, currentPath)) { // is the new path identical to the old path
                        LOG.info("Opening file " + p.toAbsolutePath() + " for backup");
                        ros.roll(p); // create a new file
                        count = 0;
                        currentPath = p;
                    }
                }
                sink.process(ros.getPublicStream(), t, count++);
                lastTime = time;
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

    /**
     * Creates a new MessageToFileService.
     *
     * @param <T>             the type parameter
     * @param root            the root directory to write to
     * @param filenamePattern the filename pattern
     * @param sink            the sink
     * @return message to file service
     */
    public static <T> MessageToFileService<T> dateTimeService(Path root, String filenamePattern,
            OutputStreamSink<T> sink) {
        return new MessageToFileService<>(root, validateFilename(root, filenamePattern), sink, Long.MAX_VALUE);
    }

    /**
     * Validate filename string.
     *
     * @param root     the root
     * @param filename the filename
     * @return the string
     */
    static String validateFilename(Path root, String filename) {
        requireNonNull(root, "root is null");
        SimpleDateFormat sdf = new SimpleDateFormat(filename);
        String format = sdf.format(new Date());
        root.resolve(format);// validates path
        return filename;
    }

    /**
     * Starts the flushing thread. We need this situations where we do not have a constant inflow of messages. Since
     * files can only be closed when a new file arrives (see code in {@link #handleMessages(List)}. We need to
     * periodically check, if no messages arrive for a period, if a file should be closed.
     *
     * @return service service
     */
    public Service startFlushThread() {
        return new FlushThread();
    }

    /**
     * The type Flush thread.
     */
    class FlushThread extends AbstractScheduledService {

        protected void runOneIteration() throws Exception {
            lock.lock();
            try {
                try {
                    ros.flush();
                    if (currentPath != null) {
                        Path p = root.resolve(sdf.format(new Date(time()))); // create new path
                        if (!Objects.equal(p, currentPath)) { // is the new path identical to the old path
                            currentPath = null;
                            ros.close();
                            count = 0;
                        }
                    }
                } catch (IOException e) {
                    LOG.error("FlushThread failed", e);
                }
            } finally {
                lock.unlock();
            }
        }

        protected Scheduler scheduler() {
            return Scheduler.newFixedRateSchedule(1, 1, TimeUnit.SECONDS);
        }
    }
}
