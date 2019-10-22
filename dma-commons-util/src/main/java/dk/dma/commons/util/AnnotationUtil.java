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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Annotation util.
 *
 * @author Kasper Nielsen
 */
public class AnnotationUtil {

    /**
     * Gets annotated fields.
     *
     * @param <T>        the type parameter
     * @param type       the type
     * @param annotation the annotation
     * @return the annotated fields
     */
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

    /**
     * Gets annotated methods.
     *
     * @param <T>        the type parameter
     * @param type       the type
     * @param annotation the annotation
     * @return the annotated methods
     */
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
