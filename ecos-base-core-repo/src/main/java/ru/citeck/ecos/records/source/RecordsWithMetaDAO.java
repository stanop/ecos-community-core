package ru.citeck.ecos.records.source;

import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;

public interface RecordsWithMetaDAO extends RecordsDAO {

    RecordsQueryResult<RecordMeta> getRecords(RecordsQuery query, String metaSchema);

    default RecordsQueryResult<RecordRef> getRecords(RecordsQuery query) {
        RecordsQueryResult<RecordMeta> records = getRecords(query, "id");
        return new RecordsQueryResult<>(records, RecordMeta::getId);
    }
}
