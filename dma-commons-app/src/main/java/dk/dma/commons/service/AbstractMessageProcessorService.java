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
package dk.dma.commons.service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

import dk.dma.commons.management.ManagedAttribute;

/**
 * supporting orderly shutdown.
 *
 * @param <T> the type parameter
 * @author Kasper Nielsen
 */
public abstract class AbstractMessageProcessorService<T> extends AbstractExecutionThreadService {

    /**
     * The Execution thread.
     */
    volatile Thread executionThread;

    private volatile boolean isInInterruptableBlock;

    /**
     * The Queue.
     */
    final ShutdownBlockingQueue<Object> queue;
    /**
     * The Number processed.
     */
    final AtomicLong numberProcessed = new AtomicLong();

    /**
     * Instantiates a new Abstract message processor service.
     *
     * @param queueSize the queue size
     */
    protected AbstractMessageProcessorService(int queueSize) {
        queue = new ShutdownBlockingQueue<>(queueSize);
    }

    /**
     * Gets input queue.
     *
     * @return the input queue
     */
    @SuppressWarnings("unchecked")
    public BlockingQueue<T> getInputQueue() {
        return (BlockingQueue<T>) queue;
    }

    /**
     * Gets number of messages processed.
     *
     * @return the number of messages processed
     */
    @ManagedAttribute
    public long getNumberOfMessagesProcessed() {
        return numberProcessed.get();
    }

    /**
     * Gets size.
     *
     * @return the size
     */
    public int getSize() {
        return queue.size();
    }

    /**
     * Handle messages.
     *
     * @param messages the messages
     * @throws Exception the exception
     */
    protected abstract void handleMessages(List<T> messages) throws Exception;

    /**
     * Poll interruptable t.
     *
     * @param queue   the queue
     * @param timeout the timeout
     * @param unit    the unit
     * @return the t
     */
    T pollInterruptable(ShutdownBlockingQueue<T> queue, long timeout, TimeUnit unit) {
        try {
            isInInterruptableBlock = true;
            T t = queue.poll(timeout, unit);
            synchronized (this) {
                isInInterruptableBlock = false;
                return t;
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
            return null;
        }
    }

    /**
     * Take interruptable t.
     *
     * @return the t
     */
    T takeInterruptable() {
        try {
            isInInterruptableBlock = true;
            @SuppressWarnings("unchecked")
            T t = (T) queue.take();
            synchronized (this) {
                isInInterruptableBlock = false;
                return t;
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
            return null;
        }
    }

    //
    // T takeInterruptable(ShutdownBlockingQueue<T> queue) {
    // try {
    // isInInterruptableBlock = true;
    // T t = queue.take();
    // synchronized (this) {
    // isInInterruptableBlock = false;
    // return t;
    // }
    // } catch (InterruptedException e) {
    // Thread.interrupted();
    // return null;
    // }
    // }

    /** {@inheritDoc} */
    @Override
    protected final synchronized void triggerShutdown() {
        queue.shutdown();
        Thread t = executionThread;
        if (t != null) {
            if (isInInterruptableBlock) {
                // We only want to interrupt in interruptable blocks
                t.interrupt();
            }
        }
    }

    /**
     * Sleep until shutdown.
     *
     * @param time the time
     * @param unit the unit
     * @throws InterruptedException the interrupted exception
     */
    protected void sleepUntilShutdown(long time, TimeUnit unit) throws InterruptedException {
        queue.awaitShutdown(time, unit);
    }
}
