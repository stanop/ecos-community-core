package ru.citeck.ecos.records.source.alfnode;

import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;

import java.util.Date;

public interface AlfNodesSearch {

    enum AfterIdType {
        DB_ID, CREATED
    }

    RecordsResult<RecordRef> queryRecords(RecordsQuery query, Long afterDbId, Date afterCreated);

    AfterIdType getAfterIdType();
    
    String getLanguage();
}
