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
package dk.dma.app.application;

import com.google.common.util.concurrent.Service;
import com.google.inject.Injector;

/**
 * 
 * @author Kasper Nielsen
 */
public abstract class AbstractDaemon extends AbstractCommandLineTool {

    // Like a command tools, but keeps going and has a shutdown hook

    /** {@inheritDoc} */
    @Override
    protected final void run(Injector injector) throws Exception {
        runDaemon(injector);
        for (Service s : services) {
            awaitServiceStopped(s);
        }
        // Await on Ctrl-C, or all service exited
    }

    protected abstract void runDaemon(Injector injector) throws Exception;

    /** Creates a new AbstractDaemon */
    public AbstractDaemon() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdown();
            }
        });
    }

    /**
     * @param applicationName
     *            the name of the application
     */
    public AbstractDaemon(String applicationName) {
        super(applicationName);
    }

    // Install shutdown hooks
    protected void externalShutdown() {};
}
