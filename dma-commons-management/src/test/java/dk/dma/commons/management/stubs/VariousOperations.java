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
package dk.dma.commons.management.stubs;

import dk.dma.commons.management.Description;
import dk.dma.commons.management.ManagedOperation;

/**
 * @author Kasper Nielsen
 */
public class VariousOperations {
    public int invokeCount;

    @ManagedOperation(name = "m2")
    public void method2() {
        invokeCount++;
    }

    @ManagedOperation()
    public String method3() {
        return "m3";
    }

    @ManagedOperation()
    public String method4(String arg) {
        return arg.toUpperCase();
    }

    @Description("desca")
    @ManagedOperation()
    public void method5() {}

    @Description("desc")
    @ManagedOperation(name = "foo")
    public void method6() {}
}
