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

import org.otavio.memory.TTLCache.TTLCacheBuilder;
import org.otavio.memory.TTLCache.TTLCacheBuilderSupplier;
import org.otavio.memory.TTLCache.TTLCacheBuilderUnit;
import org.otavio.memory.TTLCache.TTLCacheBuilderValue;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A default builder to {@link TTLCache}
 * @param <K> the key type
 * @param <V> the value type
 */
class DefaultTTLCacheBuilder<K, V> implements TTLCacheBuilderValue<K, V>, TTLCacheBuilderUnit<K, V>,
        TTLCacheBuilderSupplier<K, V>, TTLCacheBuilder<K, V> {

    private long value;

    private TimeUnit unit;

    private Function<K, V> supplier;

    @Override
    public TTLCacheBuilderUnit value(long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("The value to TTL must be greater than zero");
        }
        this.value = value;
        return this;
    }

    @Override
    public TTLCacheBuilderUnit unit(TimeUnit unit) {
        Objects.requireNonNull(unit, "timeUnit is required");
        this.unit = unit;
        return this;
    }

    @Override
    public TTLCacheBuilder<K, V>  supplier(Function<K, V> supplier) {
        Objects.requireNonNull(supplier, "supplier is required");
        this.supplier = supplier;
        return this;
    }

    @Override
    public TTLCache build() {
        return new DefaultTTLCache<>(unit.toNanos(value), supplier);
    }


}
