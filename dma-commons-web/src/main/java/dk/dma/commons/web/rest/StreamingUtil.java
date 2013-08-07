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
package dk.dma.commons.web.rest;

import static java.util.Objects.requireNonNull;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import dk.dma.commons.util.io.OutputStreamSink;

/**
 * 
 * @author Kasper Nielsen
 */
public class StreamingUtil {

    /**
     * Creates a streaming output from the specified iterable and output stream sink.
     * 
     * @param i
     *            the iterable to stream
     * @param sink
     *            the sink to stream to
     * @return
     */
    public static <T> StreamingOutput createStreamingOutput(final Iterable<T> i, final OutputStreamSink<T> sink) {
        requireNonNull(i);
        requireNonNull(sink);
        return new StreamingOutput() {
            @Override
            public void write(OutputStream paramOutputStream) throws IOException {
                try {
                    try (BufferedOutputStream bos = new BufferedOutputStream(paramOutputStream);) {
                        sink.writeAll(i, bos);
                    }
                    paramOutputStream.close();
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }
            }
        };
    }
}
