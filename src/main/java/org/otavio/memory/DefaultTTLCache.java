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

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.Collections.synchronizedMap;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

/**
 * A default implementation of {@link TTLCache}
 *
 * @param <K> the key type
 * @param <V> the value type
 */
final class DefaultTTLCache<K, V> implements TTLCache<K, V>, Runnable {

    private static final ScheduledExecutorService SCHEDULED_THREAD_POOL = Executors.newScheduledThreadPool(1);


    private final Map<K, V> store = new ConcurrentHashMap<>();
    private final Map<K, Long> timestamps = new ConcurrentHashMap<>();
    private final long ttl;
    private final Function<K, V> supplier;
    private final ScheduledFuture<?> schedule;
    private boolean open = true;
    private final Map<K, WeakReference<K>> mutex = synchronizedMap(new WeakHashMap<>());

    DefaultTTLCache(long value, TimeUnit unit, Function<K, V> supplier) {
        this.ttl = unit.toNanos(value);
        this.supplier = supplier;
        this.schedule = SCHEDULED_THREAD_POOL.schedule(this, value * 2, unit);
    }

    @Override
    public V getFromSupplier(K key) {
        Objects.requireNonNull(key, "key is required");
        if (Objects.isNull(supplier)) {
            throw new IllegalStateException("This Maps does not have supplier");
        }
        K synchronizedKey = mutex.computeIfAbsent(key, (a) -> new WeakReference<>((K) key)).get();
        synchronized (synchronizedKey) {
            V value = supplier.apply(key);
            if (Objects.nonNull(value)) {
                put(key, value);
            }
            return value;
        }

    }

    @Override
    public V get(Object key) {
        checkIsOpen();
        V value = this.store.get(key);

        boolean isExpired = isExpired(key, value);
        boolean hasSupplier = supplier != null;
        if (isExpired && !hasSupplier) {
            return null;
        } else if (Objects.nonNull(value) && !isExpired) {
            return value;
        } else if (hasSupplier) {
            K synchronizedKey = mutex.computeIfAbsent((K) key, (a) -> new WeakReference<>((K) key)).get();
            synchronized (synchronizedKey) {
                value = supplier.apply((K) key);
                if (value != null) {
                    put((K) key, value);
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        checkIsOpen();
        timestamps.put(key, System.nanoTime());
        return store.put(key, value);
    }

    @Override
    public int size() {
        checkIsOpen();
        return store.size();
    }

    @Override
    public boolean isEmpty() {
        checkIsOpen();
        return store.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        checkIsOpen();
        boolean containsKey = store.containsKey(key);
        return containsKey ? !checkExpired(key) : containsKey;
    }

    @Override
    public boolean containsValue(Object value) {
        checkIsOpen();
        return store.containsValue(value);
    }

    @Override
    public V remove(Object key) {
        checkIsOpen();
        timestamps.remove(key);
        return store.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        checkIsOpen();
        Objects.requireNonNull(map, "map is required");
        map.entrySet().forEach(this::put);
    }

    @Override
    public void clear() {
        checkIsOpen();
        synchronized (this) {
            timestamps.clear();
            store.clear();
        }

    }

    @Override
    public Set<K> keySet() {
        checkIsOpen();
        clearExpired();
        return unmodifiableSet(store.keySet());
    }

    @Override
    public Collection<V> values() {
        checkIsOpen();
        clearExpired();
        return unmodifiableCollection(store.values());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        checkIsOpen();
        clearExpired();
        return unmodifiableSet(store.entrySet());
    }

    @Override
    public void run() {
        checkIsOpen();
        if (!this.isEmpty()) {
            clearExpired();
        }
    }

    @Override
    public void close() throws Exception {
        schedule.cancel(true);
        this.open = false;
        this.store.clear();
        this.timestamps.clear();
    }

    private void checkIsOpen() {
        if (!open) {
            throw new IllegalStateException("This cache is disabled to use, the resource it close" +
                    " and ready to GC, please create a new one.");
        }
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
