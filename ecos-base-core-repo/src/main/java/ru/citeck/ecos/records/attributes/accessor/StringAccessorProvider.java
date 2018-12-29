package ru.citeck.ecos.records.attributes.accessor;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.attributes.AttributeAccessor;

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
                sb.append("str");
            } else {
                throw new IllegalArgumentException("Not supported");
            }
            return sb;
        }

        @Override
        public JsonNode getValue(JsonNode raw) {
            return raw;
        }
    }
}
/*
* id,
* attributes: o(
*
* )
*
*
* */