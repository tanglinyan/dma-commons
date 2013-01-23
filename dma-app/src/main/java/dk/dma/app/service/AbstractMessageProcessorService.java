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
package dk.dma.app.service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

import dk.dma.app.management.ManagedAttribute;

/**
 * supporting orderly shutdown.
 * 
 * @author Kasper Nielsen
 */
public abstract class AbstractMessageProcessorService<T> extends AbstractExecutionThreadService {

    volatile Thread executionThread;

    private volatile boolean isInInterruptableBlock;

    final ShutdownBlockingQueue<Object> queue;
    final AtomicLong numberProcessed = new AtomicLong();

    protected AbstractMessageProcessorService(int queueSize) {
        queue = new ShutdownBlockingQueue<>(queueSize);
    }

    @SuppressWarnings("unchecked")
    public BlockingQueue<T> getInputQueue() {
        return (BlockingQueue<T>) queue;
    }

    @ManagedAttribute
    public long getNumberOfMessagesProcessed() {
        return numberProcessed.get();
    }

    protected abstract void handleMessages(List<T> messages) throws Exception;

    // All messages have been processed
    protected void onTermination() {

    }

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

    T takeInterruptable(ShutdownBlockingQueue<T> queue) {
        try {
            isInInterruptableBlock = true;
            T t = queue.take();
            synchronized (this) {
                isInInterruptableBlock = false;
                return t;
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
            return null;
        }
    }

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
}
