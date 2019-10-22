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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * The type Rolling output stream test.
 *
 * @author Kasper Nielsen
 */
public class RollingOutputStreamTest {

    /**
     * Test single file.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testSingleFile() throws IOException {
        Path p = Files.createTempFile("aaa", "bbb");
        Files.delete(p);// delete it again
        Path tmpP = Paths.get(p.toAbsolutePath().toString() + ".tmp"); // the temp file
        try {
            try (RollingOutputStream ros = new RollingOutputStream()) {
                ros.roll(p);

                assertFalse(Files.exists(p));
                assertFalse(Files.exists(tmpP));
                ros.write(12); // opens and writes file
                assertFalse(Files.exists(p));
                assertTrue(Files.exists(tmpP));

                ros.write(14);
                ros.write(new byte[] { 1, 2, 3 });
                assertFalse(Files.exists(p));
                ros.close();
                assertTrue(Files.exists(p));
                assertArrayEquals(new byte[] { 12, 14, 1, 2, 3 }, Files.readAllBytes(p));
            }
        } finally {
            Files.delete(p);
        }
    }

    /**
     * Test multiple files.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testMultipleFiles() throws IOException {
        Path p1 = Files.createTempFile("aaa", "bbb");
        Path p2 = Files.createTempFile("aaa", "bbb");
        Files.delete(p1);// delete it again
        Files.delete(p2);// delete it again
        Path tmpP1 = Paths.get(p1.toAbsolutePath().toString() + ".tmp"); // the temp file
        Path tmpP2 = Paths.get(p2.toAbsolutePath().toString() + ".tmp"); // the temp file

        try {
            try (RollingOutputStream ros = new RollingOutputStream()) {
                ros.roll(p1);
                ros.write(12); // opens and writes file

                assertFalse(Files.exists(p1));
                assertFalse(Files.exists(p2));
                assertTrue(Files.exists(tmpP1));
                assertFalse(Files.exists(tmpP2));
                ros.roll(p2);
                assertTrue(Files.exists(p1));
                assertFalse(Files.exists(p2));
                assertFalse(Files.exists(tmpP1));
                assertFalse(Files.exists(tmpP2));
                assertArrayEquals(new byte[] { 12 }, Files.readAllBytes(p1));

                ros.write(15); // opens and writes file
                assertTrue(Files.exists(p1));
                assertFalse(Files.exists(p2));
                assertFalse(Files.exists(tmpP1));
                assertTrue(Files.exists(tmpP2));

                ros.close();

                assertTrue(Files.exists(p1));
                assertTrue(Files.exists(p2));
                assertFalse(Files.exists(tmpP1));
                assertFalse(Files.exists(tmpP2));
            }
        } finally {
            Files.delete(p1);
            Files.delete(p2);
        }
    }
}
