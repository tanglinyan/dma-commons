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
package dk.dma.commons.util.filtering;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * A single thread cleaning filters.
 *
 * @author Kasper Nielsen
 */
class Cleaner extends Thread {

    /**
     * The Set.
     */
    static final Set<Runnable> SET = Collections.newSetFromMap(new WeakHashMap<Runnable, Boolean>());

    private static ScheduledExecutorService ses;

    /**
     * Add.
     *
     * @param runnable the runnable
     */
    static synchronized void add(Runnable runnable) {
        SET.add(requireNonNull(runnable));

        if (ses == null) {// lazy start thread
            ses = Executors.newScheduledThreadPool(1, new ThreadFactory() {

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setName("Filter cleaning thread [dk.dma.app.util.filtering.Cleaner]");
                    return t;
                }
            });
            ses.schedule(new Runnable() {
                @Override
                public void run() {
                    for (Runnable r : SET) {
                        if (r != null) {
                            r.run();
                        }
                    }
                }
            }, 1, TimeUnit.SECONDS);
        }
    }
}
