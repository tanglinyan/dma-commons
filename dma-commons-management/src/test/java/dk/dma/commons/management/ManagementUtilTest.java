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

import org.junit.Test;

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
