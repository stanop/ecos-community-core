package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;

public class RecordMeta {

    private RecordRef id = RecordRef.EMPTY;

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
        this.id = id != null ? id : RecordRef.EMPTY;
    }

    public ObjectNode getAttributes() {
        return attributes;
    }

    public JsonNode getAttribute(String name) {
        return attributes.path(name);
    }

    public JsonNode getAttribute(String name, Object orElse) {
        JsonNode att = attributes.get(name);
        if (!isEmpty(att)) {
            return att;
        }
        if (orElse == null) {
            return NullNode.getInstance();
        }
        if (orElse instanceof JsonNode) {
            return (JsonNode) orElse;
        }
        if (orElse instanceof String) {
            return TextNode.valueOf((String) orElse);
        }
        if (orElse instanceof Number) {
            return DoubleNode.valueOf(((Number) orElse).doubleValue());
        }
        if (orElse instanceof Boolean) {
            return BooleanNode.valueOf((Boolean) orElse);
        }
        return NullNode.getInstance();
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    private boolean isEmpty(JsonNode value) {
        return value == null || value.isMissingNode() || value.isNull();
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
