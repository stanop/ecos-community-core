package ru.citeck.ecos.records.source.alfnode.meta;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.attr.prov.VirtualScriptAttributes;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.graphql.node.GqlQName;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AlfNodeRecord implements MetaValue {

    private static final String VIRTUAL_SCRIPT_ATTS_ID = "virtualScriptAttributesProvider";

    public static final String ATTR_ASPECTS = "attr:aspects";
    public static final String ATTR_IS_DOCUMENT = "attr:isDocument";
    public static final String ATTR_IS_CONTAINER = "attr:isContainer";
    public static final String ATTR_PARENT = "attr:parent";

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
    public Optional<MetaAttribute> att(String name) {
        AlfNodeAtt attribute = null;
        if (ATTR_ASPECTS.equals(name)) {
            attribute = new AlfNodeAtt(name, node.aspects(), context);
        } else if (ATTR_IS_CONTAINER.equals(name)) {
            attribute = new AlfNodeAtt(name, Collections.singletonList(node.isContainer()), context);
        } else if (ATTR_IS_DOCUMENT.equals(name)) {
            attribute = new AlfNodeAtt(name, Collections.singletonList(node.isDocument()), context);
        } if (ATTR_PARENT.equals(name)) {
            attribute = new AlfNodeAtt(name, Collections.singletonList(node.getParent()), context);
        } else {
            Attribute nodeAtt = node.attribute(name);
            if (Attribute.Type.UNKNOWN.equals(nodeAtt.type())) {
                Optional<QName> attQname = context.getQName(name).map(GqlQName::getQName);
                if (attQname.isPresent()) {
                    VirtualScriptAttributes attributes = context.getService(VIRTUAL_SCRIPT_ATTS_ID);
                    if (attributes != null && attributes.provides(attQname.get())) {
                        Object value = attributes.getAttribute(new NodeRef(node.nodeRef()), attQname.get());
                        attribute = new AlfNodeAtt(name, Collections.singletonList(value), context);
                    }
                }
            }
            if (attribute == null) {
                attribute = new AlfNodeAtt(nodeAtt, context);
            }
        }
        return Optional.of(attribute);
    }

    @Override
    public List<MetaAttribute> atts(String filter) {
        return Collections.emptyList();
    }
}

