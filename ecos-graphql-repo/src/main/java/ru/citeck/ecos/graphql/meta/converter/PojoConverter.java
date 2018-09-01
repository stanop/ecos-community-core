package ru.citeck.ecos.graphql.meta.converter;

import com.fasterxml.jackson.databind.JsonNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PojoConverter<T> extends MetaConverter<T> {

    private Class<T> dataClass;

    private Field idField;
    private Map<Field, FieldConverter<?>> fields = new ConcurrentHashMap<>();

    PojoConverter(Class<T> dataClass, ConvertersProvider provider) {
        this.dataClass = dataClass;
        for (Field field : dataClass.getDeclaredFields()) {
            if (field.getName().startsWith("this$")) {
                continue;
            }
            if (field.getName().equals("id")) {
                idField = field;
            } else {
                fields.put(field, new FieldConverter(field, provider));
            }
        }
    }

    @Override
    public T convert(JsonNode data) throws ReflectiveOperationException {

        T result = dataClass.newInstance();

        if (idField != null) {
            String str = data.get("id").asText();
            if (StringUtils.isNotEmpty(str)) {
                if (NodeRef.class.isAssignableFrom(idField.getType())) {
                    if (NodeRef.isNodeRef(str)) {
                        idField.set(result, new NodeRef(str));
                    }
                } else {
                    idField.set(result, str);
                }
            }
        }

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
        if (idField != null) {
            query.append("id\n");
        }
        fields.forEach((field, converter) -> converter.appendQuery(query).append("\n"));
        return query;
    }
}
