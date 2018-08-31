package ru.citeck.ecos.graphql.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.ExecutionResult;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.meta.converter.ConvertersProvider;
import ru.citeck.ecos.graphql.meta.converter.MetaConverter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class GqlMetaUtils {

    private ConvertersProvider convertersProvider = new ConvertersProvider();
    private ObjectMapper objectMapper = new ObjectMapper();

    public GqlQuery createQuery(String queryBase, Collection<String> ids, String schema) {

        Map<String, String> keysMapping = new HashMap<>();

        StringBuilder query = new StringBuilder("{");
        int idx = 0;
        for (String id : ids) {
            String key = "a" + idx++;
            keysMapping.put(key, id);
            query.append(key)
                    .append(":")
                    .append(String.format(queryBase, id))
                    .append("{...meta}\n");
        }
        query.append("}");

        query.append("fragment meta on MetaValue {").append(schema).append("}");

        return new GqlQuery(query.toString(), keysMapping);
    }

    public GqlQuery createQuery(String queryBase, Collection<String> ids, Class<?> metaClass) {
        MetaConverter<?> converter = convertersProvider.getConverter(metaClass);
        String schema = converter.appendQuery(new StringBuilder()).toString();
        return createQuery(queryBase, ids, schema);
    }

    public Map<String, ObjectNode> convertMeta(GqlQuery query, ExecutionResult executionResult) {

        JsonNode jsonNode = objectMapper.valueToTree(executionResult.getData());

        Map<String, ObjectNode> result = new HashMap<>();
        query.keysMapping.forEach((key, id) -> result.put(id, (ObjectNode) jsonNode.get(key)));

        return result;
    }

    public <V> Map<String, V> convertMeta(GqlQuery query, ExecutionResult executionResult, Class<V> metaClass) {

        MetaConverter<V> converter = convertersProvider.getConverter(metaClass);

        Map<String, ObjectNode> meta = convertMeta(query, executionResult);
        Map<String, V> result = new HashMap<>();

        meta.forEach((id, value) -> {
            try {
                result.put(id, converter.convert(value));
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                result.put(id, null);
            }
        });

        return result;
    }

    public static class GqlQuery {

        private final String query;
        private final Map<String, String> keysMapping;

        GqlQuery(String query, Map<String, String> keysMapping) {
            this.query = query;
            this.keysMapping = keysMapping;
        }

        public String getQuery() {
            return query;
        }
    }
}
