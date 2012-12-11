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
package dk.dma.app.util.service;


/**
 * 
 * @author Kasper Nielsen
 */
public abstract class AbstractStage<T> extends AbstractMessageProcessorService<T> {

    /**
     * @param queueSize
     */
    protected AbstractStage(int queueSize) {
        super(queueSize);
    }

    protected abstract void handleMessage(T message);

    /** {@inheritDoc} */
    @Override
    protected final void run() throws Exception {
        executionThread = Thread.currentThread();
        while (state() == State.RUNNING || !queue.isTerminated()) {
            T t = takeInterruptable();
            if (t != null) {
                handleMessage(t);
            }
        }
    }
}
