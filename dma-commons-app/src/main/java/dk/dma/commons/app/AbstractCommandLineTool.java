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
package dk.dma.commons.app;

import java.lang.reflect.Field;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;

import dk.dma.commons.util.AnnotationUtil;
import dk.dma.commons.util.ReflectionUtil;

/**
 * 
 * @author Kasper Nielsen
 */
public abstract class AbstractCommandLineTool extends AbstractDmaApplication {

    // We should always have a help
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

        JCommander jc = null;
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
}
