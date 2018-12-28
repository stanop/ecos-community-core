package ru.citeck.ecos.graphql.meta;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface GraphQLMetaService {

    List<ObjectNode> getMeta(Function<GqlContext, List<MetaValue>> valuesProvider, String schema);

    <T> List<ObjectNode> getMeta(List<T> values, BiFunction<T, GqlContext, MetaValue> converter, String schema);

    List<ObjectNode> getMeta(List<MetaValue> valuesProvider, String schema);

    <V> List<V> convertMeta(List<ObjectNode> meta, Class<V> metaClass);

    String createSchema(Class<?> metaClass);
}
