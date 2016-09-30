/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.fares.bind.jackson.discovery;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Bi-directional map that works both ways.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 * @author Niels Bertram
 */
public class BiMap<K, V> {

    private Map<K, V> keyToValueMap = new ConcurrentHashMap<K, V>();
    private Map<V, K> valueToKeyMap = new ConcurrentHashMap<V, K>();

    synchronized public void put(K key, V value) {
        keyToValueMap.put(key, value);
        valueToKeyMap.put(value, key);
    }

    synchronized public V removeByKey(K key) {
        V removedValue = keyToValueMap.remove(key);
        valueToKeyMap.remove(removedValue);
        return removedValue;
    }

    synchronized public K removeByValue(V value) {
        K removedKey = valueToKeyMap.remove(value);
        keyToValueMap.remove(removedKey);
        return removedKey;
    }

    public boolean containsKey(K key) {
        return keyToValueMap.containsKey(key);
    }

    public boolean containsValue(V value) {
        return keyToValueMap.containsValue(value);
    }

    public K getKey(V value) {
        return valueToKeyMap.get(value);
    }

    public V get(K key) {
        return keyToValueMap.get(key);
    }

    public Set<K> keySet() {
        return keyToValueMap.keySet();
    }

}