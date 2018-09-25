package ru.citeck.ecos.graphql.meta.converter;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class MetaConverter<T> {

    public static final String META_STR_FIELD = "str";
    public static final String META_VAL_FIELD = "val";

    /**
     * Method must be stateless!
     */
    public abstract T convert(JsonNode data) throws ReflectiveOperationException;

    public abstract StringBuilder appendQuery(StringBuilder query);
}
