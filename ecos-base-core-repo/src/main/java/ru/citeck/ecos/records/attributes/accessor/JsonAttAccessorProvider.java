package ru.citeck.ecos.records.attributes.accessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.attributes.AttributeAccessor;

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

    public class Accessor implements AttributeAccessor<JsonNode> {

        @Override
        public StringBuilder appendSchema(StringBuilder sb) {
            return sb.append("j:str");
        }

        @Override
        public JsonNode getValue(JsonNode raw) {
            if (raw.isTextual()) {
                try {
                    return objectMapper.readTree(raw.asText());
                } catch (IOException e) {
                    throw new IllegalArgumentException("Incorrect data");
                }
            }
            return null;
        }
    }
}
