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
package dk.dma.commons.tracker;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import dk.dma.commons.management.ManagedAttribute;
import dk.dma.enav.model.geometry.Area;
import dk.dma.enav.model.geometry.Circle;
import dk.dma.enav.model.geometry.PositionTime;

/**
 * An object that tracks positions.
 * 
 * @author Kasper Nielsen
 */
public class PositionTracker<T> {

    /** Magic constant. */
    static final int THRESHOLD = 1;

    /** All targets at last update. Must be read via synchronized */
    private ConcurrentHashMap<T, PositionTime> latest = new ConcurrentHashMap<>();

    /** All current subscriptions. */
    final ConcurrentHashMap<PositionUpdatedHandler<? super T>, Subscription<T>> subscriptions = new ConcurrentHashMap<>();

    /** All targets that we are currently monitoring. */
    private final ConcurrentHashMap<T, PositionTime> targets = new ConcurrentHashMap<>();

    /**
     * Invokes the callback for every tracked object within the specified area of interest.
     * 
     * @param shape
     *            the area of interest
     * @param block
     *            the callback
     */
    public void forEachWithinArea(final Area shape, final BiConsumer<T, PositionTime> block) {
        requireNonNull(shape, "shape is null");
        requireNonNull(block, "block is null");
        targets.forEach(THRESHOLD, new BiConsumer<T, PositionTime>() {
            @Override
            public void accept(T a, PositionTime b) {
                if (shape.contains(b)) {
                    block.accept(a, b);
                }
            }
        });
    }

    public PositionTime getLatestIfLaterThan(T target, long time) {
        PositionTime t = getLatest(target);
        return t != null && time < t.getTime() ? t : null;
    }

    /**
     * Returns the latest position time updated for the specified target. Or <code>null</code> if no position has ever
     * been recorded for the target.
     * 
     * 
     * @param target
     *            the target
     * @return the latest position time updated
     */
    public PositionTime getLatest(T target) {
        return latest.get(target);
    }

    /**
     * Returns the number of subscriptions.
     * 
     * @return the number of subscriptions
     */
    @ManagedAttribute
    public int getNumberOfSubscriptions() {
        return subscriptions.size();
    }

    /**
     * Returns the number of tracked objects.
     * 
     * @return the number of tracked objects
     */
    @ManagedAttribute
    public int getNumberOfTrackedObjects() {
        return targets.size();
    }

    /**
     * Returns a map of all tracked objects and their latest position.
     * 
     * @param shape
     *            the area of interest
     * @return a map of all tracked objects within the area as keys and their latest position as the value
     */
    public Map<T, PositionTime> getTargetsWithin(final Area shape) {
        final ConcurrentHashMap<T, PositionTime> result = new ConcurrentHashMap<>();
        forEachWithinArea(shape, new BiConsumer<T, PositionTime>() {
            public void accept(T a, PositionTime b) {
                if (shape.contains(b)) {
                    result.put(a, b);
                }
            }
        });
        return result;
    }

    public boolean remove(T t) {
        return latest.remove(t) != null;
    }

    public Future<?> schedule(ScheduledExecutorService ses, int updatePeriodMS) {
        return ses.scheduleAtFixedRate(new Runnable() {
            public void run() {
                doRun();
            }
        }, 0, updatePeriodMS, TimeUnit.MILLISECONDS);
    }

    /** Should be scheduled to run every x second to update handlers. */
    synchronized void doRun() {
        ConcurrentHashMap<T, PositionTime> current = new ConcurrentHashMap<>(targets);
        final Map<T, PositionTime> latest = this.latest;

        // We only want to process those that have been updated since last time
        final ConcurrentHashMap<T, PositionTime> updates = new ConcurrentHashMap<>();
        current.forEach(THRESHOLD, new BiConsumer<T, PositionTime>() {
            public void accept(T t, PositionTime pt) {
                PositionTime p = latest.get(t);
                if (p == null || !p.positionEquals(pt)) {
                    updates.put(t, pt);
                }
            }
        });
        // update each subscription with new positions
        subscriptions.forEachValue(THRESHOLD, new Consumer<Subscription<T>>() {
            public void accept(final Subscription<T> s) {
                s.updateWith(updates);
            }
        });
        // update latest with current latest
        this.latest = current;
    }

    /**
     * Subscribes to changes in the specified area.
     * 
     * @param area
     *            the area to monitor
     * @param handler
     *            a subscription that can be used to cancel the subscription
     * @return
     */
    public Subscription<T> subscribe(Area area, PositionUpdatedHandler<? super T> handler) {
        return subscribe(area, handler, 100);
    }

    /**
     * Subscribes to changes in the specified area.
     * 
     * @param area
     *            the area to monitor
     * @param handler
     *            a subscription that can be used to cancel the subscription
     * @param slack
     *            is the precision in meters with which we want to report entering/exiting messages. We use it to avoid
     *            situations where a boat sails on a boundary line and keeps changing from being inside to outside of it
     * @return
     */
    public Subscription<T> subscribe(Area area, PositionUpdatedHandler<? super T> handler, double slack) {
        Area exitShape = requireNonNull(area, "area is null");
        if (slack < 0) {
            throw new IllegalArgumentException("Slack must be non-negative, was " + slack);
        }
        if (slack > 0) {
            if (area instanceof Circle) {
                Circle c = (Circle) area;
                exitShape = c.withRadius(c.getRadius() + slack);
            } else {
                throw new UnsupportedOperationException("Only circles allowed for now");
            }
        }
        Subscription<T> s = new Subscription<>(this, handler, area, exitShape);
        if (subscriptions.putIfAbsent(handler, s) != null) {
            throw new IllegalArgumentException("The specified handler has already been registered");
        }
        return s;
    }

    /**
     * Updates the current position of the specified target.
     * 
     * @param target
     *            the target
     * @param positionTime
     *            the position and reported time
     */
    public void update(T target, PositionTime positionTime) {
        requireNonNull(positionTime, "positionTime is null"); // target gets checked in merge
        // make sure we keep the positiontime with the highest timestamp
        targets.merge(target, positionTime, new BiFunction<PositionTime, PositionTime, PositionTime>() {
            public PositionTime apply(PositionTime a, PositionTime b) {
                return a.getTime() >= b.getTime() ? a : b;
            }
        });
    }
}
