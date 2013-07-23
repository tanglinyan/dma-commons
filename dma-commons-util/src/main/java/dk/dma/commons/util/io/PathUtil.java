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
