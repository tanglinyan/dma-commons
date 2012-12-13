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
package deprecated.batch;

import com.google.common.base.Supplier;

import dk.dma.app.util.function.Processor;

/**
 * 
 * @author Kasper Nielsen
 */
public abstract class PullBasedProcessor<F, T> extends Processor<Supplier<T>> {
    public void start(F file) {}

    public void stop(F file) {}
}
