package ru.citeck.ecos.records.source.dao.local;

import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.source.dao.RecordsQueryWithMetaDAO;

public interface RecordsQueryWithMetaLocalDAO<T> extends RecordsQueryWithMetaDAO {

    RecordsQueryResult<T> getMetaValues(RecordsQuery query);
}
