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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author Kasper Nielsen
 */
public class CliCommandList {

    private final String name;
    private final Map<String, String> helpText = new LinkedHashMap<>();// keep registration order
    private final Map<String, Command> command = new LinkedHashMap<>();
    static final ThreadLocal<String> CLI_APP_NAME = new ThreadLocal<>();

    public CliCommandList(String name) {
        this.name = requireNonNull(name);
    }

    public final void add(Class<?> main, String name, String description) {
        final Method m;
        try {
            m = main.getMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not find a main(String[] args) method");
        }
        helpText.put(requireNonNull(name), requireNonNull(description));
        command.put(name, new Command() {
            public void execute(String[] args) throws Exception {
                m.invoke(null, (Object) args);
            }
        });
    }

    public final void add(String name, String description, Command cmd) {
        helpText.put(requireNonNull(name), requireNonNull(description));
        command.put(name, requireNonNull(cmd));
    }

    public final void invoke(String[] args) throws Exception {
        // So we have to write some custom code.
        ArrayList<String> list = new ArrayList<>(Arrays.asList(args));
        int cmdIndex = 0;
        for (;; cmdIndex++) {
            if (cmdIndex == list.size()) {
                printError("No command specified");
            } else if (!list.get(cmdIndex).startsWith("-")) {
                break;
            }
        }
        String cmd = list.get(cmdIndex);
        Command c = command.get(cmd);
        if (c == null) {
            printError("Unknown command specified: " + cmd);
        } else {
            list.remove(cmdIndex);
            args = list.toArray(new String[list.size()]);
            CLI_APP_NAME.set(name); // makes sure we use the name of this list, and not the original app name
            try {
                c.execute(args);
            } finally {
                CLI_APP_NAME.remove();
            }
        }
    }

    private void printError(String errorMessage) {
        System.out.println(errorMessage);
        System.out.println("The available " + name + " commands are:");
        int longest = helpText.keySet().iterator().next().length();
        for (String s : helpText.keySet()) {
            longest = Math.max(longest, s.length());
        }
        for (Map.Entry<String, String> e : helpText.entrySet()) {
            System.out.printf("    %-" + longest + "s  %s\n", e.getKey(), e.getValue());
        }
        System.exit(1);
    }

    public abstract static class Command {
        public abstract void execute(String[] args) throws Exception;
    }
}
