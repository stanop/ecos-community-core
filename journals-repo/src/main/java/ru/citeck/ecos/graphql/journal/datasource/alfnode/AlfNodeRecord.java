package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeValueGql;
import ru.citeck.ecos.graphql.node.GqlAlfNode;

import java.util.Collections;
import java.util.Optional;

public class AlfNodeRecord implements JournalAttributeValueGql {

    public static final String ATTR_ASPECTS = "attr:aspects";
    public static final String ATTR_IS_DOCUMENT = "attr:isDocument";
    public static final String ATTR_IS_CONTAINER = "attr:isContainer";

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
    public String str() {
        return node.displayName();
    }

    @Override
    public Optional<JournalAttributeGql> attr(String name) {
        AlfNodeAttribute attribute;
        if (ATTR_ASPECTS.equals(name)) {
            attribute = new AlfNodeAttribute(name, node.aspects(), context);
        } else if (ATTR_IS_CONTAINER.equals(name)) {
            attribute = new AlfNodeAttribute(name, Collections.singletonList(node.isContainer()), context);
        } else if (ATTR_IS_DOCUMENT.equals(name)) {
            attribute = new AlfNodeAttribute(name, Collections.singletonList(node.isDocument()), context);
        } else {
            attribute = new AlfNodeAttribute(node.attribute(name), context);
        }
        return Optional.of(attribute);
    }
}

