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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Note this stream should also
 *
 * @param <T> the type parameter
 * @author Kasper Nielsen
 */
public abstract class OutputStreamSink<T> {

    /**
     * A sink that will ignore any input.
     */
    public static final OutputStreamSink<Object> IGNORE = new OutputStreamSink<Object>() {

        /** {@inheritDoc} */
        @Override
        public void process(OutputStream os, Object msg, long count) {}
    };

    /**
     * The constant TO_STRING_US_ASCII_SINK.
     */
    public static final OutputStreamSink<Object> TO_STRING_US_ASCII_SINK = toStringSink(StandardCharsets.US_ASCII);

    /**
     * The constant TO_STRING_UTF8_SINK.
     */
    public static final OutputStreamSink<Object> TO_STRING_UTF8_SINK = toStringSink(StandardCharsets.UTF_8);

    /**
     * Footer.
     *
     * @param stream the stream
     * @param count  the count
     * @throws IOException the io exception
     */
    public void footer(OutputStream stream, long count) throws IOException {}

    /**
     * Process.
     *
     * @param stream  the stream
     * @param message the message
     * @param count   the count
     * @throws IOException the io exception
     */
    public abstract void process(OutputStream stream, T message, long count) throws IOException;

    /**
     * Header.
     *
     * @param stream the stream
     * @throws IOException the io exception
     */
    public void header(OutputStream stream) throws IOException {}

    /**
     * Close when footer written output stream sink.
     *
     * @return the output stream sink
     */
// Returns a new sink that closes the stream
    public final OutputStreamSink<T> closeWhenFooterWritten() {
        return new DelegatingOutputStreamSink<T>(this) {
            @Override
            public void footer(OutputStream stream, long count) throws IOException {
                try {
                    OutputStreamSink.this.footer(stream, count);
                } finally {
                    stream.close();
                }
            }
        };
    }

    /**
     * New flush every time sink output stream sink.
     *
     * @return the output stream sink
     */
    public final OutputStreamSink<T> newFlushEveryTimeSink() {
        return new OutputStreamSink<T>() {
            @Override
            public void footer(OutputStream stream, long count) throws IOException {
                OutputStreamSink.this.footer(stream, count);
                stream.flush();
            }

            @Override
            public void header(OutputStream stream) throws IOException {
                OutputStreamSink.this.header(stream);
                stream.flush();
            }

            @Override
            public void process(OutputStream stream, T message, long count) throws IOException {
                OutputStreamSink.this.process(stream, message, count);
                stream.flush();
            }
        };
    }

    /**
     * Write all.
     *
     * @param iterable the iterable
     * @param os       the os
     * @throws IOException the io exception
     */
    public final void writeAll(Iterable<T> iterable, OutputStream os) throws IOException {
        header(os);
        long count = 0;
        for (T t : iterable) {
            process(os, t, ++count);
        }
        footer(os, count);
    }

    /**
     * Write footer output stream sink.
     *
     * @param footer  the footer
     * @param charset the charset
     * @return the output stream sink
     */
    protected final OutputStreamSink<T> writeFooter(final String footer, final Charset charset) {
        requireNonNull(footer);
        requireNonNull(charset);
        return new DelegatingOutputStreamSink<T>(this) {
            @Override
            public void footer(OutputStream stream, long count) throws IOException {
                OutputStreamSink.this.footer(stream, count);
                stream.write(footer.getBytes(charset));
            }
        };
    }

    /**
     * Write footer ascii output stream sink.
     *
     * @param footer the footer
     * @return the output stream sink
     */
    protected final OutputStreamSink<T> writeFooterAscii(String footer) {
        return writeFooter(footer, StandardCharsets.US_ASCII);
    }

    /**
     * With fixed header output stream sink.
     *
     * @param header  the header
     * @param charset the charset
     * @return the output stream sink
     */
    protected final OutputStreamSink<T> withFixedHeader(final String header, final Charset charset) {
        requireNonNull(header);
        requireNonNull(charset);
        return new DelegatingOutputStreamSink<T>(this) {
            @Override
            public void header(OutputStream stream) throws IOException {
                stream.write(header.getBytes(charset));
                OutputStreamSink.this.header(stream);
            }
        };
    }

    /**
     * Write header ascii output stream sink.
     *
     * @param header the header
     * @return the output stream sink
     */
    protected final OutputStreamSink<T> writeHeaderAscii(String header) {
        return withFixedHeader(header, StandardCharsets.US_ASCII);
    }

    /**
     * To string sink output stream sink.
     *
     * @param <T>     the type parameter
     * @param charset the charset that should be used for encoding
     * @return output stream sink
     */
    public static <T> OutputStreamSink<T> toStringSink(final Charset charset) {
        requireNonNull(charset);
        return new OutputStreamSink<T>() {

            @Override
            public void process(OutputStream stream, T message, long count) throws IOException {
                String s = message.toString();
                stream.write(s.getBytes(charset));
                stream.write('\n');
            }
        };
    }

    /**
     * The type Delegating output stream sink.
     *
     * @param <T> the type parameter
     */
    static class DelegatingOutputStreamSink<T> extends OutputStreamSink<T> {
        private final OutputStreamSink<T> oss;

        /**
         * Instantiates a new Delegating output stream sink.
         *
         * @param oss the oss
         */
        DelegatingOutputStreamSink(OutputStreamSink<T> oss) {
            this.oss = requireNonNull(oss);
        }

        /** {@inheritDoc} */
        public void footer(OutputStream stream, long count) throws IOException {
            oss.footer(stream, count);
        }

        /** {@inheritDoc} */
        public void header(OutputStream stream) throws IOException {
            oss.header(stream);
        }

        /** {@inheritDoc} */
        public void process(OutputStream stream, T message, long count) throws IOException {
            oss.process(stream, message, count);
        }
    }
}
