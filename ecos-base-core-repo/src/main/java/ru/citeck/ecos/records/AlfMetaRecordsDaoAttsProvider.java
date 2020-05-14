package ru.citeck.ecos.records;

import ecos.com.google.common.cache.CacheBuilder;
import ecos.com.google.common.cache.CacheLoader;
import ecos.com.google.common.cache.LoadingCache;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records2.source.dao.local.MetaRecordsDaoAttsProvider;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class AlfMetaRecordsDaoAttsProvider implements MetaRecordsDaoAttsProvider {

    private final ModuleService moduleService;

    private final LoadingCache<String, Optional<Object>> attsCache;

    @Autowired
    public AlfMetaRecordsDaoAttsProvider(ModuleService moduleService) {
        this.moduleService = moduleService;

        attsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .maximumSize(100)
            .build(CacheLoader.from(this::getAttributeImpl));
    }

    private Optional<Object> getAttributeImpl(String name) {

        Object result = null;

        switch (name) {
            case "edition":
                result = getEdition();
                break;
            case "alfModules":
                result = new AlfModules();
                break;
        }

        return Optional.ofNullable(result);
    }

    private String getEdition() {
        ModuleDetails module = moduleService.getModule("ecos-enterprise-repo");
        if (module == null) {
            return "community";
        } else {
            return "enterprise";
        }
    }

    @Override
    public Object getAttributes() {
        return new Attributes();
    }

    public class Attributes implements MetaValue {

        @Override
        public Object getAttribute(String name, MetaField field) {
            return attsCache.getUnchecked(name).orElse(null);
        }
    }

    public class AlfModules implements MetaValue {

        @Override
        public boolean has(String name) {
            return moduleService.getModule(name) != null;
        }
    }
}
