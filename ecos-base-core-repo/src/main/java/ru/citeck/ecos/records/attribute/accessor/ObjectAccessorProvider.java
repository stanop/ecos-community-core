package ru.citeck.ecos.records.attribute.accessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.util.Pair;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.attribute.AttributeAccessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ObjectAccessorProvider extends AbstractAttAccessorProvider {

    @Override
    public String getId() {
        return "o";
    }

    private Pair<String, String> getAttribute(String attStr) {
        boolean inQuotes = false;
        boolean nameInQuotes = false;
        for (int i = 0; i < attStr.length(); i++) {
            char ch = attStr.charAt(i);
            if (ch == '\'') {
                nameInQuotes = true;
                inQuotes = !inQuotes;
                continue;
            }
            if (inQuotes) {
                continue;
            }
            if (ch == '(' || ch == '?') {
                break;
            }
            if (ch == ':') {
                String fieldName = nameInQuotes ? attStr.substring(1, i - 1) : attStr.substring(0, i);
                return new Pair<>(fieldName, attStr.substring(i + 1));
            }
        }
        return new Pair<>(attStr, attStr);
    }

    @Override
    public AttributeAccessor getAccessor(List<String> attributes, AttributeAccessor internal) {

        Map<String, AttributeAccessor> accessors = new HashMap<>();
        Map<String, String> internalToExternal = new HashMap<>();

        NameCounter nameCounter = new NameCounter();

        for (String attribute : attributes) {

            Pair<String, String> att = getAttribute(attribute);
            String internalName = nameCounter.incrementAndGet();

            accessors.put(internalName, recordAttributes.getAccessor(att.getSecond()));
            internalToExternal.put(internalName, att.getFirst());
        }

        return new Accessor(accessors, internalToExternal);
    }

    private class Accessor implements AttributeAccessor {

        private final Map<String, AttributeAccessor> accessors;
        private final Map<String, String> internalToExternal;

        public Accessor(Map<String, AttributeAccessor> accessors, Map<String, String> internalToExternal) {
            this.accessors = accessors;
            this.internalToExternal = internalToExternal;
        }

        @Override
        public StringBuilder appendSchema(StringBuilder sb) {
            accessors.forEach((name, acc) -> {
                sb.append(name).append(":");
                acc.appendSchema(sb);
            });
            return sb;
        }

        @Override
        public JsonNode getValue(JsonNode raw, boolean flat) {

            ObjectNode result = JsonNodeFactory.instance.objectNode();

            accessors.forEach((name, acc) ->
                result.put(internalToExternal.get(name), acc.getValue(raw.path(name), true))
            );

            return result;
        }
    }

    private static class NameCounter {

        private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private static final int CHARS_LEN = CHARS.length();

        private int numValue = -1;
        private StringBuilder value = new StringBuilder();

        String incrementAndGet() {

            value.setLength(0);

            int left = ++numValue;
            do {
                value.append(CHARS.charAt(left % CHARS_LEN));
                left /= CHARS_LEN;
            } while (left > 0);

            return value.toString();
        }
    }
}
