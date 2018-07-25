package ru.citeck.ecos.graphql.journal.record.attribute;

import ru.citeck.ecos.graphql.journal.record.JournalAttributeGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeValueGql;

import java.util.Map;
import java.util.Optional;

/**
 * @author Pavel Simonov
 */
public class JournalAttributeMapValue implements JournalAttributeValueGql {

    private String id;
    private Map<String, Object> attributes;

    public JournalAttributeMapValue(String id) {
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
    public Optional<JournalAttributeGql> attr(String name) {
        Object value = attributes.get(name);
        JournalAttributeGql result;
        if (value instanceof JournalAttributeGql) {
            result = (JournalAttributeGql) value;
        } else {
            result = new JournalExplicitAttribute(name, value);
        }
        return Optional.of(result);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
