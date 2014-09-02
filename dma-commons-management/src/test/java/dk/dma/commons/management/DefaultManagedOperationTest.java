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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.junit.Before;
import org.junit.Test;

import dk.dma.commons.management.stubs.OperationStub;

/**
 * 
 * @author Kasper Nielsen
 */
public class DefaultManagedOperationTest {

    private static final Method DUMMY = DefaultManagedOperationTest.class.getMethods()[0];

    Map<OperationKey, DefaultManagedOperation> attr;

    OperationStub stub;

    @Test(expected = NullPointerException.class)
    public void constructorNPEDescription() {
        new DefaultManagedOperation("foo", DUMMY, "desc", null, 1);
    }

    @Test(expected = NullPointerException.class)
    public void constructorNPEMethod() {
        new DefaultManagedOperation("foo", null, "desc", "foo", 1);
    }

    @Test(expected = NullPointerException.class)
    public void constructorNPEName() {
        new DefaultManagedOperation("foo", DUMMY, null, "foo", 1);
    }

    @Test(expected = NullPointerException.class)
    public void AbstractManagedOperation() {
        new DefaultManagedOperation(null, DUMMY, "desc", "foo", 1);
    }

    @Test(expected = ReflectionException.class)
    public void illegalAccess() throws Exception {
        Method m = PrivateMethods.class.getDeclaredMethod("illegal");
        DefaultManagedOperation opr = new DefaultManagedOperation(new PrivateMethods(), m, "", "", 1);
        opr.invoke();
    }

    @Test
    public void method1() throws Exception {
        DefaultManagedOperation opr = attr.get(new OperationKey("method1"));

        MBeanOperationInfo info = opr.getInfo();
        assertEquals("method1", info.getName());
        assertEquals("", info.getDescription());
        assertEquals("void", info.getReturnType());
        assertEquals(0, info.getSignature().length);

        opr.invoke();
        assertEquals(1, stub.invokeCount);
    }

    @Test
    public void method2() throws Exception {
        DefaultManagedOperation opr = attr.get(new OperationKey("mymethod"));

        MBeanOperationInfo info = opr.getInfo();
        assertEquals("mymethod", info.getName());
        assertEquals("desc", info.getDescription());
        assertEquals("void", info.getReturnType());
        assertEquals(0, info.getSignature().length);

        opr.invoke();
        assertEquals(2, stub.invokeCount);
    }

    @Test
    public void method3Boolean() throws Exception {
        DefaultManagedOperation opr = attr.get(new OperationKey("method3", "java.lang.Boolean"));

        MBeanOperationInfo info = opr.getInfo();
        assertEquals("method3", info.getName());
        assertEquals("", info.getDescription());
        assertEquals("void", info.getReturnType());
        assertEquals(1, info.getSignature().length);
        assertEquals("java.lang.Boolean", info.getSignature()[0].getType());
        opr.invoke(Boolean.FALSE);
        assertEquals(16, stub.invokeCount);
    }

    @Test
    public void method3boolean() throws Exception {
        DefaultManagedOperation opr = attr.get(new OperationKey("method3", "boolean"));

        MBeanOperationInfo info = opr.getInfo();
        assertEquals("method3", info.getName());
        assertEquals("", info.getDescription());
        assertEquals("void", info.getReturnType());
        assertEquals(1, info.getSignature().length);
        assertEquals("boolean", info.getSignature()[0].getType());
        opr.invoke(false);
        assertEquals(8, stub.invokeCount);
    }

    @Test
    public void method3String() throws Exception {
        DefaultManagedOperation opr = attr.get(new OperationKey("method3", "java.lang.String"));

        MBeanOperationInfo info = opr.getInfo();
        assertEquals("method3", info.getName());
        assertEquals("", info.getDescription());
        assertEquals("void", info.getReturnType());
        assertEquals(1, info.getSignature().length);
        assertEquals("java.lang.String", info.getSignature()[0].getType());
        opr.invoke("foo");
        assertEquals(4, stub.invokeCount);
    }

    @Test
    public void method4() throws Exception {
        DefaultManagedOperation opr = attr.get(new OperationKey("method4"));

        MBeanOperationInfo info = opr.getInfo();
        assertEquals("method4", info.getName());
        assertEquals("", info.getDescription());
        assertEquals("java.lang.String", info.getReturnType());
        assertEquals("32", opr.invoke());
    }

    @Test
    public void method5() throws Exception {
        DefaultManagedOperation opr = attr.get(new OperationKey("method5"));

        MBeanOperationInfo info = opr.getInfo();
        assertEquals("method5", info.getName());
        assertEquals("", info.getDescription());
        assertEquals("int", info.getReturnType());
        assertEquals(64, opr.invoke());
    }

    @Before
    public void setup() throws Exception {
        stub = new OperationStub();
        attr = DefaultManagedOperation.getOperationFromMethods(stub, OperationStub.class.getMethods());
        assertEquals(10, attr.size());
    }

    @Test(expected = LinkageError.class)
    public void throwError() throws Exception {
        DefaultManagedOperation opr = attr.get(new OperationKey("throwError"));
        opr.invoke();
    }

    @Test(expected = IOException.class)
    public void throwException() throws Throwable {
        DefaultManagedOperation opr = attr.get(new OperationKey("throwException"));
        try {
            opr.invoke();
        } catch (ReflectionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void throwRuntimeError() throws Throwable {
        DefaultManagedOperation opr = attr.get(new OperationKey("throwRuntimeException"));
        try {
            opr.invoke();
        } catch (ReflectionException e) {
            throw e.getCause();
        }
    }

}
