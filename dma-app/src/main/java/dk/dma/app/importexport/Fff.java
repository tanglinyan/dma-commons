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
package dk.dma.app.importexport;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Supplier;

import dk.dma.app.util.batch.PullProcessor;
import dk.dma.app.util.batch.QueuePumper2;

/**
 * 
 * @author Kasper Nielsen
 */
public class Fff {

    public static void main(String[] args) throws IOException {
        @SuppressWarnings("resource")
        QueuePumper2<Path, String> qp = new QueuePumper2<>();
        qp.pull(new PullProcessor<Path, String>() {
            @Override
            public void processResource(Path resource, Supplier<String> supplier) {
                for (String line = supplier.get(); line != null; line = supplier.get()) {
                    System.out.println(line);
                }
            }
        });
        Path p = Paths.get("/Users/kasperni/export.csv");
        System.out.println("ddd");
        for (String s : Files.readAllLines(p, Charset.defaultCharset())) {
            System.out.println(s);
        }
    }

}
