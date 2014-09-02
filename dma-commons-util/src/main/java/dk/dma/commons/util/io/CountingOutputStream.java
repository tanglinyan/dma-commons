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
