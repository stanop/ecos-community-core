package ru.citeck.ecos.records.source.alfnode;

import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;

public interface AlfNodesSearch {

    DaoRecordsResult queryRecords(RecordsQuery query, Long afterDbId);

    String getLanguage();
}
