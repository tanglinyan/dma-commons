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
package dk.dma.app.management;

import java.lang.reflect.Method;

/**
 * 
 * @author Kasper Nielsen
 */
@SuppressWarnings("unused")
public class PrivateMethods {

    public static final Method PRIVATE_VOID = m("privateVoid");

    public static final Method PRIVATE_VOID_INT = m("privateVoidInt", int.class);

    private void privateVoid() {}

    private void privateVoidInt(int i) {}

    private String getIllegal() {
        return null;
    };

    private void illegal() {};

    private void setIllegal(String parameter) {};

    static Method m(String name, Class<?>... parameterTypes) {
        try {
            return PrivateMethods.class.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }
}
