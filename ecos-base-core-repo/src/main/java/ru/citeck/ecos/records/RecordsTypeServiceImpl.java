package ru.citeck.ecos.records;

import ecos.com.google.common.cache.CacheBuilder;
import ecos.com.google.common.cache.CacheLoader;
import ecos.com.google.common.cache.LoadingCache;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.citeck.ecos.records.type.TypeDto;
import ru.citeck.ecos.records.type.TypeInfoProvider;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.type.ComputedAttribute;
import ru.citeck.ecos.records2.type.RecordTypeService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RecordsTypeServiceImpl implements RecordTypeService {

    private final LoadingCache<RecordRef, Map<String, ComputedAttribute>> computedAttributes;
    private final TypeInfoProvider typeInfoProvider;

    public RecordsTypeServiceImpl(TypeInfoProvider typeInfoProvider) {

        this.typeInfoProvider = typeInfoProvider;
        if (typeInfoProvider == null) {
            log.warn("TypeInfoProvider is null. Some features of ECOS types won't be allowed");
        }

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

        if (typeInfoProvider == null) {
            return Collections.emptyMap();
        }

        TypeDto typeDto = typeInfoProvider.getType(type);

        if (typeDto == null) {
            return Collections.emptyMap();
        }
        List<ComputedAttribute> atts = typeDto.getComputedAttributes();

        if (atts == null || atts.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, ComputedAttribute> attributes = new HashMap<>();
        atts.forEach(att -> attributes.put(att.getId(), att));

        return attributes;
    }

    @Data
    public static class Attributes {
        private List<ComputedAttribute> computedAttributes;
    }
}
