package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeGql;
import ru.citeck.ecos.graphql.journal.record.JournalRecordGql;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;

import java.util.List;
import java.util.stream.Collectors;

public class AlfNodeRecord implements JournalRecordGql {

    private GqlAlfNode node;
    private GqlContext context;

    public AlfNodeRecord(GqlAlfNode node, GqlContext context) {
        this.context = context;
        this.node = node;
    }

    @Override
    public String id() {
        return node.nodeRef();
    }

    @Override
    public List<JournalAttributeGql> attributes(List<String> names) {
        List<Attribute> attributes = node.attributes(null, names);
        return attributes.stream()
                         .map(a -> new AlfNodeAttribute(a, context))
                         .collect(Collectors.toList());
    }

    @Override
    public JournalAttributeGql attribute(String name) {
        return new AlfNodeAttribute(node.attribute(name), context);
    }
}

