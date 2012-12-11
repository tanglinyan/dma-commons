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
package dk.dma.app.util.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

import dk.dma.app.util.concurrent.ShutdownBlockingQueue;

/**
 * A service where we do not want to be interrupted while doing IO when shutdown. This is important for example when we
 * are closing a file. Really much more complicated than I like.
 * 
 * @author Kasper Nielsen
 */
public abstract class IOInterruptableExecuteThreadService extends AbstractExecutionThreadService {

    private volatile Thread executionThread;

    private volatile boolean isInInterruptableBlock;

    /** {@inheritDoc} */
    @Override
    protected final void run() throws Exception {
        executionThread = Thread.currentThread();
        run0();
    }

    protected abstract void run0() throws Exception;

    protected <T> T pollInterruptable(ShutdownBlockingQueue<T> queue, long timeout, TimeUnit unit) {
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

    protected <T> T takeInterruptable(ShutdownBlockingQueue<T> queue) {
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

    protected <T> T takeInterruptable(BlockingQueue<T> queue) {
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
    protected synchronized void triggerShutdown() {
        Thread t = executionThread;
        if (t != null) {
            if (isInInterruptableBlock) {
                // We only want to interrupt in interruptable blocks
                t.interrupt();
            }
        }
    }
}
