package ru.citeck.ecos.graphql.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import graphql.ExecutionResult;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.meta.converter.ConvertersProvider;
import ru.citeck.ecos.graphql.meta.converter.MetaConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GqlMetaUtils {

    private static final String META_KEY = "meta";
    private static final String QUERY_TEMPLATE = "{" + META_KEY + ":%s{%s}}";

    private ConvertersProvider convertersProvider = new ConvertersProvider();
    private ObjectMapper objectMapper = new ObjectMapper();

    public <K> String createQuery(String queryBase, String schema) {
        return String.format(QUERY_TEMPLATE, queryBase, schema);
    }

    public <K> String createQuery(String queryBase, List<K> ids, String schema) {
        List<String> strIds = ids.stream().map(Object::toString).collect(Collectors.toList());
        String baseWithId = String.format(queryBase, String.join("\",\"", strIds));
        return String.format(QUERY_TEMPLATE, baseWithId, schema);
    }

    public String createSchema(Class<?> metaClass) {
        MetaConverter<?> converter = convertersProvider.getConverter(metaClass);
        return converter.appendQuery(new StringBuilder()).toString();
    }

    public <K> List<ObjectNode> convertMeta(ExecutionResult executionResult) {
        return convertMeta(Collections.emptyList(), executionResult);
    }

    public <K> List<ObjectNode> convertMeta(List<K> ids, ExecutionResult executionResult) {

        List<ObjectNode> result = new ArrayList<>();

        if (executionResult == null || executionResult.getData() == null) {
            for (K id : ids) {
                ObjectNode node = JsonNodeFactory.instance.objectNode();
                node.set("id", TextNode.valueOf(String.valueOf(id)));
                result.add(node);
            }
        } else {
            JsonNode jsonNode = objectMapper.valueToTree(executionResult.getData());
            JsonNode meta = jsonNode.get(META_KEY);

            for (int i = 0; i < meta.size(); i++) {
                JsonNode metaNode = meta.get(i);
                result.add(metaNode instanceof NullNode ? null : (ObjectNode) metaNode);
            }
        }
        return result;
    }

    public <V> List<V> convertMeta(List<ObjectNode> meta, Class<V> metaClass) {

        MetaConverter<V> converter = convertersProvider.getConverter(metaClass);

        return meta.stream().map(m -> {
            try {
                return converter.convert(m);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Meta receiving error", e);
            }
        }).collect(Collectors.toList());
    }

}
