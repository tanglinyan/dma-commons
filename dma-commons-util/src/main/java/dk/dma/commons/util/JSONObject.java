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
package dk.dma.commons.util;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The type Json object.
 *
 * @author Kasper Nielsen
 */
public final class JSONObject {

    /**
     * The Unix line separator.
     */
    public static final String UNIX_LINE_SEPARATOR = "\n";

    private final List<Map.Entry<String, Object>> elements = new ArrayList<>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void add(String name, Object o) {
        elements.add(new AbstractMap.SimpleImmutableEntry(requireNonNull(name), o));
    }

    /**
     * Add element json object.
     *
     * @param name  the name
     * @param value the value
     * @return the json object
     */
    public JSONObject addElement(String name, Object value) {
        add(name, value);
        return this;
    }

    /**
     * Add list json object.
     *
     * @param name   the name
     * @param values the values
     * @return the json object
     */
    public JSONObject addList(String name, Object... values) {
        add(name, Arrays.asList(values));
        return this;
    }

    private void addValue(StringBuilder sb, Object stringOrNumber) {
        if (stringOrNumber instanceof Number) {
            sb.append(stringOrNumber);
        } else {
            sb.append('"').append(stringOrNumber).append("\"");
        }
    }

    /**
     * New child json object.
     *
     * @param name the name
     * @return the json object
     */
    public JSONObject newChild(String name) {
        JSONObject o2 = new JSONObject();
        add(name, o2);
        return o2;
    }

    /**
     * Single json object.
     *
     * @param name  the name
     * @param value the value
     * @return the json object
     */
    public static JSONObject single(String name, Object value) {
        return new JSONObject().addElement(name, value);
    }

    /**
     * Single json object.
     *
     * @param name   the name
     * @param value  the value
     * @param name1  the name 1
     * @param value1 the value 1
     * @return the json object
     */
    public static JSONObject single(String name, Object value, String name1, Object value1) {
        return new JSONObject().addElement(name, value).addElement(name1, value1);
    }

    /**
     * Single json object.
     *
     * @param name   the name
     * @param value  the value
     * @param name1  the name 1
     * @param value1 the value 1
     * @param name2  the name 2
     * @param value2 the value 2
     * @return the json object
     */
    public static JSONObject single(String name, Object value, String name1, Object value1, String name2, Object value2) {
        return new JSONObject().addElement(name, value).addElement(name1, value1).addElement(name2, value2);
    }

    /**
     * Single list json object.
     *
     * @param name  the name
     * @param value the value
     * @return the json object
     */
    public static JSONObject singleList(String name, Object... value) {
        return new JSONObject().addElement(name, Arrays.asList(value));
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        singleList("sources", "AisD", "Helcom").toString();
        System.out.println(single("sources", "AisD", "23234", 1322));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(UNIX_LINE_SEPARATOR);
        toString(sb, 1);
        sb.append("}\n");
        return sb.toString();
    }

    private void toString(StringBuilder sb, int indent) {
        for (Map.Entry<String, Object> e : elements) {
            spaces(sb, indent * 2);
            sb.append('"').append(e.getKey()).append("\": ");
            Object o = e.getValue();
            if (o instanceof List) {
                List<?> l = (List<?>) o;
                sb.append("[");
                for (int i = 0; i < l.size(); i++) {
                    if (i != 0) {
                        sb.append(", ");
                    }
                    addValue(sb, l.get(i));
                }
                sb.append("]");
            } else if (o instanceof JSONObject) {
                JSONObject jo = (JSONObject) o;
                jo.toString(sb, indent + 1);
            } else {
                addValue(sb, o);
            }
            sb.append("\n");
        }
    }

    /**
     * Adds the specified count of spaces to the specified string builder.
     *
     * @param sb    the string builder to add to
     * @param count the number of spaces to add
     * @return the specified string builder
     */
    public static StringBuilder spaces(StringBuilder sb, int count) {
        for (int i = 0; i < count; i++) {
            sb.append(' ');
        }
        return sb;
    }
}
