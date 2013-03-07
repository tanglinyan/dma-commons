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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import dk.dma.enav.util.function.Consumer;
import dk.dma.enav.util.function.Predicate;

/**
 * <p>
 * Note this stream should also
 * 
 * @author Kasper Nielsen
 */
public abstract class OutputStreamSink<T> {

    /** A sink that will ignore any input. */
    public static final OutputStreamSink<?> IGNORE = new OutputStreamSink<Object>() {

        /** {@inheritDoc} */
        @Override
        public void process(OutputStream os, Object msg) {}
    };

    public static final OutputStreamSink<?> TO_STRING_US_ASCII_SINK = toStringSink(StandardCharsets.US_ASCII);

    public static final OutputStreamSink<?> TO_STRING_UTF8_SINK = toStringSink(StandardCharsets.UTF_8);

    /**
     * Returns a new sink that will only write messages that are accepted by the specified filter
     * 
     * @param filter
     *            the filter to test each message against
     * @return a new filtered sink
     */
    public final OutputStreamSink<T> filter(final Predicate<T> filter) {
        requireNonNull(filter);
        return new OutputStreamSink<T>() {
            @Override
            public void process(OutputStream stream, T message) throws IOException {
                if (filter.test(message)) {
                    OutputStreamSink.this.process(stream, message);
                }
            }

            @Override
            public void header(OutputStream stream) throws IOException {
                OutputStreamSink.this.header(stream);
            }

            @Override
            public void footer(OutputStream stream) throws IOException {
                OutputStreamSink.this.footer(stream);
            }

        };
    }

    // Returns a new sink that closes the stream
    public final OutputStreamSink<T> closeWhenFooterWritten() {
        return new OutputStreamSink<T>() {
            @Override
            public void process(OutputStream stream, T message) throws IOException {
                OutputStreamSink.this.process(stream, message);
            }

            @Override
            public void header(OutputStream stream) throws IOException {
                OutputStreamSink.this.header(stream);
            }

            @Override
            public void footer(OutputStream stream) throws IOException {
                try {
                    OutputStreamSink.this.footer(stream);
                } finally {
                    stream.close();
                }
            }
        };
    }

    public final Consumer<T> asConsumer(final OutputStream os) {
        requireNonNull(os);
        return new Consumer<T>() {
            public void accept(T t) {
                throw new UnsupportedOperationException();
                // process(os, t);
            }
        };
    }

    public final OutputStreamSink<T> writeHeaderAscii(String header) {
        return writeHeader(header, StandardCharsets.US_ASCII);
    }

    public final OutputStreamSink<T> writeHeader(final String header, final Charset charset) {
        requireNonNull(header);
        requireNonNull(charset);
        return new OutputStreamSink<T>() {
            @Override
            public void process(OutputStream stream, T message) throws IOException {
                OutputStreamSink.this.process(stream, message);
            }

            @Override
            public void header(OutputStream stream) throws IOException {
                stream.write(header.getBytes(charset));
                OutputStreamSink.this.header(stream);
            }

            @Override
            public void footer(OutputStream stream) throws IOException {
                OutputStreamSink.this.footer(stream);
            }
        };
    }

    public final OutputStreamSink<T> writeFooterAscii(String footer) {
        return writeFooter(footer, StandardCharsets.US_ASCII);
    }

    public final OutputStreamSink<T> writeFooter(final String footer, final Charset charset) {
        requireNonNull(footer);
        requireNonNull(charset);
        return new OutputStreamSink<T>() {
            @Override
            public void process(OutputStream stream, T message) throws IOException {
                OutputStreamSink.this.process(stream, message);
            }

            @Override
            public void header(OutputStream stream) throws IOException {
                OutputStreamSink.this.header(stream);
            }

            @Override
            public void footer(OutputStream stream) throws IOException {
                System.out.println("WRITE FOOT");
                OutputStreamSink.this.footer(stream);
                stream.write(footer.getBytes(charset));
            }
        };
    }

    @SuppressWarnings("unused")
    public void header(OutputStream stream) throws IOException {}

    @SuppressWarnings("unused")
    public void footer(OutputStream stream) throws IOException {}

    public abstract void process(OutputStream stream, T message) throws IOException;

    /**
     * 
     * 
     * @param charset
     *            the charset that should be used for encoding
     * @return
     */
    public static <T> OutputStreamSink<T> toStringSink(final Charset charset) {
        requireNonNull(charset);
        return new OutputStreamSink<T>() {

            @Override
            public void process(OutputStream stream, T message) throws IOException {
                String s = message.toString();
                stream.write(s.getBytes(charset));
                stream.write('\n');
            }
        };
    }
}
