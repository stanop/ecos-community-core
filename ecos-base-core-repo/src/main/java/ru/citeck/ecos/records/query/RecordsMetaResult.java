package ru.citeck.ecos.records.query;

import com.fasterxml.jackson.databind.JsonNode;
import ru.citeck.ecos.records.RecordRef;

import java.util.Map;

public class RecordsMetaResult extends RecordsResult {

    private Map<RecordRef, JsonNode> meta;

    public RecordsMetaResult() {}

    public RecordsMetaResult(RecordsResult other) {
        super(other);
    }

    public RecordsMetaResult(RecordsMetaResult other) {
        super(other);
        this.meta = other.meta;
    }

    public Map<RecordRef, JsonNode> getMeta() {
        return meta;
    }

    public void setMeta(Map<RecordRef, JsonNode> meta) {
        this.meta = meta;
    }
}