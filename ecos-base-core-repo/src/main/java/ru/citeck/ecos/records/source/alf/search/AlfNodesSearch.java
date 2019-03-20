package ru.citeck.ecos.records.source.alf.search;

import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;

import java.util.Date;

public interface AlfNodesSearch {

    enum AfterIdType {
        DB_ID, CREATED
    }

    RecordsQueryResult<RecordRef> queryRecords(RecordsQuery query, Long afterDbId, Date afterCreated);

    AfterIdType getAfterIdType();
    
    String getLanguage();
}
