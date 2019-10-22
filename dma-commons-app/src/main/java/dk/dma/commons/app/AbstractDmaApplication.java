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

import static java.util.Objects.requireNonNull;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;

import dk.dma.commons.management.Managements;

/**
 * The type Abstract dma application.
 *
 * @author Kasper Nielsen
 */
public abstract class AbstractDmaApplication {

    /**
     * The name of the application.
     */
    final String applicationName;

    /**
     * The Injector.
     */
    volatile Injector injector;

    private final List<Module> modules = new ArrayList<>();

    /**
     * The Services.
     */
    final CopyOnWriteArrayList<Service> services = new CopyOnWriteArrayList<>();

    /**
     * The Log.
     */
    static final Logger LOG = LoggerFactory.getLogger(AbstractDmaApplication.class);

    /**
     * The Is shutdown.
     */
    final CountDownLatch isShutdown = new CountDownLatch(1);

    /**
     * Instantiates a new Abstract dma application.
     */
    AbstractDmaApplication() {
        String cliName = CliCommandList.CLI_APP_NAME.get();
        applicationName = cliName == null ? getClass().getSimpleName() : cliName;
    }

    /**
     * Clients should use one of {@link AbstractCommandLineTool}, {@link AbstractSwingApplication} or
     * {@link AbstractWebApplication}.
     *
     * @param applicationName the name of the application
     */
    AbstractDmaApplication(String applicationName) {
        this.applicationName = requireNonNull(applicationName);
    }

    /**
     * Add module.
     *
     * @param module the module
     */
    protected final void addModule(Module module) {
        modules.add(requireNonNull(module));
    }

    /**
     * Add property file.
     *
     * @param name the name
     */
    protected void addPropertyFile(String name) {}

    /**
     * Add property file on classpath.
     *
     * @param name the name
     */
// A required properties
    protected void addPropertyFileOnClasspath(String name) {};

    /**
     * Configure.
     */
    protected void configure() {}

    private void defaultModule() {
        addModule(new AbstractModule() {

            @Override
            protected void configure() {
                Names.bindProperties(binder(), Collections.singletonMap("app.name", applicationName));
            }
        });
    }

    /**
     * Gets application name.
     *
     * @return the application name
     */
    public final String getApplicationName() {
        return applicationName;
    }

    /**
     * Start t.
     *
     * @param <T>     the type parameter
     * @param service the service
     * @return the t
     */
    protected <T extends Service> T start(T service) {
        services.add(requireNonNull(service));
        service.startAsync();
        service.awaitRunning();
        return service;
    }

    /**
     * Run.
     *
     * @param injector the injector
     * @throws Exception the exception
     */
    protected abstract void run(Injector injector) throws Exception;

    /**
     * Sleep unless shutdown.
     *
     * @param timeout the timeout
     * @param unit    the unit
     * @throws InterruptedException the interrupted exception
     */
    public void sleepUnlessShutdown(long timeout, TimeUnit unit) throws InterruptedException {
        isShutdown.await(timeout, unit);
    }

    /**
     * Execute.
     *
     * @throws Exception the exception
     */
    void execute() throws Exception {
        defaultModule();
        configure();
        Injector i = Guice.createInjector(modules);
        // Management
        tryManage(this);
        try {
            run(i);
        } finally {
            // Shutdown in reverse order
            Collections.reverse(services);
            for (Service s : services) {
                s.stopAsync();
                s.awaitTerminated();
            }
        }
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        LOG.info("Shutting down all services");
        // shutdown services in reverse order
        List<Service> list = new ArrayList<>(services);
        Collections.reverse(list);
        for (Service s : list) {
            LOG.info("Trying to shut down " + s.getClass().getName());
            s.stopAsync();
            s.awaitTerminated();
            LOG.info("Succeeded in shutting down " + s.getClass().getName());
        }
        LOG.info("All services was succesfully shutdown");
    }

    /**
     * Await service stopped.
     *
     * @param s the s
     * @throws InterruptedException the interrupted exception
     */
    void awaitServiceStopped(Service s) throws InterruptedException {
        State st = s.state();
        CountDownLatch cdl = null;
        while (st == State.RUNNING || st == State.NEW) {
            if (cdl != null) {
                cdl.await();
            }
            final CountDownLatch c = cdl = new CountDownLatch(1);
            s.addListener(new Service.Listener() {
                public void terminated(State from) {
                    c.countDown();
                }

                @Override
                public void stopping(State from) {
                    c.countDown();
                }

                @Override
                public void starting() {
                    c.countDown();
                }

                @Override
                public void running() {
                    c.countDown();
                }

                @Override
                public void failed(State from, Throwable failure) {
                    c.countDown();
                }
            }, MoreExecutors.newDirectExecutorService());
        }
    }

    /**
     * Try manage.
     *
     * @param o the o
     * @throws Exception the exception
     */
    protected void tryManage(Object o) throws Exception {
        DynamicMBean mbean = Managements.tryCreate(this);
        if (mbean != null) {
            MBeanServer mb = ManagementFactory.getPlatformMBeanServer();
            Class<?> c = o.getClass();
            ObjectName objectName = new ObjectName(c.getPackage().getName() + ":type=" + c.getSimpleName());
            mb.registerMBean(mbean, objectName);
        }
    }

    private final Management management = new Management();

    /**
     * With management management.
     *
     * @return the management
     */
    public Management withManagement() {
        return management;
    }

    /**
     * The type Management.
     */
    public static class Management {
        /**
         * The D.
         */
        volatile String d;

    }
}
