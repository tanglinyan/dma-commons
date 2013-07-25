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
import com.google.common.util.concurrent.Service;

import dk.dma.commons.service.AbstractBatchedStage;
import dk.dma.commons.util.io.OutputStreamSink;

/**
 * 
 * @author Kasper Nielsen
 */
public class MessageToFileService<T> extends AbstractBatchedStage<T> {

    static final Logger LOG = LoggerFactory.getLogger(MessageToFileService.class);

    Path currentPath;

    final String filename;

    long lastTime = -1;

    final ReentrantLock lock = new ReentrantLock();

    final long maxSize;

    final Path root;

    final RollingOutputStream ros = new RollingOutputStream();

    final SimpleDateFormat sdf;

    final OutputStreamSink<T> sink;

    /**
     * @param queueSize
     * @param maxBatchSize
     */
    MessageToFileService(Path root, String filename, OutputStreamSink<T> sink, long maxSize) {
        super(10000, 100);
        this.root = requireNonNull(root);
        this.filename = requireNonNull(filename);
        this.sink = requireNonNull(sink);
        this.maxSize = maxSize;
        sdf = new SimpleDateFormat(filename);
    }

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
                        currentPath = p;
                    }
                }
                sink.process(ros.getPublicStream(), t);
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
     * @param root
     *            the root directory to write to
     * @param filenamePattern
     *            the filename pattern
     * @param sink
     *            the sink
     * @return
     */
    public static <T> MessageToFileService<T> dateTimeService(Path root, String filenamePattern,
            OutputStreamSink<T> sink) {
        return new MessageToFileService<>(root, validateFilename(root, filenamePattern), sink, Long.MAX_VALUE);
    }

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
     * @return
     */
    public Service startFlushThread() {
        return new FlushThread();
    }

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
