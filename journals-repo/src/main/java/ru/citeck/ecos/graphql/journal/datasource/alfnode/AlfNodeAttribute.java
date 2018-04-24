package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeGql;
import ru.citeck.ecos.graphql.journal.record.JournalRecordGql;
import ru.citeck.ecos.graphql.node.Attribute;

import java.util.List;
import java.util.stream.Collectors;

public class AlfNodeAttribute implements JournalAttributeGql {

    private Attribute attribute;
    private GqlContext context;

    public AlfNodeAttribute(Attribute attribute, GqlContext context) {
        this.attribute = attribute;
        this.context = context;
    }

    @Override
    public String name() {
        return attribute.name();
    }

    @Override
    public List<String> values() {
        return attribute.values();
    }

    @Override
    public List<JournalRecordGql> records() {
        return attribute.nodes()
                        .stream()
                        .map(n -> new AlfNodeRecord(n, context))
                        .collect(Collectors.toList());
    }
}
