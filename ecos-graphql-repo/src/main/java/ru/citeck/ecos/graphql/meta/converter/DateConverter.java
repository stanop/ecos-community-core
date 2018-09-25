package ru.citeck.ecos.graphql.meta.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

import java.util.Date;

public class DateConverter extends MetaConverter<Date> {

    @Override
    public Date convert(JsonNode data) throws ReflectiveOperationException {
        JsonNode strValue = data.get(META_STR_FIELD);
        return strValue.isTextual() ? ISO8601Utils.parse(strValue.asText()) : null;
    }

    @Override
    public StringBuilder appendQuery(StringBuilder query) {
        return query.append(META_STR_FIELD);
    }
}
