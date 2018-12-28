package ru.citeck.ecos.records.attribute.accessor;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.attribute.AttributeAccessor;

import java.util.List;

@Component
public class IdAttributeAccessorProvider extends AbstractAttAccessorProvider {

    @Override
    public String getId() {
        return "id";
    }

    @Override
    public AttributeAccessor getAccessor(List<String> args, AttributeAccessor internal) {
        return new Accessor();
    }

    public class Accessor implements AttributeAccessor {

        @Override
        public StringBuilder appendSchema(StringBuilder sb) {
            return sb.append("id\n");
        }

        @Override
        public JsonNode getValue(JsonNode raw, boolean flat) {
            return flat ? raw : raw.path("id");
        }
    }
}
