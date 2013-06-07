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
package dk.dma.commons.service;

import java.util.ArrayList;
import java.util.Collection;

/**
 * supporting orderly shutdown.
 * 
 * @author Kasper Nielsen
 */
public abstract class AbstractBatchedStage<T> extends AbstractMessageProcessorService<T> {

    private final int maxBatchSize;

    protected AbstractBatchedStage(int queueSize, int maxBatchSize) {
        super(queueSize);
        if (maxBatchSize < 1) {
            throw new IllegalArgumentException("maxBatchSize must be at least 1");
        }
        this.maxBatchSize = maxBatchSize;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected final void run() throws Exception {
        executionThread = Thread.currentThread();
        while (state() == State.RUNNING || !queue.isTerminated()) {
            // This is not the most efficient solution but it is good enough for us.
            T t = takeInterruptable();
            if (t != null) {// okay we might have more more than one element
                ArrayList<T> list = new ArrayList<>(maxBatchSize);
                list.add(t);
                queue.drainToBlocking((Collection<? super Object>) list.subList(1, list.size()), maxBatchSize - 1);
                handleMessages(list);
                numberProcessed.addAndGet(list.size());
            }
        }
        onShutdown();
    }

    protected void onShutdown() {}

}
