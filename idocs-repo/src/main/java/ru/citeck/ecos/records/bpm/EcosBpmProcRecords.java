package ru.citeck.ecos.records.bpm;

import org.alfresco.service.cmr.search.SearchService;
import ru.citeck.ecos.model.EcosBpmModel;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsResult;
import ru.citeck.ecos.records.source.AbstractRecordsDAO;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

public class EcosBpmProcRecords extends AbstractRecordsDAO {

    public static final String ID = "ebpmproc";

    public EcosBpmProcRecords() {
        setId(ID);
    }

    @Override
    public RecordsResult<RecordRef> getRecords(RecordsQuery query) {

        RecordsQuery processNodesQuery = new RecordsQuery();
        processNodesQuery.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);

        processNodesQuery.setQuery(FTSQuery.create()
                                          .type(EcosBpmModel.TYPE_PROCESS_MODEL)
                                          .getQuery());

        RecordsResult<RecordRef> result = recordsService.getRecords(processNodesQuery);
        return RecordsUtils.toScoped(ID, result);
    }

    private static class ProcessQuery {
        public String category;
    }
}
