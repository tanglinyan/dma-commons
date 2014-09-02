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

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

/**
 * An AbstractOperation corresponds to a JMX operation.
 * 
 * @author Kasper Nielsen
 */
class DefaultManagedOperation {

    /** The description of the operation. */
    private final String description;

    /** The method to invoke when this operation is called. */
    private final Method m;

    /** The name of the operation. */
    private final String name;

    /** The object to invoke on. */
    private final Object o;

    /** The impact as defined in {@link MBeanOperationInfo}. */
    private final int impact;

    /**
     * Creates a new AbstractManagedOperation with the specified name and description.
     * 
     * @param obj
     *            the object the specified method should be invoked on
     * @param method
     *            the method corresponding to the operation
     * @param name
     *            the name of the operation
     * @param description
     *            the description of the operation
     * @throws NullPointerException
     *             if the specified object, method, name or description is <code>null</code>
     */
    DefaultManagedOperation(Object obj, Method method, String name, String description, int impact) {
        this.m = requireNonNull(method, "method is null");
        this.o = requireNonNull(obj, "object is null");
        this.name = requireNonNull(name, "name is null");
        this.description = requireNonNull(description, "description is null");
        this.impact = impact;
    }

    /**
     * Returns the MBeanOperationInfo for this operation.
     * 
     * @return the MBeanOperationInfo for this operation
     * @throws IntrospectionException
     *             could not obtain the information for this operation
     */
    MBeanOperationInfo getInfo() {
        return new MBeanOperationInfo(name, description, Managements.methodSignature(m), m.getReturnType().getName(),
                impact);
    }

    /**
     * Invoke the operation with specified arguments.
     * 
     * @param arguments
     *            the arguments used for invoking the operation
     * @return the result of the invocation
     * @throws MBeanException
     *             could not invoke the operation
     * @throws ReflectionException
     *             could not invoke the operation
     */
    Object invoke(Object... arguments) throws ReflectionException {
        return Managements.invoke(m, o, "operation " + name, arguments);
    }

    /**
     * Creates DefaultManagedAttribute(s) from the specified methods if the {@link ManagedOperation} annotation is
     * present.
     * 
     * @param obj
     *            the object that the operations should be invoked on
     * @param methods
     *            the methods for the object
     * @return a map mapping from the combined name of the attribute to the AbstractManagedOperation
     */
    public static Map<OperationKey, DefaultManagedOperation> getOperationFromMethods(Object obj, Method[] methods) {
        Map<OperationKey, DefaultManagedOperation> result = new HashMap<>();
        for (Method m : methods) {
            ManagedOperation mo = m.getAnnotation(ManagedOperation.class);
            if (mo != null) {
                String name = Managements.filterString(obj, mo.name());
                if (name.equals("")) {
                    name = m.getName();
                }
                List<String> signatures = new ArrayList<>(); // create String[] signature
                for (Class<?> c : m.getParameterTypes()) {
                    signatures.add(c.getName());
                }
                result.put(new OperationKey(name, signatures.toArray(new String[0])), new DefaultManagedOperation(obj,
                        m, name, Managements.getDescription(m), mo.impact()));
            }
        }
        return result;
    }
}
