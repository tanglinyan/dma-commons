/*
 * Copyright (c) 2008 Kasper Nielsen.
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
package dk.dma.app.io;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import dk.dma.app.util.function.Predicate;

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
