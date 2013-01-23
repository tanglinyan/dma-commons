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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Kasper Nielsen
 */
public class AnnotationUtil {

    public static <T extends Annotation> Map<Field, T> getAnnotatedFields(Class<?> type, Class<T> annotation) {
        final Map<Field, T> fields = new HashMap<>();
        while (type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotation)) {
                    fields.put(field, field.getAnnotation(annotation));
                }
            }
            type = type.getSuperclass();
        }
        return fields;
    }

    public static <T extends Annotation> Map<Method, T> getAnnotatedMethods(Class<?> type, Class<T> annotation) {
        final Map<Method, T> methods = new HashMap<>();
        while (type != Object.class) {
            for (Method method : type.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    methods.put(method, method.getAnnotation(annotation));
                }
            }
            type = type.getSuperclass();
        }
        return methods;
    }
}
