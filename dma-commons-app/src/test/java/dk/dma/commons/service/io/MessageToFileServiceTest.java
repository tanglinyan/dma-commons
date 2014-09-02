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
package dk.dma.commons.service.io;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import dk.dma.commons.util.io.OutputStreamSink;

/**
 * 
 * @author Kasper Nielsen
 */
public class MessageToFileServiceTest {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @Ignore
    public void test() throws IOException {
        MessageToFileService<Integer> s = MessageToFileService.dateTimeService(Paths.get("."), "yyyy",
                (OutputStreamSink) OutputStreamSink.TO_STRING_US_ASCII_SINK);
        s.handleMessages(Arrays.asList(1, 2, 3));
        s.ros.flush();
        s.stopAsync();
    }
}
