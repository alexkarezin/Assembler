/*
 * Copyright 2023 Sebastien Pelletier
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

package io.github.pellse.util.function.checked;

import java.util.function.Supplier;

import static io.github.pellse.util.ExceptionUtils.sneakyThrow;

@FunctionalInterface
public interface CheckedSupplier<T, E extends Throwable> extends Supplier<T> {

    T checkedGet() throws E;

    @Override
    default T get() {
        try {
            return checkedGet();
        } catch (Throwable e) {
            return sneakyThrow(e);
        }
    }

    static <T1, E1 extends Throwable> CheckedSupplier<T1, E1> of(CheckedSupplier<T1, E1> supplier) {
        return supplier;
    }
}
