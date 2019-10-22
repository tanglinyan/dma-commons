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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.commons.util.io.CountingOutputStream;
import dk.dma.commons.util.io.IoUtil;
import dk.dma.commons.util.io.PathUtil;

/**
 * The type Rolling output stream.
 *
 * @author Kasper Nielsen
 */
class RollingOutputStream extends OutputStream {
    /**
     * The Log.
     */
    static final Logger LOG = LoggerFactory.getLogger(RollingOutputStream.class);

    /** The current output stream we are writing to. */
    private OutputStream current;

    /** The final name of the current path. */
    private volatile Path finalPath;

    private volatile Path nextPath;

    /** The output stream that should be presented to users (cannot be closed). */
    private final OutputStream publicStream = IoUtil.notCloseable(this);

    /**
     * The total number of bytes that has been written.
     */
    final AtomicLong totalWritten = new AtomicLong();

    /**
     * The number of bytes that has been written to the current file.
     */
    final AtomicLong written = new AtomicLong();

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        if (current != null) {
            current.close();
            if (!finalPath.equals(nextPath)) {
                finalPath = PathUtil.findUnique(finalPath);
                Files.move(nextPath, finalPath);
                LOG.info("Renaming " + nextPath + " to " + finalPath);
            }
            current = null;
            nextPath = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void flush() throws IOException {
        if (current != null) {
            current.flush();
        }
    }

    /**
     * Returns the number bytes written to the current file that is open.
     *
     * @return the number bytes written to the current file that is open
     */
    public long getCurrentFileBytesWritten() {
        return written.get();
    }

    /**
     * Returns an output stream that cannot be closed.
     *
     * @return an output stream that cannot be closed
     */
    public OutputStream getPublicStream() {
        return publicStream;
    }

    /**
     * Returns the total number of bytes written.
     *
     * @return the total number of bytes written
     */
    public long getTotalBytesWritten() {
        return totalWritten.get();
    }

    private OutputStream lazyOutput() throws IOException {
        if (current == null) {
            written.set(0);
            Path p = nextPath;
            boolean isZip = p.getFileName().toString().endsWith(".zip");
            finalPath = p;
            nextPath = p.resolveSibling(p.getFileName().toString() + ".tmp");
            nextPath = PathUtil.findUnique(nextPath);

            Files.createDirectories(nextPath.getParent());

            LOG.info("Opening file " + nextPath + " for backup");
            // System.out.println("Using " + nextPath.toAbsolutePath());
            // big buffer size is important as we do not want to write to disc to often
            current = new BufferedOutputStream(new CountingOutputStream(new CountingOutputStream(Files.newOutputStream(
                    nextPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND), written), totalWritten),
                    1024 * 1024);
            if (isZip) {
                ZipOutputStream zos = new ZipOutputStream(current);
                zos.putNextEntry(new ZipEntry(p.getFileName().toString().replace(".zip", "")));
                // big buffer size is important as we do not want to zip to often
                current = new BufferedOutputStream(zos, 1024 * 1024);
            }
            // A previous bug
            if (nextPath.toAbsolutePath().toString().length() > 200) {
                throw new Error(nextPath.toAbsolutePath().toString());
            }
        }
        return current;
    }

    /**
     * Closes (if open) the current output file. And sets the name of the file that should be openen next
     *
     * @param nextPath the name of the output file (excluding .zip)
     * @throws IOException the io exception
     */
    public void roll(Path nextPath) throws IOException {
        close();

        this.nextPath = requireNonNull(nextPath);
    }

    /** {@inheritDoc} */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        lazyOutput().write(b, off, len);
        lazyOutput().flush();
    }

    /** {@inheritDoc} */
    @Override
    public void write(int b) throws IOException {
        lazyOutput().write(b);
        lazyOutput().flush();
    }
}

// void rollIfGreaterThan(long maxFileSize) throws IOException {
// if (maxFileSize < 1) {
// throw new IllegalArgumentException("maxFileSize must be at least 1, was " + maxFileSize);
// } else if (getCurrentFileBytesWritten() >= maxFileSize) {
// close();
// }
// }

// public <T> EBlock<T> createProcessor(final OutputStreamSink<T> sink, final long chunkSize) {
// requireNonNull(sink);
// if (chunkSize < 1) {
// throw new IllegalArgumentException("Chunksize must be at least 1, was " + chunkSize);
// }
// return new EBlock<T>() {
// @Override
// public void accept(T message) throws Exception {
// sink.process(IoUtil.notCloseable(RollingOutputStream.this), message);
// rollIfGreaterThan(chunkSize);
// }
// };
// }
