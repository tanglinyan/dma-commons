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
package dk.dma.app.management;

import static java.util.Objects.requireNonNull;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.ReflectionException;

import dk.dma.app.management.ManagedAttribute;
import dk.dma.app.management.Managements;

/**
 * An AbstractAttribute is a wrapper for a JMX attribute.
 * 
 * @author Kasper Nielsen
 */
class DefaultManagedAttribute {
    /** The description of the operation. */
    private final String description;

    /** The name of the operation. */
    private final String name;

    /** The getter for this attribute or <code>null</code> if it is write-only. */
    private final Method getter;

    /** The object that this attribute should be invoked on. */
    private final Object obj;

    /** The setter for this attribute or <code>null</code> if it is read-only. */
    private final Method setter;

    /**
     * Creates a new DefaultManagedAttribute.
     * 
     * @param obj
     *            the object that contains the attribute
     * @param reader
     *            the reader method of the attribute or <code>null</code> if it is write-only.
     * @param writer
     *            the writer method of the attribute or <code>null</code> if it is read-only.
     * @param name
     *            the name of the attribute
     * @param description
     *            the description of the attribute
     * @throws NullPointerException
     *             if the specified object, name or description is <code>null</code>. Or if both reader and writer are
     *             <code>null</code>
     */
    DefaultManagedAttribute(Object obj, Method reader, Method writer, String name, String description) {
        if (reader == null && writer == null) {
            throw new NullPointerException("reader and writer cannot both be null");
        }
        this.name = requireNonNull(name, "name is null");
        this.description = requireNonNull(description, "description is null");
        this.obj = requireNonNull(obj, "obj is null");
        this.getter = reader;
        this.setter = writer;
    }

    /**
     * Returns the MBeanAttributeInfo for this attribute.
     * 
     * @return the MBeanAttributeInfo for this attribute
     * @throws IntrospectionException
     *             could not optain the information for this attribute
     */
    MBeanAttributeInfo getInfo() throws IntrospectionException {
        return new MBeanAttributeInfo(name, description, getter, setter);
    }

    /**
     * Returns the value of the attribute.
     * 
     * @return the value of the attribute
     * @throws ReflectionException
     *             could not get the value of the attribute
     */
    Object getValue() throws ReflectionException {
        if (getter == null) {
            throw new IllegalStateException("Attribute is write-only");
        }
        return Managements.invoke(getter, obj, "getter for the attribute " + name, (Object[]) null);
    }

    /**
     * Sets the value of the attribute to specified object.
     * 
     * @param o
     *            the value that the attribute should be set to
     * @throws ReflectionException
     *             could not set the attribute
     */
    void setValue(Object o) throws ReflectionException {
        if (setter == null) {
            throw new IllegalStateException("Attribute is read-only");
        }
        Managements.invoke(setter, obj, "setter for the attribute " + name, o);
    }

    /**
     * Creates a DefaultManagedAttribute from the specified PropertyDescriptor if the {@link ManagedAttribute}
     * annotation is present on the getter or setter.
     * 
     * @param pd
     *            the PropertyDescriptor of the attribute
     * @param obj
     *            the object where the attribute should be read and written to.
     * @return a DefaultManagedAttribute if the ManagedAttribute annotation is present on the getter or setter. Or
     *         <code>null</code> if no annotation is present.
     * @throws IllegalArgumentException
     *             if an attribute has the ManagedAttribute set for both the reader and the writter. Or if it has a
     *             ManagedAttribute set on the reader where isWriteOnly is set to <code>true</code>
     */
    static DefaultManagedAttribute fromPropertyDescriptor(PropertyDescriptor pd, Object obj) {
        ManagedAttribute readAttribute = pd.getReadMethod() == null ? null : pd.getReadMethod().getAnnotation(
                ManagedAttribute.class);
        ManagedAttribute writeAttribute = pd.getWriteMethod() == null ? null : pd.getWriteMethod().getAnnotation(
                ManagedAttribute.class);
        Method writer = null;
        Method reader = null;
        if (readAttribute != null) {
            if (writeAttribute != null) {
                throw new IllegalArgumentException("cannot define ManagedAttribute on both setter and getter for "
                        + pd.getReadMethod());
            } else if (readAttribute.isWriteOnly()) {
                throw new IllegalArgumentException("cannot set writeonly on getter " + pd.getReadMethod());
            }
            reader = pd.getReadMethod();
            writeAttribute = readAttribute;
        } else if (writeAttribute != null) {
            writer = pd.getWriteMethod();
            if (!writeAttribute.isWriteOnly()) {
                reader = pd.getReadMethod();
            }
        } else {
            return null;
        }
        String name = Managements.filterString(obj, writeAttribute.name());
        if (name.equals("")) {
            name = capitalizeFirstLetter(pd.getName());
        }
        return new DefaultManagedAttribute(obj, reader, writer, name,
                Managements.getDescription(writer == null ? reader : writer));
    }

    /**
     * Creates {@link DefaultManagedAttribute}'s for the object and its {@link PropertyDescriptor}'s.
     * 
     * @param pds
     *            the PropertyDescriptor that should be created AbstractManagedAttributes for
     * @param obj
     *            the object that the properties can be set and retrieved from
     * @return a map mapping from the name of the attribute to the AbstractManagedAttribute
     */
    public static Map<String, DefaultManagedAttribute> fromPropertyDescriptors(PropertyDescriptor[] pds, Object obj) {
        Map<String, DefaultManagedAttribute> result = new HashMap<>();
        for (PropertyDescriptor pd : pds) {
            DefaultManagedAttribute a = fromPropertyDescriptor(pd, obj);
            if (a != null) {
                result.put(a.name, a);
            }
        }
        return result;
    }

    /**
     * Returns a new string where the first letter of the specified string is capitalized.
     * 
     * @param str
     *            the string to capitalize
     * @return the string to capitalize
     */
    static String capitalizeFirstLetter(String str) {
        if (str.length() > 0) {
            return replaceCharAt(str, 0, Character.toUpperCase(str.charAt(0)));
        }
        return str;
    }

    static String replaceCharAt(String s, int pos, char c) {
        return s.substring(0, pos) + c + s.substring(pos + 1);
    }

}
