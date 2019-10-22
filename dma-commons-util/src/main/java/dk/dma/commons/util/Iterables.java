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
package dk.dma.commons.util;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * The type Iterables.
 *
 * @author Kasper Nielsen
 */
public class Iterables {

    /**
     * Counting iterable.
     *
     * @param <T>      the type parameter
     * @param iterable the iterable
     * @param counter  the counter
     * @return the iterable
     */
    public static <T> Iterable<T> counting(final Iterable<T> iterable, final AtomicLong counter) {
        requireNonNull(iterable);
        requireNonNull(counter);
        return new Iterable<T>() {
            public Iterator<T> iterator() {
                return Iterators.counting(iterable.iterator(), counter);
            }
        };
    }

    /**
     * Filter iterable.
     *
     * @param <T>        the type parameter
     * @param unfiltered the unfiltered
     * @param predicate  the predicate
     * @return the iterable
     */
    public static <T> Iterable<T> filter(final Iterable<T> unfiltered, final Predicate<? super T> predicate) {
        requireNonNull(unfiltered);
        requireNonNull(predicate);
        return new Iterable<T>() {
            public Iterator<T> iterator() {
                return Iterators.filter(unfiltered.iterator(), predicate);
            }
        };
    }
}
