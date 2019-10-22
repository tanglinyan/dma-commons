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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import com.google.common.collect.AbstractIterator;

/**
 * The type Iterators.
 *
 * @author Kasper Nielsen
 */
public class Iterators {

    /**
     * Counting iterator.
     *
     * @param <T>      the type parameter
     * @param iterator the iterator
     * @param counter  the counter
     * @return the iterator
     */
    public static <T> Iterator<T> counting(final Iterator<T> iterator, final AtomicLong counter) {
        requireNonNull(iterator);
        requireNonNull(counter);
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                T next = iterator.next();
                counter.incrementAndGet();
                return next;
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    /**
     * Returns the elements of {@code unfiltered} that satisfy a predicate.
     *
     * @param <T>        the type parameter
     * @param unfiltered the unfiltered
     * @param predicate  the predicate
     * @return the iterator
     */
    public static <T> Iterator<T> filter(final Iterator<T> unfiltered, final Predicate<? super T> predicate) {
        requireNonNull(unfiltered);
        requireNonNull(predicate);
        return new AbstractIterator<T>() {
            @Override
            protected T computeNext() {
                while (unfiltered.hasNext()) {
                    T element = unfiltered.next();
                    if (predicate.test(element)) {
                        return element;
                    }
                }
                return endOfData();
            }
        };
    }

    /**
     * Combine iterator.
     *
     * @param <T>        the type parameter
     * @param iterators  the iterators
     * @param comparator the comparator
     * @return the iterator
     */
    public static <T> Iterator<T> combine(Collection<? extends Iterator<T>> iterators, final Comparator<T> comparator) {
        if (iterators.size() == 1) {
            return iterators.iterator().next();
        }
        final PriorityQueue<Entry<T>> q = new PriorityQueue<>();
        for (Iterator<T> i : iterators) {
            Entry<T> e = new Entry<>(i, requireNonNull(comparator));
            if (e.next != null) { // only add it if it has at least 1 entry
                q.add(e);
            }
        }
        return new AbstractIterator<T>() {
            @Override
            protected T computeNext() {
                Entry<T> e = q.poll();
                if (e != null) {
                    T p = e.next;
                    if (e.iterator.hasNext()) {
                        e.next = e.iterator.next();
                        q.add(e);// add it again
                    }
                    return p;
                }
                return endOfData();
            }
        };

    }

    /**
     * The type Entry.
     *
     * @param <T> the type parameter
     */
    static class Entry<T> implements Comparable<Entry<T>> {
        /**
         * The Next.
         */
        T next;
        /**
         * The Iterator.
         */
        final Iterator<T> iterator;
        /**
         * The Comparator.
         */
        final Comparator<T> comparator;

        /**
         * Instantiates a new Entry.
         *
         * @param i          the
         * @param comparator the comparator
         */
        Entry(Iterator<T> i, Comparator<T> comparator) {
            this.iterator = requireNonNull(i);
            this.comparator = comparator;
            if (i.hasNext()) {
                next = i.next();
            }
        }

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public int compareTo(Entry<T> o) {
            if (comparator == null) {
                ((Comparable) next).compareTo(o.next);
            }
            return comparator.compare(this.next, o.next);
        }
    }

}
