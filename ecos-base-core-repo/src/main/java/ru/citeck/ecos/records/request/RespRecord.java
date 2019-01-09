package ru.citeck.ecos.records.request;

import com.fasterxml.jackson.databind.JsonNode;
import ru.citeck.ecos.records.RecordRef;

import java.util.HashMap;
import java.util.Map;

public class RespRecord {

    private RecordRef id;

    private Map<String, JsonNode> attributes = new HashMap<>();

    public RespRecord() {
    }

    public RespRecord(RecordRef id) {
        this.id = id;
    }

    public RecordRef getId() {
        return id;
    }

    public void setId(RecordRef id) {
        this.id = id;
    }

    public Map<String, JsonNode> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, JsonNode> attributes) {
        if (attributes != null) {
            this.attributes = new HashMap<>(attributes);
        } else {
            this.attributes = new HashMap<>();
        }
    }
}
