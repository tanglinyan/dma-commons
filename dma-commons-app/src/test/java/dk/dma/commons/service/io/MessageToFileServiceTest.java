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
package dk.dma.commons.service.io;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Test;

import dk.dma.commons.util.io.OutputStreamSink;

/**
 * 
 * @author Kasper Nielsen
 */
public class MessageToFileServiceTest {

    @Test
    public void test() throws IOException {
        MessageToFileService<Integer> s = MessageToFileService.dateTimeService(Paths.get("."), "yyyy",
                (OutputStreamSink) OutputStreamSink.TO_STRING_US_ASCII_SINK);
        s.handleMessages(Arrays.asList(1, 2, 3));
        s.ros.flush();
        s.stop();
    }
}
