package ru.citeck.ecos.graphql.journal.record.attribute;

import ru.citeck.ecos.graphql.journal.record.JGqlAttribute;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeValue;

import java.util.Map;
import java.util.Optional;

/**
 * @author Pavel Simonov
 */
public class JGqlAttributeMapValue implements JGqlAttributeValue {

    private String id;
    private Map<String, Object> attributes;

    public JGqlAttributeMapValue(String id) {
        this.id = id;
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
    public Optional<JGqlAttribute> attr(String name) {
        Object value = attributes.get(name);
        JGqlAttribute result;
        if (value instanceof JGqlAttribute) {
            result = (JGqlAttribute) value;
        } else {
            result = new JGqlExplicitAttribute(name, value);
        }
        return Optional.of(result);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
