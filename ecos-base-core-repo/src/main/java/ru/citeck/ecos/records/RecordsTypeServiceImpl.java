package ru.citeck.ecos.records;

import ecos.com.google.common.cache.CacheBuilder;
import ecos.com.google.common.cache.CacheLoader;
import ecos.com.google.common.cache.LoadingCache;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.type.ComputedAttribute;
import ru.citeck.ecos.records2.type.RecordTypeService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RecordsTypeServiceImpl implements RecordTypeService {

    private final RecordsService recordsService;
    private final LoadingCache<RecordRef, Map<String, ComputedAttribute>> computedAttributes;

    public RecordsTypeServiceImpl(RecordsService recordsService) {
        this.recordsService = recordsService;

        computedAttributes = CacheBuilder.newBuilder()
                                        .expireAfterWrite(10, TimeUnit.SECONDS)
                                        .maximumSize(50)
                                        .build(CacheLoader.from(this::getComputedAttributesImpl));
    }

    @NotNull
    @Override
    public Map<String, ComputedAttribute> getComputedAttributes(RecordRef type) {

        if (RecordRef.isEmpty(type)) {
            return Collections.emptyMap();
        }
        return computedAttributes.getUnchecked(type);
    }

    private Map<String, ComputedAttribute> getComputedAttributesImpl(RecordRef type) {

        Attributes meta = recordsService.getMeta(type, Attributes.class);
        if (meta == null || meta.computedAttributes == null) {
            return Collections.emptyMap();
        }

        Map<String, ComputedAttribute> attributes = new HashMap<>();
        meta.computedAttributes.forEach(att -> {
            attributes.put(att.getId(), att);
        });

        return attributes;
    }

    @Data
    public static class Attributes {
        private List<ComputedAttribute> computedAttributes;
    }
}
