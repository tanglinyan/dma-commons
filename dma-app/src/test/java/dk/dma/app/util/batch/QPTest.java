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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.google.common.base.Supplier;

import dk.dma.app.util.Processor;

/**
 * 
 * @author Kasper Nielsen
 */
public class QPTest {
    final static List<String> expected = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H");
    final static int COUNT = 1000;
    final static long SUM = COUNT * (COUNT + 1L) / 2L;

    @Test(timeout = 10000)
    public void test1() throws Exception {
        AddableProcessor p = new AddableProcessor();
        try (QueuePumper2<String, Integer> qp = new QueuePumper2<>(988)) {
            qp.pushTo(p);
            processAll(qp);
            qp.shutdownAndAwait(100, TimeUnit.SECONDS);
            assertTrue(qp.q.isEmpty());
        }
        assertEquals(SUM * expected.size(), p.result.get());
    }

    @Test(timeout = 10000)
    public void test2() throws Exception {
        try (QueuePumper2<String, Integer> qp = new QueuePumper2<>(988)) {
            final Queue<String> q = new LinkedList<>();
            qp.pull(new PullProcessor<String, Integer>() {
                /** {@inheritDoc} */
                @Override
                public void start() throws IOException {
                    q.addAll(expected);
                }

                /** {@inheritDoc} */
                @Override
                public void stop() throws IOException {
                    assertTrue(q.isEmpty());
                    q.add("XXXXX");
                }

                public void processResource(String resource, Supplier<Integer> supplier) {
                    assertEquals(q.poll(), resource);
                    long sum = 0;
                    for (Integer r = supplier.get(); r != null; r = supplier.get()) {
                        sum += r.intValue();
                    }
                    assertEquals(SUM, sum);
                }
            });
            processAll(qp);
            qp.shutdownAndAwait(100, TimeUnit.SECONDS);
            assertTrue(qp.q.isEmpty());
            assertEquals("[XXXXX]", q.toString());
        }
    }

    private void processAll(QueuePumper2<String, Integer> qp) throws Exception {
        for (String r : expected) {
            qp.startResource(r);
            for (int j = 1; j <= COUNT; j++) {
                qp.process(j);
            }
            qp.startResource(r);
        }
    }

    static class AddableProcessor extends Processor<Integer> {
        AtomicLong result = new AtomicLong();

        /** {@inheritDoc} */
        @Override
        public void process(Integer t) throws Exception {
            result.addAndGet(t);
        }
    }
}
