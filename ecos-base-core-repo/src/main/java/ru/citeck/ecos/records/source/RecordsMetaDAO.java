package ru.citeck.ecos.records.source;

import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.result.RecordsResult;

import java.util.List;

public interface RecordsMetaDAO extends RecordsDAO {

    RecordsResult<RecordMeta> getMeta(List<RecordRef> records, String gqlSchema);
}
