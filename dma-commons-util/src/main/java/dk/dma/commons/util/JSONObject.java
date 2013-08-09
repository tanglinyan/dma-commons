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
package dk.dma.commons.util;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Kasper Nielsen
 */
public final class JSONObject {

    /** The Unix line separator. */
    public static final String UNIX_LINE_SEPARATOR = "\n";

    private final List<Map.Entry<String, Object>> elements = new ArrayList<>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void add(String name, Object o) {
        elements.add(new AbstractMap.SimpleImmutableEntry(requireNonNull(name), o));
    }

    public JSONObject addElement(String name, Object value) {
        add(name, value);
        return this;
    }

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

    public JSONObject newChild(String name) {
        JSONObject o2 = new JSONObject();
        add(name, o2);
        return o2;
    }

    public static JSONObject single(String name, Object value) {
        return new JSONObject().addElement(name, value);
    }

    public static JSONObject singleList(String name, Object... value) {
        return new JSONObject().addElement(name, Arrays.asList(value));
    }

    public static void main(String[] args) {
        singleList("sources", "AisD", "Helcom").toString();
        System.out.println(singleList("sources", "AisD", "Helcom"));
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
     * @param sb
     *            the string builder to add to
     * @param count
     *            the number of spaces to add
     * @return the specified string builder
     */
    public static StringBuilder spaces(StringBuilder sb, int count) {
        for (int i = 0; i < count; i++) {
            sb.append(' ');
        }
        return sb;
    }
}
