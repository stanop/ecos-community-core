package ru.citeck.ecos.graphql.meta.converter;

import com.fasterxml.jackson.databind.JsonNode;

public class StringConverter extends MetaConverter<String> {

    @Override
    public String convert(JsonNode data) throws ReflectiveOperationException {
        JsonNode strValue = data.get(META_STR_FIELD);
        return strValue.isTextual() ? strValue.asText() : null;
    }

    @Override
    public StringBuilder appendQuery(StringBuilder query) {
        return query.append(META_STR_FIELD);
    }
}
