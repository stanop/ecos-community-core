package ru.citeck.ecos.records.source;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.RecordsUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractRecordsDAO implements RecordsDAO {

    private String id;

    protected RecordsService recordsService;
    protected RecordsUtils recordsUtils;

    @PostConstruct
    public final void registerDAO() {
        if (id != null) {
            recordsService.register(this);
        }
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, RecordRef recordRef) {
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
    }

    @Autowired
    public void setRecordsUtils(RecordsUtils recordsUtils) {
        this.recordsUtils = recordsUtils;
    }
}
