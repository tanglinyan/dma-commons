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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import dk.dma.app.management.Description;
import dk.dma.app.management.ManagedResource;
import dk.dma.app.management.Managements;

/**
 * 
 * @author Kasper Nielsen
 */
public class ManagementUtilTest {

    @Test
    public void getName() {
        assertEquals("Foo", Managements.getName(Foo.class, Foo.class.getAnnotation(ManagedResource.class)));
        assertEquals("bb", Managements.getName(Boo.class, Boo.class.getAnnotation(ManagedResource.class)));
    }

    @Test
    public void getDescription() {
        assertEquals("", Managements.getDescription(Foo.class));
        assertEquals("hi", Managements.getDescription(Boo.class));
    }

    /** A test stub. */
    @ManagedResource()
    public static class Foo {}

    /** A test stub. */
    @ManagedResource("bb")
    @Description("hi")
    public static class Boo {}
}
