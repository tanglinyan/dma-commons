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
package dk.dma.commons.util.filtering;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import dk.dma.commons.management.ManagedAttribute;
import dk.dma.commons.management.ManagedOperation;
import dk.dma.enav.util.function.Predicate;

/**
 * A non-blocking down sampling filter.
 * 
 * @author Kasper Nielsen
 */
public class DownSamplingFilter<T> extends Predicate<T> {

    /** The number of elements accepted by this filter. */
    final AtomicLong accepted = new AtomicLong();

    /** The cache of encountered events. */
    final ConcurrentHashMap<T, Long> cache = new ConcurrentHashMap<>();

    /** The number of elements rejected by this filter. */
    final AtomicLong rejected = new AtomicLong();

    /** The sampling rate. */
    volatile long samplingRateNanos;

    /** Creates a new down sampling filter with a sampling rate of 1 minute. */
    public DownSamplingFilter() {
        this(1, TimeUnit.MINUTES);
    }

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

    /** Clears all internal data in the filter. */
    @ManagedOperation
    public void clear() {
        cache.clear();
    }

    @ManagedAttribute
    public long getAcceptanceCount() {
        return accepted.get();
    }

    @ManagedAttribute
    public int getCacheSize() {
        return cache.size();
    }

    @ManagedAttribute
    public long getTotalCount() {
        return getAcceptanceCount() + getRejectionCount();
    }

    @ManagedAttribute
    public long getRejectionCount() {
        return rejected.get();
    }

    public long getSamplingRate(TimeUnit unit) {
        return unit.convert(samplingRateNanos, TimeUnit.NANOSECONDS);
    }

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
