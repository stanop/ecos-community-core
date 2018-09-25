package ru.citeck.ecos.records.source.alfnode;

import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;

public interface AlfNodesSearch {

    RecordsResult queryRecords(RecordsQuery query, Long afterDbId);

    String getLanguage();
}
