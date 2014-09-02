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
package dk.dma.commons.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for checking if other instances are running.
 */
public class OneInstanceGuard {

    private static final Logger LOG = LoggerFactory.getLogger(OneInstanceGuard.class);

    private File lockFile;
    private FileChannel channel;
    private FileLock lock;
    private boolean alreadyRunning;

    public OneInstanceGuard(String lockFileName) {
        lockFile = new File(lockFileName);
        if (lockFile.exists()) {
            lockFile.delete();
        }
        try {
            channel = new RandomAccessFile(lockFile, "rw").getChannel();
        } catch (FileNotFoundException e) {
            // Not running
            LOG.info("File not found: " + e);
            return;
        }
        try {
            lock = channel.tryLock();
            if (lock == null) {
                // File is lock by other application
                channel.close();
                throw new IOException("Instance already active");
            }
        } catch (IOException e) {
            // Running
            LOG.info("Instance already running");
            alreadyRunning = true;
            return;
        }
        ShutdownHook shutdownHook = new ShutdownHook(this);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

    }

    public void unlockFile() {
        // release and delete file lock
        try {
            if (lock != null) {
                lock.release();
                channel.close();
                lockFile.delete();
            }
        } catch (IOException e) {
            LOG.error("Failed to unlock lock file");
        }
    }

    public boolean isAlreadyRunning() {
        return alreadyRunning;
    }

    static class ShutdownHook extends Thread {

        private OneInstanceGuard guard;

        public ShutdownHook(OneInstanceGuard guard) {
            setDaemon(true);
            this.guard = guard;
        }

        public void run() {
            guard.unlockFile();
        }
    }

}
