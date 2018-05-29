package ru.citeck.ecos.graphql.journal.record.attribute;

import ru.citeck.ecos.graphql.journal.record.JournalAttributeGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeValueGql;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pavel Simonov
 */
public class JournalExplicitAttribute implements JournalAttributeGql {

    private String name;
    private List<JournalAttributeValueGql> values;

    public JournalExplicitAttribute(String name, Object value) {
        this.name = name;
        if (value instanceof Collection) {
            values = ((Collection<?>) value).stream()
                                            .map(JournalAttributeExplicitValue::new)
                                            .collect(Collectors.toList());
        } else {
            values = Collections.singletonList(new JournalAttributeExplicitValue(value));
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<JournalAttributeValueGql> val() {
        return values;
    }
}
