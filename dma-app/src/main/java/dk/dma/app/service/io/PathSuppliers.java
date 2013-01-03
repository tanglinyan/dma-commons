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
package dk.dma.app.service.io;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Supplier;

/**
 * 
 * @author Kasper Nielsen
 */
class PathSuppliers {
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
