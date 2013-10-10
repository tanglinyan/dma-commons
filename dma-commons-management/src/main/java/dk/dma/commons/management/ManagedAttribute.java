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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to mark methods that should be exposed via JMX. Should only be used on JavaBean getters or
 * setters. If bean has both a setter and a getter for a specific attribute, only the getter or the setter should be
 * annotated with this annotation, not both.
 * 
 * It is possible to expose attributes as both Read-only, Write-only or as Read-Writeable by using these simple rules.
 * 
 * Read-Write: If this annotation is used on the setter and the attribute has a valid getter for the attribute
 * Read-Only: If this annotation is used on the getter, the attribute will be exposed as read only even if a valid
 * setter is available for the attribute. Write-only, if this annotation is used on the setter, and no valid getter
 * exists. If a getter exists {@link #isWriteOnly()} must be set to true
 * 
 * @author Kasper Nielsen
 */
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface ManagedAttribute {

    /** The name of the attribute. */
    String name() default "";

    /** Whether or not this attribute is write only. Should only be used on a setter. */
    boolean isWriteOnly() default false;
}
