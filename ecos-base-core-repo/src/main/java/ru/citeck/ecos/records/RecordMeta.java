package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RecordMeta {

    private RecordRef id;

    private ObjectNode attributes = JsonNodeFactory.instance.objectNode();

    public RecordMeta() {
    }

    public RecordMeta(String id) {
        this.id = new RecordRef(id);
    }

    public RecordMeta(RecordRef id) {
        this.id = id;
    }

    public RecordMeta(RecordRef id, ObjectNode attributes) {
        this.id = id;
        setAttributes(attributes);
    }

    public RecordRef getId() {
        return id;
    }

    public void setId(RecordRef id) {
        this.id = id;
    }

    public ObjectNode getAttributes() {
        return attributes;
    }

    public JsonNode getAttribute(String name) {
        return attributes.path(name);
    }

    public void setAttributes(ObjectNode attributes) {
        if (attributes != null) {
            this.attributes = attributes.deepCopy();
        } else {
            this.attributes = JsonNodeFactory.instance.objectNode();
        }
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ", \"attributes\":" + attributes +
                '}';
    }
}
