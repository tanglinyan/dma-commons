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
package dk.dma.commons.util.concurrent;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CountedCompleter;
import java.util.function.Consumer;

import dk.dma.enav.util.function.EConsumer;

/**
 * The type Fork join util.
 *
 * @author Kasper Nielsen
 */
@SuppressWarnings("serial")
public class ForkJoinUtil {

    /**
     * Invokes the consumer for each element in the specified array.
     *
     * @param <E>      the type parameter
     * @param array    the array to consume
     * @param consumer the consumer of each element
     * @throws NullPointerException if the array or consumer is null
     */
    public static <E> void forEach(E[] array, Consumer<E> consumer) {
        requireNonNull(array, "array is null");
        requireNonNull(consumer, "consumer is null");
        new ForEach<>(null, consumer, array, 0, array.length).invoke();
    }


    /**
     * Invokes the consumer for each element in the specified array.
     *
     * @param <E>      the type parameter
     * @param array    the array to consume
     * @param consumer the consumer of each element
     * @throws Exception the exception
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


    /**
     * Invokes the specified consumer for each element in an array.
     *
     * @param <E> the type parameter
     */
    static class ForEach<E> extends CountedCompleter<Void> {

        /** The array of elements to process */
        private final E[] array;

        /** The index of the last element to process (excluding) */
        private final int hi;

        /** The index of the first element to process */
        private final int lo;

        /** The consumer to consume each element. */
        private final Consumer<E> op;

        /**
         * Instantiates a new For each.
         *
         * @param p     the p
         * @param op    the op
         * @param array the array
         * @param lo    the lo
         * @param hi    the hi
         */
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

    /**
     * Invokes the specified consumer for each element in an array.
     *
     * @param <E> the type parameter
     */
    static class ForEachE<E> extends CountedCompleter<Void> {

        /** The array of elements to process */
        private final E[] array;

        /** The index of the last element to process (excluding) */
        private final int hi;

        /** The index of the first element to process */
        private final int lo;

        /** The consumer to consume each element. */
        private final EConsumer<E> op;

        /**
         * Instantiates a new For each e.
         *
         * @param p     the p
         * @param op    the op
         * @param array the array
         * @param lo    the lo
         * @param hi    the hi
         */
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

    /**
     * A hack to find the original checked exception that was thrown
     */
    static class InnerRuntimeException extends RuntimeException {
        /**
         * Instantiates a new Inner runtime exception.
         *
         * @param e the e
         */
        InnerRuntimeException(Exception e) {
            super(e);
        }
    }
}
