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
package dk.dma.enav.tracking;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import jsr166e.ConcurrentHashMapV8;
import jsr166e.ConcurrentHashMapV8.Action;
import jsr166e.ConcurrentHashMapV8.BiAction;
import jsr166e.ConcurrentHashMapV8.BiFun;
import dk.dma.app.management.ManagedAttribute;
import dk.dma.app.util.function.BiBlock;
import dk.dma.enav.model.geometry.Circle;
import dk.dma.enav.model.geometry.PositionTime;
import dk.dma.enav.model.geometry.Area;

/**
 * An object that tracks positions.
 * 
 * @author Kasper Nielsen
 */
/**
 * 
 * @author Kasper Nielsen
 */
public class PositionTracker<T> implements Runnable {

    /** All targets at last update. Must be read via synchronized */
    private Map<T, PositionTime> latest = new ConcurrentHashMapV8<>();

    /** All current subscriptions. */
    final ConcurrentHashMapV8<PositionUpdateHandler<? super T>, Subscription<T>> subscriptions = new ConcurrentHashMapV8<>();

    /** All targets that we are currently monitoring. */
    private final ConcurrentHashMapV8<T, PositionTime> targets = new ConcurrentHashMapV8<>();

    /**
     * Invokes the callback for every tracked object within the specified area of interest.
     * 
     * @param shape
     *            the area of interest
     * @param block
     *            the callback
     */
    public void forEachWithinArea(final Area shape, final BiBlock<T, PositionTime> block) {
        requireNonNull(shape, "shapeis null");
        requireNonNull(block, "block is null");
        targets.forEachInParallel(new BiAction<T, PositionTime>() {
            @Override
            public void apply(T a, PositionTime b) {
                block.accept(a, b);
            }
        });
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
        final ConcurrentHashMapV8<T, PositionTime> result = new ConcurrentHashMapV8<>();
        forEachWithinArea(shape, new BiBlock<T, PositionTime>() {
            public void accept(T a, PositionTime b) {
                if (shape.containedWithin(b)) {
                    result.put(a, b);
                }
            }
        });
        return result;
    }

    /** Should be scheduled to run every x second to update handlers. */
    public synchronized void run() {
        ConcurrentHashMapV8<T, PositionTime> current = new ConcurrentHashMapV8<>(targets);
        final Map<T, PositionTime> latest = this.latest;

        // We only want to process those that have been updated since last time
        final ConcurrentHashMapV8<T, PositionTime> updates = new ConcurrentHashMapV8<>();
        current.forEachInParallel(new BiAction<T, PositionTime>() {
            public void apply(T t, PositionTime pt) {
                PositionTime p = latest.get(t);
                if (p == null || !p.positionEquals(pt)) {
                    updates.put(t, pt);
                }
            }
        });
        // update each subscription with new positions
        subscriptions.forEachValueInParallel(new Action<Subscription<T>>() {
            public void apply(final Subscription<T> s) {
                s.updateWith(updates);
            }
        });
        // update latest with current latest
        this.latest = current;
    }

    public Subscription<T> subscribe(Area shape, PositionUpdateHandler<? super T> handler) {
        return subscribe(shape, handler, 100);
    }

    /**
     * @param shape
     * @param handler
     * @param slack
     *            is the precision with which we want to report entering/exiting messages. We use it to avoid situations
     *            where a boat sails on a boundary line and keeps changing from being inside to outside of it
     * @return
     */
    public Subscription<T> subscribe(Area shape, PositionUpdateHandler<? super T> handler, double slack) {
        if (slack < 0) {
            throw new IllegalArgumentException("Slack must be non-negative, was " + slack);
        }
        Area exitShape = requireNonNull(shape, "shape is null");
        if (slack > 0) {
            if (shape instanceof Circle) {
                Circle c = (Circle) shape;
                exitShape = c.withRadius(c.getRadius() + slack);
            } else {
                // somebody implement box
                throw new UnsupportedOperationException("Only circles allowed for now");
            }
        }
        Subscription<T> s = new Subscription<>(this, handler, shape, exitShape);
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
        targets.merge(target, positionTime, new BiFun<PositionTime, PositionTime, PositionTime>() {
            public PositionTime apply(PositionTime a, PositionTime b) {
                return a.getTime() >= b.getTime() ? a : b;
            }
        });
    }
}
