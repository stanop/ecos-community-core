package ru.citeck.ecos.graphql.journal.record.attribute;

import ru.citeck.ecos.graphql.journal.record.JGqlAttribute;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pavel Simonov
 */
public class JGqlExplicitAttribute implements JGqlAttribute {

    private String name;
    private List<JGqlAttributeValue> values;

    public JGqlExplicitAttribute(String name, Object value) {
        this.name = name;
        if (value instanceof Collection) {
            values = ((Collection<?>) value).stream()
                                            .map(JGqlAttributeExplicitValue::new)
                                            .collect(Collectors.toList());
        } else {
            values = Collections.singletonList(new JGqlAttributeExplicitValue(value));
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<JGqlAttributeValue> val() {
        return values;
    }
}
