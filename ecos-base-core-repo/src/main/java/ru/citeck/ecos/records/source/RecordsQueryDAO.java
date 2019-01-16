package ru.citeck.ecos.records.source;

import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;

public interface RecordsQueryDAO extends RecordsDAO {

    RecordsQueryResult<RecordRef> getRecords(RecordsQuery query);

}
