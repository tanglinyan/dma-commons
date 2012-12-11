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
package dk.dma.app.util.io;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Kasper Nielsen
 */
public class IoUtil {

    static Path addTimestamp(Path base, TimeUnit unit) {
        long now = System.currentTimeMillis();
        String filename = base.getFileName().toString();
        int i = filename.indexOf('.');
        String postfix = addTimeStampGetPostFix(new Date(now), unit);
        if (i > 0) {
            filename = filename.substring(0, i) + "-" + postfix + filename.substring(i);
        } else {
            filename = filename + "-" + postfix;
        }
        return base.getParent().resolve(filename);
    }

    private static String addTimeStampGetPostFix(Date now, TimeUnit unit) {
        if (unit == TimeUnit.MINUTES) {
            // does not handle daylight saving properties
            // Ogsaa lige et problem hvis den allerede har vaere aabnet
            // og saa bliver service genstarted
            return new SimpleDateFormat("yyyy-MM-dd_HH:mm").format(now);
        } else if (unit == TimeUnit.HOURS) {
            return new SimpleDateFormat("yyyy-MM-dd_HH").format(now);
        } else if (unit == TimeUnit.DAYS) {
            return new SimpleDateFormat("yyyy-MM-dd").format(now);
        } else {
            throw new IllegalArgumentException(unit.toString());
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

    public static final void validateFolderExist(String parameterName, File folder) {
        if (!folder.exists()) {
            System.err.println(parameterName + " folder does not exist, " + parameterName + " = " + folder);
            System.exit(1);
        } else if (!folder.exists()) {
            System.err.println("Specified " + parameterName + " is not a folder, " + parameterName + " = " + folder);
            System.exit(1);
        }
    }
}
