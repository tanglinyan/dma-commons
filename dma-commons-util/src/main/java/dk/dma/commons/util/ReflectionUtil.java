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

import java.lang.reflect.Field;

/**
 * 
 * @author Kasper Nielsen
 */
public class ReflectionUtil {

    public static Field getDeclaredField(Class<?> type, String name) throws NoSuchFieldException {
        while (type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    return field;
                }
            }
            type = type.getSuperclass();
        }
        throw new NoSuchFieldException("Could not find field with the name " + name);
    }
}
