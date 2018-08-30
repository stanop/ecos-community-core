package ru.citeck.ecos.records.source;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.Optional;

public abstract class AbstractRecordsDAO implements RecordsDAO {

    private final String id;

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
}
