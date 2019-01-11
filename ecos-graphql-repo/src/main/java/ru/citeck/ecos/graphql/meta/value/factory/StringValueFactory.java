package ru.citeck.ecos.graphql.meta.value.factory;

import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.Collections;
import java.util.List;

@Component
public class StringValueFactory extends AbstractMetaValueFactory<String> {

    @Override
    public MetaValue getValue(String value) {
        return new MetaValue() {
            @Override
            public String getString() {
                return value;
            }

            @Override
            public Boolean getBool() {
                return Boolean.TRUE.toString().equals(value);
            }

            @Override
            public Double getDouble() {
                return Double.parseDouble(value);
            }
        };
    }

    @Override
    public List<Class<? extends String>> getValueTypes() {
        return Collections.singletonList(String.class);
    }
}
