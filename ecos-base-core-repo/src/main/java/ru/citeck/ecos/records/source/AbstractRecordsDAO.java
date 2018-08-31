package ru.citeck.ecos.records.source;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordsService;

import java.util.Optional;

public abstract class AbstractRecordsDAO implements RecordsDAO {

    private final String id;

    protected RecordsService recordsService;

    public AbstractRecordsDAO(String id) {
        this.id = id;
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, String id) {
        return Optional.empty();
    }

    @Override
    public String getId() {
        return id;
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
        recordsService.register(this);
    }
}
