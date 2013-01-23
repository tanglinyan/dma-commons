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
package dk.dma.commons.management.stubs;

import java.io.IOException;

import dk.dma.commons.management.Description;
import dk.dma.commons.management.ManagedOperation;

/**
 * 
 * @author Kasper Nielsen
 */
public class OperationStub {
    public int invokeCount;

    @ManagedOperation
    public void method1() {
        invokeCount += 1;
    }

    @Description("desc")
    @ManagedOperation(name = "mymethod")
    public void method2() {
        invokeCount += 2;
    }

    @ManagedOperation
    public void method3(Boolean arg) {
        invokeCount += 16;
    }

    @ManagedOperation
    public void method3(boolean arg) {
        invokeCount += 8;
    }

    @ManagedOperation
    public void method3(String arg) {
        invokeCount += 4;
    }

    @ManagedOperation
    public String method4() {
        invokeCount += 32;
        return "" + invokeCount;
    }

    @ManagedOperation
    public int method5() {
        invokeCount += 64;
        return invokeCount;
    }

    @ManagedOperation
    public void throwError() {
        throw new LinkageError();
    }

    @ManagedOperation
    public void throwException() throws Exception {
        throw new IOException();
    }

    @ManagedOperation
    public void throwRuntimeException() {
        throw new IllegalMonitorStateException();
    }

}
