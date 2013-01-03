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

import java.lang.reflect.Method;

/**
 * 
 * @author Kasper Nielsen
 */
@SuppressWarnings("unused")
public class PrivateMethods {

    public static final Method PRIVATE_VOID = m("privateVoid");

    public static final Method PRIVATE_VOID_INT = m("privateVoidInt", int.class);

    private void privateVoid() {}

    private void privateVoidInt(int i) {}

    private String getIllegal() {
        return null;
    };

    private void illegal() {};

    private void setIllegal(String parameter) {};

    static Method m(String name, Class<?>... parameterTypes) {
        try {
            return PrivateMethods.class.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }
}
