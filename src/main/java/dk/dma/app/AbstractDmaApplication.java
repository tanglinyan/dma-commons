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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;

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

    protected synchronized void start() throws Exception {
        defaultModule();
        run(Guice.createInjector(modules));
    }
}
