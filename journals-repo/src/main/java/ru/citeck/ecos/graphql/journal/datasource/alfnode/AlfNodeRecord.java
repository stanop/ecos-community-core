package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeValueGql;
import ru.citeck.ecos.graphql.node.GqlAlfNode;

import java.util.Optional;

public class AlfNodeRecord implements JournalAttributeValueGql {

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
    public String disp() {
        return node.displayName();
    }

    @Override
    public Optional<JournalAttributeGql> attr(String name) {
        return Optional.of(new AlfNodeAttribute(node.attribute(name), context));
    }
}

