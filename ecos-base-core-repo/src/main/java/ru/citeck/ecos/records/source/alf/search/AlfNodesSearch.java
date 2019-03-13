package ru.citeck.ecos.records.source.alf.search;

import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.request.query.lang.DistinctQuery;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public interface AlfNodesSearch {

    enum AfterIdType {
        DB_ID, CREATED
    }

    RecordsQueryResult<RecordRef> queryRecords(RecordsQuery query, Long afterDbId, Date afterCreated);

    default List<Object> queryDistinctValues(DistinctQuery query, int max) {
        return Collections.emptyList();
    }

    AfterIdType getAfterIdType();
    
    String getLanguage();
}
