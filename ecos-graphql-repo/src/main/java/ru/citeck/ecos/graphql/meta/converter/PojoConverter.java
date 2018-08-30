package ru.citeck.ecos.graphql.meta.converter;

import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PojoConverter<T> extends MetaConverter<T> {

    private Class<T> dataClass;

    private Map<Field, FieldConverter<?>> fields = new ConcurrentHashMap<>();

    PojoConverter(Class<T> dataClass, ConvertersProvider provider) {
        this.dataClass = dataClass;
        for (Field field : dataClass.getDeclaredFields()) {
            fields.put(field, new FieldConverter(field, provider));
        }
    }

    @Override
    public T convert(JsonNode data) throws ReflectiveOperationException {

        T result = dataClass.newInstance();

        fields.forEach((field, converter) -> {
            try {
                field.set(result, converter.convert(data));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Error", e);
            }
        });

        return result;
    }

    @Override
    public StringBuilder appendQuery(StringBuilder query) {
        fields.forEach((field, converter) -> converter.appendQuery(query).append("\n"));
        return query;
    }
}
