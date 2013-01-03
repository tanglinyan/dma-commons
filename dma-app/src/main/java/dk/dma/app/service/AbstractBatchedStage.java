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
package dk.dma.app.service;

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
                queue.drainTo((Collection<? super Object>) list.subList(1, list.size()), maxBatchSize - 1);
                handleMessages(list);
                numberProcessed.addAndGet(list.size());
            }
        }
    }
}
