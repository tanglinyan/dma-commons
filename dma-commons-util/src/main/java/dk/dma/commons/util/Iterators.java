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
package dk.dma.commons.util;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import com.google.common.collect.AbstractIterator;

import dk.dma.enav.util.function.Predicate;

/**
 * 
 * @author Kasper Nielsen
 */
public class Iterators {

    /**
     * Returns the elements of {@code unfiltered} that satisfy a predicate.
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

    public static <T> Iterator<T> combine(Iterable<? extends Iterator<T>> iterators, final Comparator<T> comparator) {
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

    static class Entry<T> implements Comparable<Entry<T>> {
        T next;
        final Iterator<T> iterator;
        final Comparator<T> comparator;

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
