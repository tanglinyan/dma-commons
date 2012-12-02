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

import static java.util.Objects.requireNonNull;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.io.CountingOutputStream;

/**
 * 
 * @author Kasper Nielsen
 */
public class RollingOutputStream extends OutputStream {

    private CountingOutputStream cos;

    private OutputStream current;

    private int fileCount;

    private final Path folder;

    private final long maxSize;

    private final String prefix;

    private final boolean zip;

    long totalWritten;

    public RollingOutputStream(Path folder, String prefix, int maxSizeMB, boolean zip) {
        this.folder = requireNonNull(folder);
        this.prefix = requireNonNull(prefix);
        if (maxSizeMB < 1) {
            throw new IllegalArgumentException("maxSize must be at least one, was " + maxSizeMB);
        }
        maxSize = maxSizeMB * 1024L * 1024L;
        this.zip = zip;
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        if (current != null) { // on failure path we might have cos==null && current!=null
            current.close();
            current = null;
        }
        if (cos != null) {
            totalWritten += cos.getCount();
            cos.close();
            cos = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void flush() throws IOException {
        if (current != null) {
            current.flush();
        }
    }

    // We want to make sure we do not start a new file in the middle of a sentence.
    // So we only wrap whenever a message has been fully delivered
    public void checkRoll() throws IOException {
        if (cos == null || cos.getCount() >= maxSize) {
            close();
        }
    }

    private OutputStream write() throws IOException {
        if (cos == null) {
            // TODO play with buffersize, I think we want them real big, when we go multi threaded
            // to avoid collisions with read
            String filename = folder.resolve(prefix + fileCount++ + ".txt" + (zip ? ".zip" : "")).toString();
            // big buffer size is important as we do not want to write to disc to often
            current = cos = new CountingOutputStream(new BufferedOutputStream(new FileOutputStream(filename),
                    1024 * 1024));
            if (zip) {
                ZipOutputStream zos = new ZipOutputStream(cos);
                zos.putNextEntry(new ZipEntry(prefix + fileCount + ".txt"));
                // big buffer size is important as we do not want to zip to often
                current = new BufferedOutputStream(zos, 1024 * 1024);
            }
            System.out.println("Opening file" + filename);
        }
        return current;
    }

    /** {@inheritDoc} */
    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
    }

    /** {@inheritDoc} */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
    }

    /** {@inheritDoc} */
    @Override
    public void write(int b) throws IOException {
        write().write(b);
    }
}
