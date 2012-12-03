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
package dk.dma.app;

import static java.util.Objects.requireNonNull;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;

import dk.dma.management.Managements;

/**
 * 
 * @author Kasper Nielsen
 */
public abstract class AbstractDmaApplication {

    /** The name of the application. */
    final String applicationName;

    volatile Injector injector;

    private final List<Module> modules = new ArrayList<>();

    AbstractDmaApplication() {
        applicationName = getClass().getSimpleName();
    }

    /**
     * Clients should use one of {@link AbstractCommandLineTool}, {@link AbstractSwingApplication} or
     * {@link AbstractWebApplication}.
     * 
     * @param applicationName
     *            the name of the application
     */
    AbstractDmaApplication(String applicationName) {
        this.applicationName = requireNonNull(applicationName);
    }

    protected synchronized void addModule(Module module) {
        modules.add(requireNonNull(module));
    }

    protected void addPropertyFile(String name) {}

    // A required properties
    protected void addPropertyFileOnClasspath(String name) {};

    protected void configure() {}

    private void defaultModule() {
        addModule(new AbstractModule() {

            @Override
            protected void configure() {
                Names.bindProperties(binder(), Collections.singletonMap("app.name", applicationName));
            }
        });
    }

    public final String getApplicationName() {
        return applicationName;
    }

    protected abstract void run(Injector injector) throws Exception;

    protected void start() throws Exception {
        defaultModule();
        Injector i = Guice.createInjector(modules);
        // Management
        tryManage(this);
        run(i);
    }

    private void tryManage(Object o) throws Exception {
        DynamicMBean mbean = Managements.tryCreate(this);
        if (mbean != null) {
            MBeanServer mb = ManagementFactory.getPlatformMBeanServer();
            Class<?> c = o.getClass();
            ObjectName objectName = new ObjectName(c.getPackage().getName() + ":type=" + c.getSimpleName());
            mb.registerMBean(mbean, objectName);
        }
    }
}
