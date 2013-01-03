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
package dk.dma.app.util.function;

import static java.util.Objects.requireNonNull;

/**
 * A {@link Block} that can throw an exception.
 * 
 * @author Kasper Nielsen
 */
public abstract class EBlock<T> {

    public abstract void accept(T t) throws Exception;

    public EBlock<T> chain(final EBlock<? super T> other) {
        requireNonNull(other);
        return new EBlock<T>() {
            public void accept(T t) throws Exception {
                EBlock.this.accept(t);
                other.accept(t);
            }
        };
    }

    public EBlock<T> filter(final Predicate<T> filter) {
        requireNonNull(filter);
        return new EBlock<T>() {
            public void accept(T t) throws Exception {
                if (filter.test(t)) {
                    EBlock.this.accept(t);
                }
            }
        };
    }
}
