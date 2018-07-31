package ru.citeck.ecos.graphql.journal.record.attribute;

import ru.citeck.ecos.graphql.journal.record.JGqlAttribute;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeValue;

import java.util.Optional;

/**
 * @author Pavel Simonov
 */
public class JGqlAttributeExplicitValue implements JGqlAttributeValue {

    private Object val;

    public JGqlAttributeExplicitValue(Object value) {
        if (value instanceof Optional) {
            val = ((Optional<?>) value).orElse(null);
        } else {
            val = value;
        }
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public String str() {
        return val != null ? val.toString() : null;
    }

    @Override
    public Optional<JGqlAttribute> attr(String name) {
        return Optional.of(new JGqlReflectionAttributeGql(val, name));
    }
}
