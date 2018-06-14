package ru.citeck.ecos.spring.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Makarskiy
 */
public class MappingRegistry<K, V> {

    private final Map<K, V> mapping = new HashMap<>();

    public void addMapping(K key, V value) {
        mapping.put(key, value);
    }

    public Map<K, V> getMapping() {
        return Collections.unmodifiableMap(mapping);
    }
}
