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
package dk.dma.commons.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Service;
import com.google.inject.Injector;

/**
 * 
 * @author Kasper Nielsen
 */
public abstract class AbstractDaemon extends AbstractCommandLineTool {

    static final Logger LOG = LoggerFactory.getLogger(AbstractDaemon.class);

    // Like a command tools, but keeps going and has a shutdown hook

    /** Creates a new AbstractDaemon */
    public AbstractDaemon() {}

    /**
     * @param applicationName
     *            the name of the application
     */
    public AbstractDaemon(String applicationName) {
        super(applicationName);

    }

    private void installShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                LOG.info("Shutdown request received");
                preShutdown();
                isShutdown.countDown();
                shutdown();
                postShutdown();
                LOG.info("Shutdown finished");
            }
        });
    }

    protected void postShutdown() {}

    // Install shutdown hooks
    protected void preShutdown() {}

    /** {@inheritDoc} */
    @Override
    protected final void run(Injector injector) throws Exception {
        installShutdownHook();
        runDaemon(injector);
        for (Service s : services) {
            awaitServiceStopped(s);
        }
        // Await on Ctrl-C, or all service exited
    }

    protected abstract void runDaemon(Injector injector) throws Exception;;
}
