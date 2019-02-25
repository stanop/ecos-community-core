package ru.citeck.ecos.graphql.meta.value.factory;

import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.Collections;
import java.util.List;

@Component
public class DoubleValueFactory extends AbstractMetaValueFactory<Double> {

    @Override
    public MetaValue getValue(Double value) {
        return new MetaValue() {
            @Override
            public String getString() {
                return value.toString();
            }

            @Override
            public Double getDouble() {
                return value;
            }

            @Override
            public Boolean getBool() {
                return value != 0;
            }
        };
    }

    @Override
    public List<Class<? extends Double>> getValueTypes() {
        return Collections.singletonList(Double.class);
    }
}
