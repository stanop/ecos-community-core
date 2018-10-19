package ru.citeck.ecos.graphql.meta.value;

import org.alfresco.util.ParameterCheck;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MetaIdValue implements MetaValue {

    private String id;

    public MetaIdValue(Object id) {
        ParameterCheck.mandatory("id", id);
        this.id = id.toString();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String str() {
        return id;
    }

    @Override
    public Optional<MetaAttribute> att(String name) {
        return Optional.empty();
    }

    @Override
    public List<MetaAttribute> atts(String filter) {
        return Collections.emptyList();
    }
}
