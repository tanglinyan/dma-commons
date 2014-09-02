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

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 
 * @author Kasper Nielsen
 */
public class PathUtil {

    /**
     * If the specified path does not exist. Returns the specified path. Otherwise returns a unique path similar to the
     * specified path.
     * 
     * @param path
     *            the patch to check
     * @return a unique path
     */
    public static Path findUnique(Path path) {
        if (!Files.exists(path)) {
            return path;
        }
        String filename = path.getFileName().toString();
        String postfix = "";
        if (filename.contains(".")) { // has suffix
            postfix = filename.substring(filename.indexOf('.'));
            filename = filename.substring(0, filename.indexOf('.'));
        }
        Path p = null;
        int i = 1;
        do {
            p = path.resolveSibling(filename + "-" + i++ + postfix);
        } while (Files.exists(p));
        return p;
    }
}
