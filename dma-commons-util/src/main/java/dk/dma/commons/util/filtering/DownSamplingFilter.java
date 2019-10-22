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
package dk.dma.commons.util.filtering;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import dk.dma.commons.management.ManagedAttribute;
import dk.dma.commons.management.ManagedOperation;

/**
 * A non-blocking down sampling filter.
 *
 * @param <T> the type parameter
 * @author Kasper Nielsen
 */
public class DownSamplingFilter<T> implements Predicate<T> {

    /**
     * The number of elements accepted by this filter.
     */
    final AtomicLong accepted = new AtomicLong();

    /**
     * The cache of encountered events.
     */
    final ConcurrentHashMap<T, Long> cache = new ConcurrentHashMap<>();

    /**
     * The number of elements rejected by this filter.
     */
    final AtomicLong rejected = new AtomicLong();

    /**
     * The sampling rate.
     */
    volatile long samplingRateNanos;

    /**
     * Creates a new down sampling filter with a sampling rate of 1 minute.
     */
    public DownSamplingFilter() {
        this(1, TimeUnit.MINUTES);
    }

    /**
     * Instantiates a new Down sampling filter.
     *
     * @param rate the rate
     * @param unit the unit
     */
    public DownSamplingFilter(long rate, TimeUnit unit) {
        setSamplingRate(rate, unit);
        // Add a cleaning thread, just in case we have non recurring targets
        Cleaner.add(new Runnable() {
            @Override
            public void run() {
                long toOld = System.nanoTime() - samplingRateNanos * 2;// remove old old
                Iterator<Long> i = cache.values().iterator();
                while (i.hasNext()) {
                    if (i.next() <= toOld) {
                        i.remove();
                    }
                }
            }
        });
    }

    /**
     * Clears all internal data in the filter.
     */
    @ManagedOperation
    public void clear() {
        cache.clear();
    }

    /**
     * Gets acceptance count.
     *
     * @return the acceptance count
     */
    @ManagedAttribute
    public long getAcceptanceCount() {
        return accepted.get();
    }

    /**
     * Gets cache size.
     *
     * @return the cache size
     */
    @ManagedAttribute
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * Gets total count.
     *
     * @return the total count
     */
    @ManagedAttribute
    public long getTotalCount() {
        return getAcceptanceCount() + getRejectionCount();
    }

    /**
     * Gets rejection count.
     *
     * @return the rejection count
     */
    @ManagedAttribute
    public long getRejectionCount() {
        return rejected.get();
    }

    /**
     * Gets sampling rate.
     *
     * @param unit the unit
     * @return the sampling rate
     */
    public long getSamplingRate(TimeUnit unit) {
        return unit.convert(samplingRateNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Sets sampling rate.
     *
     * @param rate the rate
     * @param unit the unit
     * @return the sampling rate
     */
    public DownSamplingFilter<T> setSamplingRate(long rate, TimeUnit unit) {
        if (rate < 0) {
            throw new IllegalArgumentException("Sampling rate must be non-negative, was " + rate);
        }
        samplingRateNanos = unit.toNanos(rate);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public boolean test(T element) {
        requireNonNull(element);
        long samplingRateNanos = this.samplingRateNanos; // make sure we sampling does not change
        if (samplingRateNanos == 0) {
            return true;
        }

        for (;;) {
            // Get last received
            Long lastTimestamp = cache.get(element);
            Long now = new Long(System.nanoTime());// we use the identity of the long so dont autobox
            // test if we have seen the element before
            if (lastTimestamp == null) {
                if (cache.putIfAbsent(element, now) == null) {// let see if are empty
                    accepted.incrementAndGet();
                    return true;
                }
                // woops some one beat os to it lets try again
            } else if (now - lastTimestamp <= samplingRateNanos) {
                rejected.incrementAndGet();
                return false;
            } else {
                // we have the extra check map.get()== now to make sure some other thread
                // did not get the same timestamp (replace uses equality and not identify to check)
                if (cache.replace(element, lastTimestamp, now) && cache.get(element) == now) {
                    accepted.incrementAndGet();
                    return true;
                }
            }
        }
    }
}
