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

    /**
     * Instantiates a new Counting input stream.
     *
     * @param in the in
     */
    public CountingInputStream(InputStream in) {
        this(in, new AtomicLong());
    }

    /**
     * Wraps another input stream, counting the number of bytes read.
     *
     * @param in      the input stream to be wrapped
     * @param counter the counter
     */
    public CountingInputStream(InputStream in, AtomicLong counter) {
        super(in);
        this.counter = requireNonNull(counter);
    }

    /**
     * Returns the number of bytes read.  @return the count
     */
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
