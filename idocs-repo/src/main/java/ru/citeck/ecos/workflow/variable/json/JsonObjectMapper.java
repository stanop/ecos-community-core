package ru.citeck.ecos.workflow.variable.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

public class JsonObjectMapper extends ObjectMapper {

    private Map<Class<?>, Class<?>> mixIns = Collections.emptyMap();

    public void init() {
        mixIns.forEach(this::addMixInAnnotations);
    }

    public boolean hasMixIn(Class clazz) {
        return mixIns.containsKey(clazz);
    }

    public Map<Class<?>, Class<?>> getMixIns() {
        return mixIns;
    }

    public void setMixIns(Map<Class<?>, Class<?>> mixins) {
        this.mixIns = mixins;
    }
}
