package ru.citeck.ecos.records.attributes.accessor;

import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.attributes.AttributeAccessor;

import java.util.List;

@Component
public class ObjectAccessorProvider extends AbstractAttAccessorProvider {

    @Override
    public String getId() {
        return "o";
    }

    @Override
    public AttributeAccessor getAccessor(List<String> attributes, AttributeAccessor internal) {
        return new ObjectAccessor(attributesDAO, attributes);
    }
}
