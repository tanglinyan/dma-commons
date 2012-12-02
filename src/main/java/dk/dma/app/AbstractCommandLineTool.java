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

import java.lang.reflect.Field;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import dk.dma.app.util.AnnotationUtils;

/**
 * 
 * @author Kasper Nielsen
 */
public abstract class AbstractCommandLineTool extends AbstractDmaApplication {

    @Parameter(names = "-help", help = true, description = "prints this help", hidden = true)
    protected boolean help;

    public AbstractCommandLineTool() {
        super();
    }

    /**
     * @param applicationName
     */
    public AbstractCommandLineTool(String applicationName) {
        super(applicationName);
    }

    /**
     * @param args
     */
    protected void execute(String[] args) throws Exception {
        // For various reasons we need to initialize the function field, because of @ParametersDelegate
        for (Field f : AnnotationUtils.getAnnotatedFields(getClass(), ParametersDelegate.class).keySet()) {
            if (f.getName().endsWith("Instance")) {
                try {
                    f.setAccessible(true);
                    if (f.get(this) == null) {
                        Field ff = getClass().getDeclaredField(f.getName().replace("Instance", ""));
                        ff.setAccessible(true);
                        String className = (String) ff.get(this);
                        for (int i = 0; i < args.length; i++) {
                            if (args[i] == "-" + ff.getName()) {
                                className = args[i + 1];
                            }
                        }
                        Class<?> exportClass = Class.forName(className);
                        f.set(this, exportClass.newInstance());
                    }

                } catch (NoSuchFieldException ignore) {}
            }
        }
        JCommander jc = new JCommander(this, args);
        if (help) {
            jc.setProgramName(getApplicationName());
            jc.usage();
            return;
        }
        start();

    }
}
