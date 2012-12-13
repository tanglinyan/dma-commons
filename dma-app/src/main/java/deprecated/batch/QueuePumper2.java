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
package deprecated.batch;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Supplier;

import dk.dma.app.util.function.Processor;

/**
 * 
 * @author Kasper Nielsen
 */
public class QueuePumper2<T, E> extends BatchProcessor<T, E> implements Closeable {
    final static Object SHUTDOWN = new Object();

    volatile ExecutorService outputSideExecuter;

    final BlockingQueue<Object> q;
    final AtomicReference<State> state = new AtomicReference<>();

    volatile Throwable throwable;

    public QueuePumper2() {
        this(10000);
    }

    public QueuePumper2(int bufferSize) {
        q = new LinkedBlockingQueue<>(bufferSize);// concurrent consume and produce
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {}

    private void enqueue(Object o) throws InterruptedException {
        q.put(o);// Check exceptions
    }

    /** {@inheritDoc} */
    @Override
    public void process(E t) throws Exception {
        enqueue(t);
    }

    public void pull(final PullProcessor<T, E> proc) {
        run(new Callable<Void>() {
            @SuppressWarnings("unchecked")
            @Override
            public Void call() throws Exception {
                boolean isFirst = true;
                for (;;) {
                    Object o = q.take();
                    if (isFirst) {
                        proc.start();
                        isFirst = false;
                    }
                    if (o == SHUTDOWN) {
                        state.set(State.TERMINATED);
                        proc.stop();
                        return null;
                    }
                    StartStop ss = (StartStop) o;
                    proc.processResource((T) ss.resource, new Sup());
                }
            }
        });
    }

    class Sup implements Supplier<E> {
        boolean done;

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public E get() {
            try {
                if (done) {
                    return null;
                }

                Object o = q.take();
                if (o instanceof StartStop) {
                    done = true;
                    return null;
                } else {
                    return (E) o;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void pushTo(final BatchProcessor<T, E> side) {
        run(new Callable<Void>() {
            @SuppressWarnings("unchecked")
            @Override
            public Void call() throws Exception {
                for (;;) {
                    Object o = q.take();
                    if (o == SHUTDOWN) {
                        state.set(State.TERMINATED);
                        return null;
                    } else if (o instanceof StartStop) {
                        StartStop ss = (StartStop) o;
                        if (ss.isStart) {
                            side.startResource((T) ss.resource);
                        } else {
                            side.stopResource((T) ss.resource, null);
                        }
                    } else {
                        try {
                            side.process((E) o);
                        } catch (Exception | Error e) {
                            throwable = e;
                            state.set(State.OUTPUTSIDE_FAILED);
                            q.clear();
                            throw e;
                        }
                    }
                }
            }
        });
    }

    public void pushTo(final Processor<E> side) {
        pushTo(new BatchProcessor<T, E>() {
            @Override
            public void process(E t) throws Exception {
                side.process(t);
            }
        });
    }

    private synchronized void run(Callable<Void> call) {
        if (outputSideExecuter != null) {
            throw new IllegalStateException("push or pull have already been called");
        }
        outputSideExecuter = Executors.newSingleThreadExecutor();
        outputSideExecuter.submit(call);
    }

    public void shutdownAndAwait(long timeout, TimeUnit unit) throws InterruptedException {
        synchronized (this) {
            if (outputSideExecuter == null) {
                throw new IllegalStateException("push or pull have never been called");
            }
        }
        enqueue(SHUTDOWN);
        outputSideExecuter.shutdown();
        outputSideExecuter.awaitTermination(timeout, unit);
    }

    /** {@inheritDoc} */
    @Override
    public void startResource(T resource) throws Exception {
        enqueue(new StartStop(true, resource));
    }

    /** {@inheritDoc} */
    @Override
    public void stopResource(T resource, Throwable t) throws Exception {
        enqueue(new StartStop(false, resource));
    }

    static class StartStop {
        final boolean isStart;

        final Object resource;

        StartStop(boolean isStart, Object resource) {
            this.isStart = isStart;
            this.resource = resource;
        }
    }

    static enum State {
        NOT_STARTED, INPUT_SIDE_STARTED, BOTH_STARTED, INPUTSIDE_FAILED, OUTPUTSIDE_FAILED, SHUTDOWN, TERMINATED;
    }

}
