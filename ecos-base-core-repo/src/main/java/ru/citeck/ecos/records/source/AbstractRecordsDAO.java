package ru.citeck.ecos.records.source;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.AttributeInfo;
import ru.citeck.ecos.records.RecordsService;

import java.util.Optional;

public abstract class AbstractRecordsDAO implements RecordsDAO {

    private String id;

    protected RecordsService recordsService;

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, String id) {
        return Optional.empty();
    }

    @Override
    public Optional<AttributeInfo> getAttributeInfo(String name) {
        return Optional.empty();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
        recordsService.register(this);
    }
}
