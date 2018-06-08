package ru.citeck.ecos.spring.registry;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Makarskiy
 */
public class MappingRegistrar<K, V> {

    private final MappingRegistry<K, V> registry;

    private Map<K, V> mapping = new HashMap<>();

    public MappingRegistrar(MappingRegistry<K, V> registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void registerMapping() {
        mapping.forEach(registry::addMapping);
    }

    public void setMapping(Map<K, V> mapping) {
        this.mapping = mapping;
    }
}
