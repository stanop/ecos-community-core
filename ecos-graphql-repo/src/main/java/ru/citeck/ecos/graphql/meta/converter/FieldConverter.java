package ru.citeck.ecos.graphql.meta.converter;

import com.fasterxml.jackson.databind.JsonNode;
import ru.citeck.ecos.graphql.meta.annotation.MetaAtt;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class FieldConverter<T> extends MetaConverter<Object> {

    private Class<Collection<T>> collectionClass;
    private MetaConverter<T> valueConverter;
    private String attName = "";
    private String fieldName;

    public FieldConverter(Field field, ConvertersProvider provider) {

        fieldName = field.getName();

        Class<?> fieldType = field.getType();
        Class<T> valueClass;

        if (Collection.class.isAssignableFrom(fieldType)) {
            if (List.class.isAssignableFrom(fieldType)) {
                collectionClass = (Class<Collection<T>>) (Object) ArrayList.class;
            } else if (Set.class.isAssignableFrom(collectionClass)) {
                collectionClass = (Class<Collection<T>>) (Object) HashSet.class;
            } else {
                throw new RuntimeException("Collection type " + collectionClass + " is not supported");
            }

            ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
            valueClass = (Class<T>) stringListType.getActualTypeArguments()[0];
        } else {

            valueClass = (Class<T>) fieldType;
        }

        MetaAtt attrInfo = field.getAnnotation(MetaAtt.class);

        if (attrInfo != null) {
            attName = attrInfo.name();
            Class<? extends MetaConverter<T>> converterClass = (Class<? extends MetaConverter<T>>) attrInfo.converter();
            if (!converterClass.equals(NotValidConverter.class)) {
                valueConverter = provider.getCustom(converterClass);
            }
        }

        if (attName.isEmpty()) {
            attName = field.getName();
        }
        if (valueConverter == null) {
            valueConverter = provider.getConverter(valueClass);
        }
    }

    @Override
    public Object convert(JsonNode data) throws ReflectiveOperationException {

        JsonNode value = data.get(fieldName);

        if (value != null) {
            value = value.get(META_VAL_FIELD);
        }

        if (collectionClass != null) {
            Collection<T> collection = collectionClass.newInstance();
            if (value != null && value.isArray() && valueConverter != null) {
                for (int i = 0; i < value.size(); i++) {
                    collection.add(valueConverter.convert(value.get(i)));
                }
            }
            return collection;
        } else if (value != null && value.isArray() && value.size() > 0 && valueConverter != null) {
            return valueConverter.convert(value.get(0));
        }

        return null;
    }

    @Override
    public StringBuilder appendQuery(StringBuilder query) {
        query.append(fieldName)
                .append(":att(name:\"")
                .append(attName)
                .append("\"){val{");
        valueConverter.appendQuery(query);
        query.append("}}");
        return query;
    }
}
