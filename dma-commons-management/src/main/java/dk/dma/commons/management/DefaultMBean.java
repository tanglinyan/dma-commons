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
package dk.dma.commons.management;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * The DynamicMBean that is used to expose this group.
 * 
 * @author Kasper Nielsen
 */
class DefaultMBean implements DynamicMBean {

    /** A map of all attributes. */
    private final Map<String, DefaultManagedAttribute> attributes;

    /** The description of this MBean. */
    private final String description;

    /** The name of this MBean. */
    private final String name;

    /** A map of all operations. */
    private final Map<OperationKey, DefaultManagedOperation> ops;

    /**
     * Creates a {@link DynamicMBean} from the specified parameters.
     * 
     * @param name
     *            the name of the MBean
     * @param description
     *            the description of the MBean
     * @param attributes
     *            a Map of all attributes
     * @param ops
     *            a Map of all operations
     */
    DefaultMBean(String name, String description, Map<String, DefaultManagedAttribute> attributes,
            Map<OperationKey, DefaultManagedOperation> ops) {
        this.name = requireNonNull(name, "name is null");
        this.description = requireNonNull(description, "description is null");
        this.attributes = requireNonNull(attributes, "attributes is null");
        this.ops = requireNonNull(ops, "ops is null");
    }

    /**
     * Finds and returns the an attribute with the specified name, or throws an Exception.
     * 
     * @param attribute
     *            the name of the attribute
     * @return the attribute with the specified name
     * @throws AttributeNotFoundException
     *             if no such attribute existed
     */
    private DefaultManagedAttribute findAttribute(String attribute) throws AttributeNotFoundException {
        DefaultManagedAttribute att = attributes.get(attribute);
        if (att == null) {
            throw new AttributeNotFoundException("Attribute " + attribute + " could not be found");
        }
        return att;
    }

    /** {@inheritDoc} */
    public Object getAttribute(String attribute) throws AttributeNotFoundException, ReflectionException {
        return findAttribute(attribute).getValue();
    }

    /** {@inheritDoc} */
    public AttributeList getAttributes(String[] attributes) {
        AttributeList result = new AttributeList(attributes.length);
        for (String attrName : attributes) {
            try {
                Object attrValue = getAttribute(attrName);
                result.add(new Attribute(attrName, attrValue));
            } catch (Exception ok) { /* Attribute is not included in returned list, per spec */}
        }
        return result;
    }

    /** {@inheritDoc} */
    public MBeanInfo getMBeanInfo() {
        ArrayList<MBeanAttributeInfo> infos = new ArrayList<>();
        for (DefaultManagedAttribute aa : attributes.values()) {
            try {
                infos.add(aa.getInfo());
            } catch (IntrospectionException e) {
                // /CLOVER:OFF
                throw new IllegalStateException(e);// don't test
                // /CLOVER:ON
            }
        }
        ArrayList<MBeanOperationInfo> operations = new ArrayList<>();
        for (DefaultManagedOperation op : ops.values()) {
            operations.add(op.getInfo());
        }

        return new MBeanInfo(name, description, infos.toArray(new MBeanAttributeInfo[0]), null,
                operations.toArray(new MBeanOperationInfo[0]), null);
    }

    /** {@inheritDoc} */
    public Object invoke(String actionName, Object[] params, String[] signature) throws ReflectionException {
        DefaultManagedOperation o = ops
                .get(new OperationKey(actionName, signature == null ? new String[0] : signature));
        if (o == null) {
            throw new IllegalArgumentException("Unknown method " + actionName + " [ signature = "
                    + Arrays.toString(signature) + "]");
        }
        return o.invoke(params);
    }

    /** {@inheritDoc} */
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, ReflectionException {
        findAttribute(attribute.getName()).setValue(attribute.getValue());
    }

    /** {@inheritDoc} */
    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList result = new AttributeList(attributes.size());
        for (Attribute attr : attributes.asList()) {
            try {
                setAttribute(attr);
                result.add(new Attribute(attr.getName(), attr.getValue()));
            } catch (Exception ok) { /* Attribute is not included in returned list, per spec */}
        }
        return result;
    }

    public static DefaultMBean createFrom(Object o, String name) {
        Class<?> c = o.getClass();
        BeanInfo bi;
        try {
            bi = Introspector.getBeanInfo(c);
        } catch (java.beans.IntrospectionException e) {
            // /CLOVER:OFF
            throw new IllegalArgumentException(e); // cannot happen
            // /CLOVER:ON
        }
        return new DefaultMBean(name, Managements.getDescription(c), DefaultManagedAttribute.fromPropertyDescriptors(
                bi.getPropertyDescriptors(), o), DefaultManagedOperation.getOperationFromMethods(o, c.getMethods()));
    }
}
