package ru.citeck.ecos.records.source.dao;

import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;

public interface RecordsQueryWithMetaDAO extends RecordsDAO {

    RecordsQueryResult<RecordMeta> getRecords(RecordsQuery query, String metaSchema);
}
