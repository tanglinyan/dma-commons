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

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Supplier;

/**
 * 
 * @author Kasper Nielsen
 */
final class PathSuppliers {
    static final Supplier<Path> EXPLICIT_ROLL = new Supplier<Path>() {
        @Override
        public Path get() {
            throw new IllegalStateException("Must explicit roll through RollingOutputStream#roll(Path p)");
        }
    };

    private PathSuppliers() {}

    /**
     * A path supplier that creates absolute paths with an increasing filename postfix such as "foo1.txt", "foo2.txt",
     * ... "foon.txt"
     * 
     * @param root
     * @param prefix
     * @param postfix
     * @return
     */
    static Supplier<Path> increasingAbsolutePaths(final Path root, final String prefix, final String postfix) {
        requireNonNull(root);
        requireNonNull(prefix);
        requireNonNull(postfix);
        return new Supplier<Path>() {
            final AtomicLong l = new AtomicLong();

            @Override
            public Path get() {
                String filename = prefix + l.incrementAndGet() + "." + postfix;
                return root.resolve(filename);
            }
        };
    }
}
