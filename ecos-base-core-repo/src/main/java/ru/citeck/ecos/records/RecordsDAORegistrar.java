package ru.citeck.ecos.records;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.source.dao.RecordsDAO;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class RecordsDAORegistrar {

    private RecordsService recordsService;
    private List<RecordsDAO> sources;

    @Autowired
    public RecordsDAORegistrar(RecordsService recordsService, List<RecordsDAO> sources) {
        this.recordsService = recordsService;
        this.sources = sources;
    }

    @PostConstruct
    public void register() {
        sources.forEach(recordsService::register);
    }
}
