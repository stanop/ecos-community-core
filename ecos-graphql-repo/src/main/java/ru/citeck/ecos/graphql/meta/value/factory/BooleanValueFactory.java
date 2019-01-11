package ru.citeck.ecos.graphql.meta.value.factory;

import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.Collections;
import java.util.List;

@Component
public class BooleanValueFactory extends AbstractMetaValueFactory<Boolean> {

    @Override
    public MetaValue getValue(Boolean value) {
        return new MetaValue() {
            @Override
            public String getString() {
                return value.toString();
            }

            @Override
            public Boolean getBool() {
                return value;
            }
        };
    }

    @Override
    public List<Class<? extends Boolean>> getValueTypes() {
        return Collections.singletonList(Boolean.class);
    }
}
