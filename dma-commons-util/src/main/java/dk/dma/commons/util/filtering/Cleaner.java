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

    static final Set<Runnable> SET = Collections.newSetFromMap(new WeakHashMap<Runnable, Boolean>());

    private static ScheduledExecutorService ses;

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
