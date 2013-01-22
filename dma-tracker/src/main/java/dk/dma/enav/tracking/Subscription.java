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

import java.util.HashMap;
import java.util.Map;

import jsr166e.ConcurrentHashMapV8;
import jsr166e.ConcurrentHashMapV8.Action;
import dk.dma.app.util.function.Block;
import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.geometry.PositionTime;
import dk.dma.enav.model.geometry.Area;

/**
 * A subscription is created for each {@link PositionUpdateHandler}. It is to use unsubscribe ({@link #cancel()}).
 * 
 * @author Kasper Nielsen
 */
public class Subscription<T> {

    /** A map of currently tracked objects for this subscription. */
    private final ConcurrentHashMapV8<T, PositionTime> trackedObjects = new ConcurrentHashMapV8<>();

    /** The tracker that this subscription is registered with. */
    private final PositionTracker<T> tracker;

    /** The shape we look at to see if we are entering the area of interest. */
    private final Area shapeEntering;

    /** The shape we look at to see if we are exiting the area of interest. */
    private final Area shapeExiting;

    /** The handler that should be called whenever objects are entering/exiting. */
    private final PositionUpdateHandler<? super T> handler;

    Subscription(PositionTracker<T> tracker, PositionUpdateHandler<? super T> handler, Area shape, Area exitShape) {
        this.tracker = requireNonNull(tracker);
        this.shapeEntering = requireNonNull(shape);
        this.shapeExiting = requireNonNull(exitShape);
        this.handler = requireNonNull(handler);
    }

    /** Cancels the subscription and free up any resources. */
    public synchronized void cancel() {
        if (tracker.subscriptions.remove(handler, this)) {
            trackedObjects.clear();
        }
    }

    /**
     * Called regular by the position tracked with updated positions. If any of updated objects are within the area of
     * interest. This class must notify the installed handler.
     * 
     * @param updates
     *            the position that have been updated since this method was last invoked
     */
    synchronized void updateWith(ConcurrentHashMapV8<T, PositionTime> updates) {
        for (Map.Entry<T, PositionTime> e : updates.entrySet()) {
            T t = e.getKey();
            PositionTime pt = e.getValue();
            PositionTime current = trackedObjects.get(t);
            boolean positionChanged = current == null || pt == null || !current.positionEquals(pt);
            if (current == null) {// not tracked
                if (shapeEntering.containedWithin(pt)) {
                    trackedObjects.put(t, pt);
                    handler.entering(t, pt);
                }
            } else if (!shapeExiting.containedWithin(pt)) {
                handler.exiting(t);
                trackedObjects.remove(t);
            } else {
                if (positionChanged) {
                    handler.updated(t, current, pt);
                }
                trackedObjects.put(t, pt);
            }
        }
    }

    /**
     * Returns a map of tracked objects with their current position.
     * 
     * @return a map of tracked objects with their current position
     */
    public Map<T, Position> getTrackedObjects() {
        // We could result trackedObjects directly. But we do not want to return PositionTime objects
        // because the time is not the latest report. But the first time with the current position.
        // Which would be easily to mistake for users.
        HashMap<T, Position> result = new HashMap<>();
        for (Map.Entry<T, PositionTime> e : trackedObjects.entrySet()) {
            result.put(e.getKey(), Position.create(e.getValue().getLatitude(), e.getValue().getLongitude()));
        }
        return result;
    }

    /**
     * Returns the number of tracked objects.
     * 
     * @return the number of tracked objects
     */
    public int getNumberOfTrackedObjects() {
        return trackedObjects.size();
    }

    /**
     * Performs the given block for each tracked object in parallel.
     * 
     * @param block
     *            the block
     */
    public void forEachTrackedObject(final Block<T> block) {
        requireNonNull(block, "block is null");
        trackedObjects.forEachKeyInParallel(new Action<T>() {
            @Override
            public void apply(T t) {
                block.accept(t);
            }
        });
    }

}
