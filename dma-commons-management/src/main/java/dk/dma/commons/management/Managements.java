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
package dk.dma.commons.management;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.management.DynamicMBean;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

/**
 * Various utility functions.
 * 
 * @author Kasper Nielsen
 */
public class Managements {

    public static DynamicMBean tryCreate(Object o) {
        ManagedResource mr = o.getClass().getAnnotation(ManagedResource.class);
        if (mr != null) {
            return tryCreate(o, getName(o.getClass(), mr));
        }
        return null;
    }

    public static DynamicMBean tryCreate(Object o, String name) {
        return DefaultMBean.createFrom(o, name);
    }

    /**
     * Filters the specified string, currently does nothing.
     * 
     * @param o
     *            the object to filter
     * @param str
     *            the string to filter
     * @return the specified string
     */
    static String filterString(Object o, String str) {
        // if (o instanceof Named) {
        // Named n = (Named) o;
        // str = str.replace("$name", n.getName());
        // // System.out.println(n.getName());
        // }
        // if (o instanceof Described) {
        // Described n = (Described) o;
        // str = str.replace("$description", n.getDescription());
        // }
        return str;
    }

    static String getName(Class<?> o, ManagedResource mr) {
        String name = mr.value();
        return name.equals("") ? o.getSimpleName() : name;
    }

    static String getDescription(Class<?> o) {
        Description desc = o.getAnnotation(Description.class);
        return Managements.filterString(o, desc == null ? "" : desc.value());
    }

    static String getDescription(AccessibleObject o) {
        Description desc = o.getAnnotation(Description.class);
        return Managements.filterString(o, desc == null ? "" : desc.value());
    }

    static Object invoke(Method m, Object o, String msg, Object... parameters) throws ReflectionException {
        try {
            return m.invoke(o, parameters);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof Error) {
                throw (Error) t;
            }
            throw new ReflectionException((Exception) t, "Exception thrown while invoking  " + msg);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e, "Illegal access while trying to invoke " + msg);
        }
    }

    /**
     * Returns information about the parameters of the specified method.
     * 
     * @param method
     *            the method to return parameter info about
     * @return information about the parameters of the specified method
     */
    static MBeanParameterInfo[] methodSignature(Method method) {
        Class<?>[] classes = method.getParameterTypes();
        MBeanParameterInfo[] params = new MBeanParameterInfo[classes.length];

        for (int i = 0; i < classes.length; i++) {
            String parameterName = "p" + (i + 1);
            params[i] = new MBeanParameterInfo(parameterName, classes[i].getName(), "");
        }

        return params;
    }
}
