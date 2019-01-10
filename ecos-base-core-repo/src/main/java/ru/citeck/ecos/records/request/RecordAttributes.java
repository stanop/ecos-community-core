package ru.citeck.ecos.records.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.citeck.ecos.records.RecordRef;

public class RecordAttributes {

    private RecordRef id;

    private ObjectNode attributes = JsonNodeFactory.instance.objectNode();

    public RecordAttributes() {
    }

    public RecordAttributes(RecordRef id) {
        this.id = id;
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
}
