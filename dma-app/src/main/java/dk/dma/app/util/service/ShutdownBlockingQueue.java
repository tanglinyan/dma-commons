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

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A blocking queue that can be shutdown and emptied. Ensuring that no more elements will be added after it has been
 * shutdown.
 * 
 * @author Kasper Nielsen
 */
class ShutdownBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {

    CountDownLatch emptied = new CountDownLatch(1);

    volatile boolean isShutdown = false;

    final ReentrantLock putLock = new ReentrantLock();

    final LinkedBlockingQueue<E> q;

    final ReentrantLock takeLock = new ReentrantLock();

    ShutdownBlockingQueue(int size) {
        q = new LinkedBlockingQueue<>(size);
    }

    /**
     * Awaits that both the queue is shutdown and all elements have been taken.
     */
    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return emptied.await(timeout, unit);
    }

    /** {@inheritDoc} */
    @Override
    public int drainTo(Collection<? super E> c) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /** {@inheritDoc} */
    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * True if shutdown has been requested. The queue might have outstanding elements.
     * 
     * @see #isTerminated()
     */
    boolean isShutdown() {
        return isShutdown;
    }

    /**
     * Returns true if the queue has been shutdown and all elements have been taken.
     * 
     * @return
     */
    boolean isTerminated() {
        return emptied.getCount() == 0;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @param packet
     * @return
     */
    public boolean offer(E element) {
        putLock.lock();
        try {
            if (isShutdown) {
                throw new IllegalStateException("Queue has been shutdown");
            }
            return q.offer(element);
        } finally {
            putLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /** {@inheritDoc} */
    public E doPeek() {
        return q.peek();
    }

    public E doPoll() {
        takeLock.lock();
        try {
            E e = q.poll();
            if (isShutdown && e == null) {
                emptied.countDown();
            }
            return e;
        } finally {
            takeLock.unlock();
        }
    }

    public E doPoll(long timeout, TimeUnit unit) throws InterruptedException {
        takeLock.lock();
        try {

            E e = q.poll(timeout, unit);
            if (isShutdown && e == null) {
                emptied.countDown();
            }
            return e;
        } finally {
            takeLock.unlock();
        }
    }

    public void put(E element) throws InterruptedException {
        putLock.lock();
        try {
            if (isShutdown) {
                throw new IllegalStateException("Queue has been shutdown");
            }
            q.put(element);
        } finally {
            putLock.unlock();
        }
    }

    public int remainingCapacity() {
        return q.remainingCapacity();
    }

    void shutdown() {
        putLock.lock();
        try {
            takeLock.lock();
            try {
                isShutdown = true;
            } finally {
                takeLock.unlock();
            }
        } finally {
            putLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return q.size();
    }

    public E doTake() throws InterruptedException {
        takeLock.lock();
        try {
            if (isShutdown) {
                E e = q.poll();
                if (e == null) {
                    emptied.countDown();
                }
                return e;
            }
            return q.take();
        } finally {
            takeLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public E poll() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public E peek() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public E take() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}
