package ru.citeck.ecos.records.source.alfnode.search;

import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsResult;

import java.util.Date;

public interface AlfNodesSearch {

    enum AfterIdType {
        DB_ID, CREATED
    }

    RecordsResult<RecordRef> queryRecords(RecordsQuery query, Long afterDbId, Date afterCreated);

    AfterIdType getAfterIdType();
    
    String getLanguage();
}
