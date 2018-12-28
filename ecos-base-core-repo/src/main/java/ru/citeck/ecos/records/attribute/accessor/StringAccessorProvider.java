package ru.citeck.ecos.records.attribute.accessor;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.attribute.AttributeAccessor;

import java.util.List;

@Component
public class StringAccessorProvider extends AbstractAttAccessorProvider {

    @Override
    public String getId() {
        return "s";
    }

    @Override
    public AttributeAccessor getAccessor(List<String> args, AttributeAccessor internal) {
        return new Accessor(internal);
    }

    class Accessor implements AttributeAccessor {

        private final AttributeAccessor internal;

        public Accessor(AttributeAccessor internal) {
            this.internal = internal;
        }

        @Override
        public StringBuilder appendSchema(StringBuilder sb) {
            if (internal == null) {
                sb.append("s");
            } else {
                throw new IllegalArgumentException("Not supported");
            }
            return sb;
        }

        @Override
        public JsonNode getValue(JsonNode raw, boolean flat) {
            return flat ? raw : raw.path("s");
        }
    }
}
