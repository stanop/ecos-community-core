package ru.citeck.ecos.records.attributes;

import com.fasterxml.jackson.databind.JsonNode;

public interface AttributeAccessor<T extends JsonNode> {

    default String getSchema() {
        return appendSchema(new StringBuilder()).toString();
    }

    StringBuilder appendSchema(StringBuilder sb);

    T getValue(JsonNode raw);
}
