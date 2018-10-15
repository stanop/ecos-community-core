package ru.citeck.ecos.records.query;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class RecordsMetaResult extends RecordsResult {

    private List<ObjectNode> meta;

    public RecordsMetaResult() {}

    public RecordsMetaResult(RecordsResult other) {
        super(other);
    }

    public RecordsMetaResult(RecordsMetaResult other) {
        super(other);
        this.meta = other.meta;
    }

    public List<ObjectNode> getMeta() {
        return meta;
    }

    public void setMeta(List<ObjectNode> meta) {
        this.meta = meta;
    }
}