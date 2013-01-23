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
package dk.dma.app.service.io;

import static java.util.Objects.requireNonNull;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.base.Supplier;

import dk.dma.app.io.IoUtil;
import dk.dma.app.io.OutputStreamSink;
import dk.dma.app.util.function.EBlock;

/**
 * 
 * @author Kasper Nielsen
 */
class RollingOutputStream extends OutputStream {

    /** The current outputstream we are writing to. */
    private OutputStream current;

    /** Supplier for creating the next path that should be written. */
    private final Supplier<Path> filePathSupplier;

    private volatile Path finalPath;

    private volatile Path nextPath;

    /** The output stream that should be presented to users (cannot be closed). */
    private final OutputStream publicStream = IoUtil.notCloseable(this);

    /** The total number of bytes that has been written. */
    final AtomicLong totalWritten = new AtomicLong();

    /** The number of bytes that has been written to the current file. */
    final AtomicLong written = new AtomicLong();

    public RollingOutputStream() {
        this(PathSuppliers.EXPLICIT_ROLL);
    }

    public RollingOutputStream(Supplier<Path> filePathSupplier) {
        this.filePathSupplier = requireNonNull(filePathSupplier);
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        if (current != null) {
            current.close();
            if (!finalPath.equals(nextPath)) {
                Files.move(nextPath, finalPath);
            }
            current = null;
            nextPath = null;
        }
    }

    public <T> EBlock<T> createProcessor(final OutputStreamSink<T> sink, final long chunkSize) {
        requireNonNull(sink);
        if (chunkSize < 1) {
            throw new IllegalArgumentException("Chunksize must be at least 1, was " + chunkSize);
        }
        return new EBlock<T>() {
            @Override
            public void accept(T message) throws Exception {
                sink.process(IoUtil.notCloseable(RollingOutputStream.this), message);
                rollIfGreaterThan(chunkSize);
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public void flush() throws IOException {
        if (current != null) {
            current.flush();
        }
    }

    public long getCurrentFileBytesWritten() {
        return written.get();
    }

    public OutputStream getPublicStream() {
        return publicStream;
    }

    public long getTotalBytesWritten() {
        return totalWritten.get();
    }

    /**
     * Rolls the current output file
     * 
     * @throws IOException
     */
    public void roll() throws IOException {
        close();
    }

    /**
     * Rolls the current output file
     * 
     * @param nextPath
     *            the name of the output file (excluding .zip)
     * @throws IOException
     */
    public void roll(Path nextPath) throws IOException {
        close();
        this.nextPath = requireNonNull(nextPath);
    }

    void rollIfGreaterThan(long maxFileSize) throws IOException {
        if (maxFileSize < 1) {
            throw new IllegalArgumentException("maxFileSize must be at least 1, was " + maxFileSize);
        } else if (getCurrentFileBytesWritten() >= maxFileSize) {
            roll();
        }
    }

    private OutputStream write() throws IOException {
        if (current == null) {
            written.set(0);
            Path p = nextPath == null ? filePathSupplier.get() : nextPath;
            boolean zip = p.getFileName().toString().endsWith(".zip");
            finalPath = p;
            nextPath = p.resolveSibling(p.getFileName().toString() + ".tmp");
            // big buffer size is important as we do not want to write to disc to often
            Files.createDirectories(nextPath.getParent());
            current = new CountingOutputStream(new BufferedOutputStream(Files.newOutputStream(nextPath), 1024 * 1024));
            if (zip) {
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

    /** {@inheritDoc} */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        write().write(b, off, len);
    }

    /** {@inheritDoc} */
    @Override
    public void write(int b) throws IOException {
        write().write(b);
    }

    /**
     * Wraps another output stream, counting the number of bytes written. We need a seperate class because we might
     * append a zip stream.
     */
    final class CountingOutputStream extends FilterOutputStream {

        CountingOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            written.addAndGet(len);
            totalWritten.addAndGet(len);
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            written.incrementAndGet();
            totalWritten.incrementAndGet();
        }
    }
}
