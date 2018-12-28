package ru.citeck.ecos.records.attribute.accessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.attribute.AttributeAccessor;

import java.io.IOException;
import java.util.List;

@Component
public class JsonAttAccessorProvider extends AbstractAttAccessorProvider {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getId() {
        return "j";
    }

    @Override
    public AttributeAccessor getAccessor(List<String> args, AttributeAccessor internal) {
        return new Accessor();
    }

    public class Accessor implements AttributeAccessor {

        @Override
        public StringBuilder appendSchema(StringBuilder sb) {
            return sb.append("str");
        }

        @Override
        public JsonNode getValue(JsonNode raw, boolean flat) {
            JsonNode data = flat ? raw : raw.path("str");
            if (data.isTextual()) {
                try {
                    return objectMapper.readTree(data.asText());
                } catch (IOException e) {
                    throw new IllegalArgumentException("Incorrect data");
                }
            }
            return null;
        }
    }
}
