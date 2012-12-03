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
package dk.dma.management;

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
            return DefaultMBean.createFrom(o, mr);
        }
        return null;
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
