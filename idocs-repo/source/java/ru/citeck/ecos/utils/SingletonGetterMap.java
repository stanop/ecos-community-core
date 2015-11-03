/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class SingletonGetterMap<K, V> implements Map<K, V> {
    
    private final K key;
    private final Callable<V> getter;
    
    public SingletonGetterMap(K key, Callable<V> getter) {
        this.key = key;
        this.getter = getter;
    }
    
    private V getImpl() {
        try {
            return getter.call();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.key.equals(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        return containsKey(key) ? getImpl() : null;
    }

    @Override
    public Set<K> keySet() {
        return Collections.singleton(key);
    }

    @Override
    public Collection<V> values() {
        return Collections.singleton(getImpl());
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        java.util.Map.Entry<K, V> entry = new java.util.Map.Entry<K, V>() {
            
            @Override 
            public K getKey() { 
                return key; 
            }
            
            @Override 
            public V getValue() { 
                return getImpl(); 
            }
            
            @Override 
            public V setValue(V value) {
                throw new UnsupportedOperationException();
            }
            
        };
        return Collections.singleton(entry);
    }
    
}

