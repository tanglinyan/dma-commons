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

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dk.dma.app.util.function.Processor;

/**
 * 
 * @author Kasper Nielsen
 */
public class QueuePumper<T> {
    final static Object SHUTDOWN = new Object();

    final Processor<T> batchedProducer;

    final BlockingQueue<T> consumer;

    volatile Throwable exception;

    volatile BlockingQueue<T> producer;

    final ExecutorService es = Executors.newSingleThreadExecutor();

    volatile boolean isStarted;

    public QueuePumper(Processor<T> batchedProducer, int bufferSize) {
        this.batchedProducer = requireNonNull(batchedProducer);
        consumer = producer = new ArrayBlockingQueue<>(bufferSize);
    }

    public synchronized void start() {
        if (isStarted) {
            throw new IllegalStateException();
        }
        isStarted = true;
        es.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                for (;;) {

                    T take = consumer.take();
                    if (take == SHUTDOWN) {
                        return null;
                    }
                    try {
                        batchedProducer.process(take);
                    } catch (Exception | Error e) {
                        consumer.clear();// Clear messages
                        producer = null;
                        exception = e;
                        throw e;
                    }
                }
            }
        });
    }

    public boolean shutdownAndAwaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        es.shutdown();
        return es.awaitTermination(timeout, unit);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public synchronized void finished(Throwable exceptionalFinished) {
        this.exception = exceptionalFinished;
        BlockingQueue bq = this.producer;
        if (bq != null) {
            bq.add(SHUTDOWN);
        }
        producer = null;
    }

    /** {@inheritDoc} */
    public void process(T message) throws Exception {
        BlockingQueue<T> producer = this.producer;
        if (producer != null) {
            producer.put(message);
        } else {
            throw new IllegalStateException("Consumer side has been shutdown", exception);
        }
    }

}
