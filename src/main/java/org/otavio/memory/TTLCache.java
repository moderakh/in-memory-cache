/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.otavio.memory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A Map implementation that expires based on TTL {@link TTLCache#of(long, TimeUnit)}
 * This class implements {@link AutoCloseable} once it uses a scheduler service
 * to clean the cache, the close method will cancel that
 * scheduler and makes the instance either GG eligible and in an illegal state.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface TTLCache<K, V> extends Map<K, V>, AutoCloseable {



    /**
     * Creates a {@link Map} that expires values from the TTL defined.
     * The value is represented by nanoseconds, so any amount lower than one nanosecond will come around to one.
     *
     * @param value    the value
     * @param timeUnit the unit
     * @param <K>      the key type
     * @param <V>      the value type
     * @return a new {@link TTLCache} instance
     * @throws NullPointerException     when timeUnit is null
     * @throws IllegalArgumentException when value is negative or zero
     */
    static <K, V> TTLCache<K, V> of(long value, TimeUnit timeUnit) {
        return of(value, timeUnit, null);
    }

    /**
     * Creates a {@link Map} that expires values from the TTL defined.
     * The value is represented by nanoseconds, so any amount lower than one nanosecond will come around to one.
     *
     * @param value    the value
     * @param timeUnit the unit
     * @param supplier the supplier
     * @param <K>      the key type
     * @param <V>      the value type
     * @return a new {@link TTLCache} instance
     * @throws NullPointerException     when timeUnit is null
     * @throws IllegalArgumentException when value is negative or zero
     */
    static <K, V> TTLCache<K, V> of(long value, TimeUnit timeUnit, Function<K, V> supplier) {
        Objects.requireNonNull(timeUnit, "timeUnit is required");
        if (value <= 0) {
            throw new IllegalArgumentException("The value to TTL must be greater than zero");
        }
        return new DefaultTTLCache<>(value, timeUnit, supplier);
    }

}
