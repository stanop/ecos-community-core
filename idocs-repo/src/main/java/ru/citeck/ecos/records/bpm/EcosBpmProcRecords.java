package ru.citeck.ecos.records.bpm;

import org.alfresco.service.cmr.search.SearchService;
import ru.citeck.ecos.model.EcosBpmModel;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.source.dao.AbstractRecordsDAO;
import ru.citeck.ecos.records.source.dao.RecordsQueryDAO;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

public class EcosBpmProcRecords extends AbstractRecordsDAO implements RecordsQueryDAO {

    public static final String ID = "ebpmproc";

    public EcosBpmProcRecords() {
        setId(ID);
    }

    @Override
    public RecordsQueryResult<RecordRef> getRecords(RecordsQuery query) {

        RecordsQuery processNodesQuery = new RecordsQuery();
        processNodesQuery.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);

        processNodesQuery.setQuery(FTSQuery.create()
                                          .type(EcosBpmModel.TYPE_PROCESS_MODEL)
                                          .getQuery());

        RecordsQueryResult<RecordRef> result = recordsService.getRecords(processNodesQuery);
        return RecordsUtils.toScoped(ID, result);
    }

    private static class ProcessQuery {
        public String category;
    }
}
