package ru.citeck.ecos.records.attribute;

import com.fasterxml.jackson.databind.JsonNode;

public interface AttributeAccessor {

    default String getSchema() {
        return appendSchema(new StringBuilder()).toString();
    }

    StringBuilder appendSchema(StringBuilder sb);

    JsonNode getValue(JsonNode raw, boolean flat);

}
