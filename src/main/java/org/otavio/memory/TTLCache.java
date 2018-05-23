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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

/**
 * A Map implementation that expires based on TTL {@link TTLCache#of(long, TimeUnit)}
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface TTLCache<K, V> extends Map<K, V> {


    /**
     * @param key the key as object
     * @return see {@link Map#get(Object)}
     * @deprecated unsafe method, please use {@link TTLCache#find(Object)} instead.
     */
    @Override
    V get(Object key);

    /**
     * @param key the key
     * @return
     */
    V find(K key);

    /**
     * Creates a {@link Map} that expires values from the TTL defined.
     * The value is represented by nanoseconds, so any amount lower than one nanosecond will come around to one.
     *
     * @param value    the value
     * @param timeUnit the unit
     * @param <K>      the key type
     * @param <V>      the value type
     * @return a new {@link DefaultTTLCache} instance
     * @throws NullPointerException     when timeUnit is null
     * @throws IllegalArgumentException when value is negative or zero
     */
    static <K, V> TTLCacheBuilderValue<K, V> of() {
        return new DefaultTTLCacheBuilder<>();
    }

    /**
     * The first step in the builder, it defines the value in TTL.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    interface TTLCacheBuilderValue<K, V> {
        /**
         * sets the value of the TTL
         *
         * @param value the value
         * @return a {@link TTLCacheBuilderUnit} instance
         * @throws IllegalArgumentException when value is negative or zero
         */
        TTLCacheBuilderUnit<K, V> value(long value);
    }

    /**
     * The second step in the builder, where it defines the time unit
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    interface TTLCacheBuilderUnit<K, V> {

        /**
         * Sets the time unit
         *
         * @param unit the time unit
         * @return a {@link TTLCacheBuilderUnit} instance
         * @throws NullPointerException when unit is null
         */
        TTLCacheBuilderUnit<K, V> unit(TimeUnit unit);
    }

    /**
     * The third step in the builder, where it definer either the supplier
     * that will request when the key was not found or the data is deprecated or construct without the supplier.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    interface TTLCacheBuilderSupplier<K, V> extends TTLCacheBuilder<K, V> {
        TTLCacheBuilder<K, V> supplier(Function<K, V> supplier);
    }

    /**
     * The last step of the builder
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    interface TTLCacheBuilder<K, V> {
        /**
         * creates a {@link TTLCache} with defined values
         *
         * @return a {@link TTLCache} instance
         */
        TTLCache<K, V> build();
    }


}
