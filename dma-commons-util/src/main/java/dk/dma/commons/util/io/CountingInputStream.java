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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wraps another input stream, counting the number of bytes written.
 * 
 * @author Kasper Nielsen
 */
public final class CountingInputStream extends FilterInputStream {

    /** The counter. */
    private final AtomicLong counter;

    public CountingInputStream(InputStream in) {
        this(in, new AtomicLong());
    }

    /**
     * Wraps another input stream, counting the number of bytes read.
     * 
     * @param in
     *            the input stream to be wrapped
     */
    public CountingInputStream(InputStream in, AtomicLong counter) {
        super(in);
        this.counter = requireNonNull(counter);
    }

    /** Returns the number of bytes read. */
    public long getCount() {
        return counter.get();
    }

    @Override
    public int read() throws IOException {
        int result = in.read();
        if (result != -1) {
            counter.incrementAndGet();
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (result != -1) {
            counter.addAndGet(result);
        }
        return result;
    }
}
