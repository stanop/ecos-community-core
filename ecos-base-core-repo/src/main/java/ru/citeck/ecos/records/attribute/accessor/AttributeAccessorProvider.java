package ru.citeck.ecos.records.attribute.accessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.attribute.AttributeAccessor;

import java.util.List;

@Component
public class AttributeAccessorProvider extends AbstractAttAccessorProvider {

    @Override
    public String getId() {
        return "a";
    }

    @Override
    public AttributeAccessor getAccessor(List<String> args, AttributeAccessor next) {
        return new Accessor(args.get(0), args.size() > 1 ? args.get(1) : "", next);
    }

    private static class Accessor implements AttributeAccessor {

        private String name;
        private boolean isMultiple;
        private AttributeAccessor next;

        public Accessor(String name, String isMultiple, AttributeAccessor next) {
            this.name = name;
            this.isMultiple = Boolean.TRUE.toString().equals(isMultiple);
            this.next = next;
        }

        @Override
        public StringBuilder appendSchema(StringBuilder sb) {
            sb.append("att(name:\"")
                    .append(name)
                    .append("\"){val{");
            next.appendSchema(sb);
            return sb.append("}}");
        }

        @Override
        public JsonNode getValue(JsonNode raw, boolean flat) {
            JsonNode values = flat ? raw.path("val") : raw.path("att").path("val");
            if (isMultiple) {
                ArrayNode result = JsonNodeFactory.instance.arrayNode();
                if (values.isArray()) {
                    for (int i = 0; i < values.size(); i++) {
                        result.add(next.getValue(values.get(i), false));
                    }
                }
                return result;
            } else {
                return next.getValue(values.path(0), false);
            }
        }
    }
}
