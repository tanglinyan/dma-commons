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
package dk.dma.commons.tracker;

import dk.dma.enav.model.geometry.PositionTime;

/**
 * A position update handler is used for notifying the user about updated positions within the area of interest.
 * 
 * @author Kasper Nielsen
 */
public abstract class PositionUpdateHandler<T> {

    /**
     * Invoked whenever an object enters the area of interest.
     * 
     * @param t
     *            the tracked object
     * @param positiontime
     *            the position and time when entering
     */
    protected void entering(T t, PositionTime positiontime) {}

    /**
     * Invoked whenever a tracked object position is updated.
     * 
     * @param t
     *            the tracked object
     * @param previous
     *            the previous position (is null if entering the area of interest)
     * @param current
     *            the current position (is null if existing the area of interest)
     */
    protected void updated(T t, PositionTime previous, PositionTime current) {}

    /**
     * Invoked whenever an object leaves the area of interest.
     * 
     * @param t
     *            the tracked object
     */
    protected void exiting(T t) {}
}
