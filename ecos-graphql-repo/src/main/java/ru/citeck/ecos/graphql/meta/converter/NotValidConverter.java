package ru.citeck.ecos.graphql.meta.converter;

import com.fasterxml.jackson.databind.JsonNode;

public final class NotValidConverter extends MetaConverter<Object> {

    private NotValidConverter() {
        throw new IllegalStateException("This converter should not be used!");
    }

    @Override
    public Object convert(JsonNode data) throws ReflectiveOperationException {
        return null;
    }

    @Override
    public StringBuilder appendQuery(StringBuilder query) {
        return null;
    }
}
