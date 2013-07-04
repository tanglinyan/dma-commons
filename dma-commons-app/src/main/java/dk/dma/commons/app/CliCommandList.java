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
package dk.dma.commons.app;

import static java.util.Objects.requireNonNull;

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

    public CliCommandList(String name) {
        this.name = requireNonNull(name);
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
            c.execute(args);
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

    public static abstract class Command {
        public abstract void execute(String[] args) throws Exception;
    }
}
