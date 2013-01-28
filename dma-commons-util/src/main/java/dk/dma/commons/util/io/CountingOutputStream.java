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
package dk.dma.commons.util.io;

import static java.util.Objects.requireNonNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wraps another output stream, counting the number of bytes written.
 * 
 * @author Kasper Nielsen
 */
public final class CountingOutputStream extends FilterOutputStream {

    /** The counter. */
    private final AtomicLong counter;

    /**
     * Wraps another output stream, counting the number of bytes written.
     * 
     * @param out
     *            the output stream to be wrapped
     */
    public CountingOutputStream(OutputStream out) {
        this(out, new AtomicLong());
    }

    /**
     * Wraps another output stream, counting the number of bytes written.
     * 
     * @param out
     *            the output stream to be wrapped
     * @param counter
     *            the atomic long that will be incremented
     */
    public CountingOutputStream(OutputStream out, AtomicLong counter) {
        super(out);
        this.counter = requireNonNull(counter);
    }

    /** Returns the number of bytes written. */
    public long getCount() {
        return counter.get();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        counter.addAndGet(len);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        counter.incrementAndGet();
    }
}
