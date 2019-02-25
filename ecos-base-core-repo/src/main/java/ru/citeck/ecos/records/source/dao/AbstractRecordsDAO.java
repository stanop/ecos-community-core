package ru.citeck.ecos.records.source.dao;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.RecordsUtils;

import javax.annotation.PostConstruct;

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
