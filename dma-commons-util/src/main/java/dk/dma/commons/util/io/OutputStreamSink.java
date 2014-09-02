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
 * @author Kasper Nielsen
 */
public abstract class OutputStreamSink<T> {

    /** A sink that will ignore any input. */
    public static final OutputStreamSink<Object> IGNORE = new OutputStreamSink<Object>() {

        /** {@inheritDoc} */
        @Override
        public void process(OutputStream os, Object msg, long count) {}
    };

    public static final OutputStreamSink<Object> TO_STRING_US_ASCII_SINK = toStringSink(StandardCharsets.US_ASCII);

    public static final OutputStreamSink<Object> TO_STRING_UTF8_SINK = toStringSink(StandardCharsets.UTF_8);

    public void footer(OutputStream stream, long count) throws IOException {}

    public abstract void process(OutputStream stream, T message, long count) throws IOException;

    public void header(OutputStream stream) throws IOException {}

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

    public final void writeAll(Iterable<T> iterable, OutputStream os) throws IOException {
        header(os);
        long count = 0;
        for (T t : iterable) {
            process(os, t, ++count);
        }
        footer(os, count);
    }

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

    protected final OutputStreamSink<T> writeFooterAscii(String footer) {
        return writeFooter(footer, StandardCharsets.US_ASCII);
    }

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

    protected final OutputStreamSink<T> writeHeaderAscii(String header) {
        return withFixedHeader(header, StandardCharsets.US_ASCII);
    }

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
            public void process(OutputStream stream, T message, long count) throws IOException {
                String s = message.toString();
                stream.write(s.getBytes(charset));
                stream.write('\n');
            }
        };
    }

    static class DelegatingOutputStreamSink<T> extends OutputStreamSink<T> {
        private final OutputStreamSink<T> oss;

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
