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
package dk.dma.commons.util.concurrent;

import static java.util.Objects.requireNonNull;
import jsr166e.CountedCompleter;
import dk.dma.enav.util.function.Consumer;
import dk.dma.enav.util.function.EConsumer;


/**
 * 
 * @author Kasper Nielsen
 */
@SuppressWarnings("serial")
public class ForkJoinUtil {

    /**
     * Invokes the consumer for each element in the specified array.
     * 
     * @param array
     *            the array to consume
     * @param consumer
     *            the consumer of each element
     * @throws NullPointerException
     *             if the array or consumer is null
     */
    public static <E> void forEach(E[] array, Consumer<E> consumer) {
        requireNonNull(array, "array is null");
        requireNonNull(consumer, "consumer is null");
        new ForEach<>(null, consumer, array, 0, array.length).invoke();
    }


    /**
     * Invokes the consumer for each element in the specified array.
     * 
     * @param array
     *            the array to consume
     * @param consumer
     *            the consumer of each element
     * @throws NullPointerException
     *             if the array or consumer is null
     */
    public static <E> void forEach(E[] array, EConsumer<E> consumer) throws Exception {
        requireNonNull(array, "array is null");
        requireNonNull(consumer, "consumer is null");
        try {
            new ForEachE<>(null, consumer, array, 0, array.length).invoke();
        } catch (InnerRuntimeException i) {
            throw (Exception) i.getCause();
        }
    }

    /** Invokes the specified consumer for each element in an array. */
    static class ForEach<E> extends CountedCompleter<Void> {

        /** The array of elements to process */
        private final E[] array;

        /** The index of the last element to process (excluding) */
        private final int hi;

        /** The index of the first element to process */
        private final int lo;

        /** The consumer to consume each element. */
        private final Consumer<E> op;

        ForEach(CountedCompleter<?> p, Consumer<E> op, E[] array, int lo, int hi) {
            super(p);
            this.array = array;
            this.op = op;
            this.lo = lo;
            this.hi = hi;
        }

        public void compute() {
            int l = lo, h = hi;
            while (h - l >= 2) {
                int mid = l + h >>> 1;
                addToPendingCount(1);
                ForEach<E> fe = new ForEach<>(this, op, array, mid, h);
                fe.fork(); // right child
                h = mid;
            }
            if (h > l) {
                op.accept(array[l]);
            }
            propagateCompletion();
        }
    }

    /** Invokes the specified consumer for each element in an array. */
    static class ForEachE<E> extends CountedCompleter<Void> {

        /** The array of elements to process */
        private final E[] array;

        /** The index of the last element to process (excluding) */
        private final int hi;

        /** The index of the first element to process */
        private final int lo;

        /** The consumer to consume each element. */
        private final EConsumer<E> op;

        ForEachE(CountedCompleter<?> p, EConsumer<E> op, E[] array, int lo, int hi) {
            super(p);
            this.array = array;
            this.op = op;
            this.lo = lo;
            this.hi = hi;
        }

        public void compute() {
            int l = lo, h = hi;
            while (h - l >= 2) {
                int mid = l + h >>> 1;
                addToPendingCount(1);
                ForEachE<E> fe = new ForEachE<>(this, op, array, mid, h);
                fe.fork(); // right child
                h = mid;
            }
            if (h > l) {
                try {
                    op.accept(array[l]);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    completeExceptionally(new InnerRuntimeException(e));
                }
            }
            propagateCompletion();
        }
    }

    /** A hack to find the original checked exception that was thrown */
    static class InnerRuntimeException extends RuntimeException {
        InnerRuntimeException(Exception e) {
            super(e);
        }
    }
}
