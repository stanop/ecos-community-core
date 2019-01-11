package ru.citeck.ecos.graphql.meta;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public interface GraphQLMetaService {

    List<ObjectNode> getEmpty(List<?> ids, String schema);

    List<ObjectNode> getMeta(List<?> values, String schema);

    <V> List<V> convertMeta(List<ObjectNode> meta, Class<V> metaClass);

    String createSchema(Class<?> metaClass);
}
