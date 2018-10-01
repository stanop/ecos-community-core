package ru.citeck.ecos.graphql.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.meta.converter.ConvertersProvider;
import ru.citeck.ecos.graphql.meta.converter.MetaConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GqlMetaUtils {

    private static final String META_KEY = "meta";
    private static final String QUERY_TEMPLATE = "{" + META_KEY + ":%s{%s}}";

    private ConvertersProvider convertersProvider = new ConvertersProvider();
    private ObjectMapper objectMapper = new ObjectMapper();

    public <K> String createQuery(String queryBase, List<K> ids, String schema) {
        List<String> strIds = ids.stream().map(Object::toString).collect(Collectors.toList());
        String baseWithId = String.format(queryBase, String.join("\",\"", strIds));
        return String.format(QUERY_TEMPLATE, baseWithId, schema);
    }

    public String createSchema(Class<?> metaClass) {
        MetaConverter<?> converter = convertersProvider.getConverter(metaClass);
        return converter.appendQuery(new StringBuilder()).toString();
    }

    public <K> Map<K, JsonNode> convertMeta(List<K> ids, ExecutionResult executionResult) {

        Map<K, JsonNode> result = new HashMap<>();

        if (executionResult == null) {
            for (K id : ids) {
                result.put(id, null);
            }
        } else {
            JsonNode jsonNode = objectMapper.valueToTree(executionResult.getData());
            JsonNode meta = jsonNode.get(META_KEY);

            for (int i = 0; i < meta.size(); i++) {
                result.put(ids.get(i), meta.get(i));
            }
        }
        return result;
    }

    public <K, V> Map<K, V> convertMeta(Map<K, JsonNode> meta, Class<V> metaClass) {

        MetaConverter<V> converter = convertersProvider.getConverter(metaClass);

        Map<K, V> result = new HashMap<>();

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

}
