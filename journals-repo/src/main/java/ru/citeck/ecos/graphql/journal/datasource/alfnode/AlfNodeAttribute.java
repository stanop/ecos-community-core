package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeValueGql;
import ru.citeck.ecos.graphql.node.Attribute;

import java.util.List;
import java.util.stream.Collectors;

public class AlfNodeAttribute implements JournalAttributeGql {

    private String name;
    private Attribute attribute;
    private GqlContext context;
    private List<?> values;

    public AlfNodeAttribute(Attribute attribute, GqlContext context) {
        this.attribute = attribute;
        this.context = context;
        this.name = attribute.name();
    }

    public AlfNodeAttribute(String name, List<?> values, GqlContext context) {
        this.name = name;
        this.values = values;
        this.context = context;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<JournalAttributeValueGql> val() {
        if (values == null) {
            values = attribute.getValues();
        }
        return values.stream()
                     .map(v -> new AlfNodeAttributeValue(v, context))
                     .collect(Collectors.toList());
    }
}
