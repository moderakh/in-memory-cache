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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

/**
 * A default implementation of {@link TTLCache}
 *
 * @param <K> the key type
 * @param <V> the value type
 */
final class DefaultTTLCache<K, V> implements TTLCache<K, V> {

    private static final Logger LOGGER = Logger.getLogger(DefaultTTLCache.class.getName());

    private final Map<K, V> store = new ConcurrentHashMap<>();
    private final Map<K, Long> timestamps = new ConcurrentHashMap<>();
    private final long ttl;
    private final Function<K, V> supplier;

    DefaultTTLCache(long ttl, Function<K, V> supplier) {
        this.ttl = ttl;
        this.supplier = supplier;
    }

    @Override
    public V get(Object key) {
        LOGGER.warning("Deprecated method and it does not use cache, to better approach, please use TTLCache#find.");
        V value = this.store.get(key);
        if (value != null && checkExpired(key)) {
            return null;
        } else {
            return value;
        }
    }

    @Override
    public V find(K key) {
        V value = this.store.get(key);

        boolean isExpired = isExpired(key, value);
        boolean hasSupplier = supplier != null;
        if (isExpired && !hasSupplier) {
            return null;
        } else if (!isExpired) {
            return value;
        } else if (hasSupplier) {
            value = supplier.apply(key);
            if (value != null) {
                put(key, value);
                return value;
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        timestamps.put(key, System.nanoTime());
        return store.put(key, value);
    }

    @Override
    public int size() {
        return store.size();
    }

    @Override
    public boolean isEmpty() {
        return store.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        boolean containsKey = store.containsKey(key);
        return containsKey ? !checkExpired(key) : containsKey;
    }

    @Override
    public boolean containsValue(Object value) {
        return store.containsValue(value);
    }

    @Override
    public V remove(Object key) {
        timestamps.remove(key);
        return store.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is required");
        map.entrySet().forEach(this::put);
    }

    @Override
    public void clear() {
        timestamps.clear();
        store.clear();
    }

    @Override
    public Set<K> keySet() {
        clearExpired();
        return unmodifiableSet(store.keySet());
    }

    @Override
    public Collection<V> values() {
        clearExpired();
        return unmodifiableCollection(store.values());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        clearExpired();
        return unmodifiableSet(store.entrySet());
    }

    private void clearExpired() {
        store.keySet().stream().forEach(this::checkExpired);
    }

    private void put(Entry<? extends K, ? extends V> entry) {
        this.put(entry.getKey(), entry.getValue());
    }

    private boolean checkExpired(Object key) {
        if (isObsolete(key)) {
            remove(key);
            return true;
        }
        return false;
    }

    private boolean isObsolete(Object key) {
        return (System.nanoTime() - timestamps.get(key)) > this.ttl;
    }

    private boolean isExpired(Object key, V value) {
        return value != null && checkExpired(key);
    }


}
