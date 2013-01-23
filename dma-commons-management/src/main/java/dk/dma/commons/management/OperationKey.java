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

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

/**
 * An OperationKey is used to represent an operation as a key in a map.
 * 
 * @author Kasper Nielsen
 */
final class OperationKey {

    /** The hashcode of this object. */
    private final int hashCode;

    /** The name of the operation. */
    private final String methodName;

    /** The signature of the operation. */
    private final String[] signature;

    /**
     * Creates a new OperationKey.
     * 
     * @param methodName
     *            the name of the method
     * @param signature
     *            the signature of the method
     */
    OperationKey(String methodName, String... signature) {
        this.methodName = requireNonNull(methodName);
        this.signature = signature.clone();
        hashCode = methodName.hashCode() ^ Arrays.hashCode(signature);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof OperationKey && equals((OperationKey) obj);
    }

    /**
     * As {@link #equals(Object)} except taking an OperationKey.
     * 
     * @param obj
     *            the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
     */
    public boolean equals(OperationKey obj) {
        return methodName.equals(obj.methodName) && Arrays.equals(signature, obj.signature);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return hashCode;
    }
}
