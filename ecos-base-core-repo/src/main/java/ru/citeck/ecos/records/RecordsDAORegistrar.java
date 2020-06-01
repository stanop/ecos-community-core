package ru.citeck.ecos.records;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.source.dao.RecordsDao;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class RecordsDAORegistrar {

    private final RecordsService recordsService;
    private final List<RecordsDao> sources;

    @Autowired
    public RecordsDAORegistrar(RecordsService recordsService, List<RecordsDao> sources) {
        this.recordsService = recordsService;
        this.sources = sources;
    }

    @PostConstruct
    public void register() {
        sources.forEach(recordsService::register);
    }
}
