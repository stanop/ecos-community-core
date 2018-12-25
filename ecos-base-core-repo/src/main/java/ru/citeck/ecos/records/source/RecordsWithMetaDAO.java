package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsResult;

public interface RecordsWithMetaDAO extends RecordsDAO {

    RecordsResult<ObjectNode> getRecords(RecordsQuery query, String metaSchema);

    default RecordsResult<RecordRef> getRecords(RecordsQuery query) {
        RecordsResult<ObjectNode> records = getRecords(query, "id");
        return new RecordsResult<>(records, node -> new RecordRef(node.get("id").asText()));
    }
}
