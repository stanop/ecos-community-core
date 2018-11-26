package ru.citeck.ecos.records.query;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class RecordsNodesResult extends RecordsResult<ObjectNode> {

    public RecordsNodesResult() {
    }

    public RecordsNodesResult(RecordsNodesResult other) {
        super(other);
    }
}
