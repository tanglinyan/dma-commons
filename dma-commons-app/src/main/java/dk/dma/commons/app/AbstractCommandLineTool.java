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

import java.lang.reflect.Field;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;

import dk.dma.commons.util.AnnotationUtil;
import dk.dma.commons.util.ReflectionUtil;

/**
 * The type Abstract command line tool.
 *
 * @author Kasper Nielsen
 */
public abstract class AbstractCommandLineTool extends AbstractDmaApplication {

    /**
     * The Help.
     */
// We should always have a help
    @Parameter(names = "-help", help = true, description = "prints this help", hidden = true)
    protected boolean help;

    /**
     * The Jc.
     */
    JCommander jc;

    /**
     * Instantiates a new Abstract command line tool.
     */
    public AbstractCommandLineTool() {
        super();
    }

    /**
     * Instantiates a new Abstract command line tool.
     *
     * @param applicationName the application name
     */
    public AbstractCommandLineTool(String applicationName) {
        super(applicationName);
    }

    /**
     * Execute.
     *
     * @param args the args
     * @throws Exception the exception
     */
    protected void execute(String[] args) throws Exception {
        // For various reasons we need to initialize the function field, because of @ParametersDelegate
        for (Field f : AnnotationUtil.getAnnotatedFields(getClass(), ParametersDelegate.class).keySet()) {
            if (f.getName().endsWith("Instance")) {
                try {
                    f.setAccessible(true);
                    if (f.get(this) == null) {
                        Field ff = ReflectionUtil.getDeclaredField(getClass(), f.getName().replace("Instance", ""));
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

        try {
            jc = new JCommander(this, args);
        } catch (ParameterException e) {
            // show the exception message and print help
            System.out.println(e.getMessage());
            jc = new JCommander(this, new String[] { "-help" });
        }
        if (help) {
            jc.setProgramName(getApplicationName());
            jc.usage();
        } else {
            execute();
        }
    }

    /**
     * Usage.
     */
    protected void usage() {
        jc.usage();
    }
    
}
