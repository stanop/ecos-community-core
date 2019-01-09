package ru.citeck.ecos.records.source;

import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsResult;

public interface RecordsDAO {

    RecordsResult<RecordRef> getRecords(RecordsQuery query);

    String getId();
}
