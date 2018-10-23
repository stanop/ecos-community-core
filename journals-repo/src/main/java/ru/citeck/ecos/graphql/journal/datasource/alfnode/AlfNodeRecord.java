package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.attr.prov.VirtualScriptAttributes;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.record.JGqlAttribute;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeValue;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.graphql.node.GqlQName;

import java.util.Collections;
import java.util.Optional;

public class AlfNodeRecord implements JGqlAttributeValue {

    private static final String VIRTUAL_SCRIPT_ATTS_ID = "virtualScriptAttributesProvider";

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
    public Optional<JGqlAttribute> attr(String name) {
        AlfNodeAttribute attribute = null;
        if (ATTR_ASPECTS.equals(name)) {
            attribute = new AlfNodeAttribute(name, node.aspects(), context);
        } else if (ATTR_IS_CONTAINER.equals(name)) {
            attribute = new AlfNodeAttribute(name, Collections.singletonList(node.isContainer()), context);
        } else if (ATTR_IS_DOCUMENT.equals(name)) {
            attribute = new AlfNodeAttribute(name, Collections.singletonList(node.isDocument()), context);
        } else {
            Attribute nodeAtt = node.attribute(name);
            if (Attribute.Type.UNKNOWN.equals(nodeAtt.type())) {
                Optional<QName> attQname = context.getQName(name).map(GqlQName::getQName);
                if (attQname.isPresent()) {
                    VirtualScriptAttributes attributes = context.getService(VIRTUAL_SCRIPT_ATTS_ID);
                    if (attributes != null && attributes.provides(attQname.get())) {
                        Object value = attributes.getAttribute(new NodeRef(node.nodeRef()), attQname.get());
                        attribute = new AlfNodeAttribute(name, Collections.singletonList(value), context);
                    }
                }
            }
            if (attribute == null) {
                attribute = new AlfNodeAttribute(node.attribute(name), context);
            }
        }
        return Optional.of(attribute);
    }
}

