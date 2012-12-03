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
package dk.dma.app.util.batch;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * 
 * @author Kasper Nielsen
 */
public class QueuePumper<T> implements Callable<Void>, BatchProcessor<T> {
    private final static Object SHUTDOWN = new Object();

    private final Processor<T> batchedProducer;

    private final BlockingQueue<T> consumer;

    private volatile Throwable exception;

    private volatile BlockingQueue<T> producer;

    public QueuePumper(Processor<T> batchedProducer, int bufferSize) {
        this.batchedProducer = requireNonNull(batchedProducer);
        consumer = producer = new ArrayBlockingQueue<>(bufferSize);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public synchronized void finished(Throwable exceptionalFinished) {
        this.exception = exceptionalFinished;
        BlockingQueue bq = this.producer;
        if (bq != null) {
            bq.add(SHUTDOWN);
        }
        producer = null;
    }

    /** {@inheritDoc} */
    @Override
    public void process(T message) throws Exception {
        BlockingQueue<T> producer = this.producer;
        if (producer != null) {
            producer.put(message);
        } else {
            throw new IllegalStateException("Consumer side has been shutdown", exception);
        }
    }

}
