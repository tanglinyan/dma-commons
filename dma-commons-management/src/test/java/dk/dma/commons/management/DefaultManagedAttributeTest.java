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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.ReflectionException;

import org.junit.Before;
import org.junit.Test;

import dk.dma.commons.management.DefaultManagedAttribute;
import dk.dma.commons.management.ManagedAttribute;
import dk.dma.commons.management.stubs.VariousAttributes;

/**
 * 
 * @author Kasper Nielsen
 */
public class DefaultManagedAttributeTest {

    Map<String, DefaultManagedAttribute> attr;

    VariousAttributes stub;

    @Test(expected = NullPointerException.class)
    public void defaultManagedAttribute1NPE() {
        Method m = DefaultManagedAttribute.class.getMethods()[0];
        new DefaultManagedAttribute(null, m, m, "name", "desc");
    }

    @Test(expected = NullPointerException.class)
    public void defaultManagedAttribute2NPE() {
        new DefaultManagedAttribute(new Object(), null, null, "name", "desc");
    }

    @Test(expected = NullPointerException.class)
    public void defaultManagedAttribute3NPE() {
        Method m = DefaultManagedAttribute.class.getMethods()[0];
        new DefaultManagedAttribute(new Object(), m, m, null, "desc");
    }

    @Test(expected = NullPointerException.class)
    public void defaultManagedAttribute4NPE() {
        Method m = DefaultManagedAttribute.class.getMethods()[0];
        new DefaultManagedAttribute(new Object(), m, m, "name", null);
    }

    @Test(expected = LinkageError.class)
    public void getError() throws Exception {
        DefaultManagedAttribute att = attr.get("throwError");
        assertEquals("desc", att.getInfo().getDescription());
        att.getValue();
    }

    @Test(expected = IOException.class)
    public void getException() throws Throwable {
        DefaultManagedAttribute att = attr.get("Exception2");
        try {
            att.getValue();
        } catch (ReflectionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void getRuntimeException() throws Throwable {
        DefaultManagedAttribute att = attr.get("throwRuntimeException");
        assertEquals("desc", att.getInfo().getDescription());
        try {
            att.getValue();
        } catch (ReflectionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ReflectionException.class)
    public void illegalAccessGet() throws Exception {
        Method mGet = PrivateMethods.class.getDeclaredMethod("getIllegal");
        Method mSet = PrivateMethods.class.getDeclaredMethod("setIllegal", String.class);
        DefaultManagedAttribute opr = new DefaultManagedAttribute(new PrivateMethods(), mGet, mSet, "", "");
        opr.getValue();
    }

    @Test(expected = ReflectionException.class)
    public void illegalAccessSet() throws Exception {
        Method mGet = PrivateMethods.class.getDeclaredMethod("getIllegal");
        Method mSet = PrivateMethods.class.getDeclaredMethod("setIllegal", String.class);
        DefaultManagedAttribute opr = new DefaultManagedAttribute(new PrivateMethods(), mGet, mSet, "", "");
        opr.setValue("dd");
    }

    @Test
    public void readOnly() throws Exception {
        DefaultManagedAttribute att = attr.get("ReadOnly");
        MBeanAttributeInfo info = att.getInfo();
        assertEquals("ReadOnly", info.getName());
        assertEquals("", info.getDescription());
        assertEquals("boolean", info.getType());
        assertTrue(info.isReadable());
        assertFalse(info.isWritable());
        assertTrue(info.isIs());

        assertFalse(stub.isReadOnly());
        stub.setReadOnly(true);
        assertTrue(stub.isReadOnly());
    }

    @Test(expected = IllegalStateException.class)
    public void readOnlySet() throws Exception {
        DefaultManagedAttribute att = attr.get("ReadOnly");
        att.setValue(false);
    }

    @Test
    public void readWritable() throws Exception {
        DefaultManagedAttribute att = attr.get("ReadWrite");
        MBeanAttributeInfo info = att.getInfo();
        assertEquals("ReadWrite", info.getName());
        assertEquals("", info.getDescription());
        assertEquals("java.lang.Integer", info.getType());
        assertTrue(info.isReadable());
        assertTrue(info.isWritable());
        assertFalse(info.isIs());

        assertNull(att.getValue());
        att.setValue(123);
        assertEquals(123, att.getValue());
    }

    @Test(expected = LinkageError.class)
    public void setError() throws Exception {
        DefaultManagedAttribute att = attr.get("throwError");
        assertEquals("desc", att.getInfo().getDescription());
        att.setValue("foo");
    }

    @Test(expected = IOException.class)
    public void setException() throws Throwable {
        DefaultManagedAttribute att = attr.get("Exception1");
        try {
            att.setValue("ignore");
        } catch (ReflectionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void setRuntimeException() throws Throwable {
        DefaultManagedAttribute att = attr.get("throwRuntimeException");
        assertEquals("desc", att.getInfo().getDescription());
        try {
            att.setValue("ignore");
        } catch (ReflectionException e) {
            throw e.getCause();
        }
    }

    @Before
    public void setup() throws Exception {
        BeanInfo bi = Introspector.getBeanInfo(VariousAttributes.class);
        stub = new VariousAttributes();
        attr = DefaultManagedAttribute.fromPropertyDescriptors(bi.getPropertyDescriptors(), stub);
        assertEquals(7, attr.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void twoAttributeAnnotations() throws Exception {
        BeanInfo bi = Introspector.getBeanInfo(TwoAttributes.class);
        attr = DefaultManagedAttribute.fromPropertyDescriptors(bi.getPropertyDescriptors(), new TwoAttributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void writableReader() throws Exception {
        BeanInfo bi = Introspector.getBeanInfo(WritableReader.class);
        attr = DefaultManagedAttribute.fromPropertyDescriptors(bi.getPropertyDescriptors(), new WritableReader());
    }

    @Test
    public void writeOnly() throws Exception {
        DefaultManagedAttribute att = attr.get("WriteOnly");
        MBeanAttributeInfo info = att.getInfo();
        assertEquals("WriteOnly", info.getName());
        assertEquals("", info.getDescription());
        assertEquals("java.lang.String", info.getType());
        assertFalse(info.isReadable());
        assertTrue(info.isWritable());
        assertFalse(info.isIs());

        assertNull(stub.getWriteOnly());
        att.setValue("foo");
        assertEquals("foo", stub.getWriteOnly());
    }

    @Test(expected = IllegalStateException.class)
    public void writeOnlySet() throws Exception {
        DefaultManagedAttribute att = attr.get("WriteOnly");
        att.getValue();
    }

    /** A test stub. */
    public static class TwoAttributes {

        @ManagedAttribute
        public String getFoo() {
            return null;
        }

        @ManagedAttribute
        public void setFoo(String ignore) {}
    }

    /** A test stub. */
    public static class WritableReader {
        @ManagedAttribute(isWriteOnly = true)
        public String getFoo() {
            return null;
        }
    }
}
