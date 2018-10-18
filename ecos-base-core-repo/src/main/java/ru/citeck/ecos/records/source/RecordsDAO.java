package ru.citeck.ecos.records.source;

import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;

public interface RecordsDAO {

    RecordsResult<RecordRef> getRecords(RecordsQuery query);

    String getId();
}
