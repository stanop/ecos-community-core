package ru.citeck.ecos.graphql.journal.record.attribute;

import ru.citeck.ecos.graphql.journal.record.JournalAttributeGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeValueGql;

import java.util.Optional;

/**
 * @author Pavel Simonov
 */
public class JournalAttributeExplicitValue implements JournalAttributeValueGql {

    private Object val;

    public JournalAttributeExplicitValue(Object value) {
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
    public Optional<JournalAttributeGql> attr(String name) {
        return Optional.of(new JournalReflectionAttributeGql(val, name));
    }
}
