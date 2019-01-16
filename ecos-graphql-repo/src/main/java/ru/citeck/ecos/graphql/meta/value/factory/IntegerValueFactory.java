package ru.citeck.ecos.graphql.meta.value.factory;

import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.Collections;
import java.util.List;

@Component
public class IntegerValueFactory extends AbstractMetaValueFactory<Integer> {

    @Override
    public MetaValue getValue(Integer value) {
        return new MetaValue() {
            @Override
            public String getString() {
                return value.toString();
            }

            @Override
            public Double getDouble() {
                return Double.valueOf(value);
            }

            @Override
            public Boolean getBool() {
                return value != 0;
            }
        };
    }

    @Override
    public List<Class<? extends Integer>> getValueTypes() {
        return Collections.singletonList(Integer.class);
    }
}
