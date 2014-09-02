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
package dk.dma.commons.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author Kasper Nielsen
 */
public class IoUtil {

    public static void writeAscii(CharSequence ascii, OutputStream os) throws IOException {
        os.write(ascii.toString().getBytes(StandardCharsets.US_ASCII));
    }

    public static long recursiveSizeOf(Path p) throws IOException {
        final AtomicLong size = new AtomicLong();
        Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                size.addAndGet(attrs.size());
                return FileVisitResult.CONTINUE;
            }
        });
        return size.get();
    }

    public static Object readObject(DataInputStream dis) throws IOException, ClassNotFoundException {
        int size = dis.readInt();
        byte[] input = new byte[size];
        dis.readFully(input);
        return readObject(input);
    }

    public static Object readObject(byte[] input) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bin = new ByteArrayInputStream(input);
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
        return in.readObject();
    }

    public static void writeObject(DataOutputStream dos, Object o) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(20000);
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));) {
            out.writeObject(o);
        }
        dos.writeInt(bout.size());
        dos.write(bout.toByteArray());
        dos.flush();
    }

    public static Path addTimestamp(Path base, TimeUnit unit) {
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

    public static Path findLatestModified(Iterable<Path> i) throws IOException {
        Path latest = null;
        FileTime latestTime = null;
        for (Path p : i) {
            if (latest == null) {
                latest = p;
                latestTime = Files.getLastModifiedTime(p);
            } else {
                FileTime t = Files.getLastModifiedTime(p);
                if (t.compareTo(latestTime) > 0) {
                    latest = p;
                    latestTime = t;
                }
            }
        }
        return latest;
    }

    public static String addTimeStampGetPostFix(Date now, TimeUnit unit) {
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
