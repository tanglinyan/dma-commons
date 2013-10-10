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
