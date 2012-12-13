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
package deprecated;

import java.nio.file.Path;
import java.util.List;

import dk.dma.app.io.OutputStreamSink;
import dk.dma.app.service.AbstractMessageProcessorService;

/**
 * 
 * @author Kasper Nielsen
 */
public class StreamToStreamStage<T> extends AbstractMessageProcessorService<T> {

    /**
     * @param queueSize
     */
    protected StreamToStreamStage(int queueSize) {
        super(queueSize);
    }

    OutputStreamSink<T> sink;

    /** {@inheritDoc} */
    @Override
    protected void handleMessages(List<T> messages) {}

    /** {@inheritDoc} */
    @Override
    protected void run() throws Exception {}

    public static <T> SinkFileWriterService<T> rollEachHourStage(Path filename, OutputStreamSink<T> sink) {
        return null;
        // return new SinkFileWriterService<>(directory, wrapUnit, queue, sink, zip);
    }

    public static <T> SinkFileWriterService<T> rollEachMinuteStage(Path filename, OutputStreamSink<T> sink) {
        return null;
        // return new SinkFileWriterService<>(directory, wrapUnit, queue, sink, zip);
    }

}
