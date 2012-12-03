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
package dk.dma.app.util;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * @author Kasper Nielsen
 */
public class IoUtil {

    public static final void validateFolderExist(String parameterName, File folder) {
        if (!folder.exists()) {
            System.err.println(parameterName + " folder does not exist, " + parameterName + " = " + folder);
            System.exit(1);
        } else if (!folder.exists()) {
            System.err.println("Specified " + parameterName + " is not a folder, " + parameterName + " = " + folder);
            System.exit(1);
        }
    }

    public static OutputStream notCloseable(OutputStream os) {
        return new FilterOutputStream(os) {
            /** {@inheritDoc} */
            @Override
            public void close() throws IOException {
                throw new UnsupportedOperationException("Close is not supported");
            }
        };
    }
}
