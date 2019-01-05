package ru.citeck.ecos.records.source.alfnode.meta;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.attr.prov.VirtualScriptAttributes;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.MetaUtils;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.graphql.node.GqlQName;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AlfNodeRecord implements MetaValue {

    private static final String VIRTUAL_SCRIPT_ATTS_ID = "virtualScriptAttributesProvider";

    public static final String ATTR_ASPECTS = "attr:aspects";
    public static final String ATTR_IS_DOCUMENT = "attr:isDocument";
    public static final String ATTR_IS_CONTAINER = "attr:isContainer";
    public static final String ATTR_PARENT = "attr:parent";

    private RecordRef recordRef;
    private GqlAlfNode node;

    public AlfNodeRecord(RecordRef recordRef) {
        this.recordRef = recordRef;
    }

    @Override
    public String getId(GqlContext context) {
        return recordRef.toString();
    }

    @Override
    public String getString(GqlContext context) {
        return getNode(context).displayName();
    }

    private GqlAlfNode getNode(GqlContext context) {
        if (node == null) {
            node = context.getNode(RecordsUtils.toNodeRef(recordRef)).orElse(null);
        }
        return node;
    }

    @Override
    public List<MetaValue> getAttribute(String name, GqlContext context) {

        GqlAlfNode node = getNode(context);

        List<MetaValue> attribute = null;
        if (ATTR_ASPECTS.equals(name)) {
            attribute = node.aspects()
                            .stream()
                            .map(a -> new AlfNodeAttValue(a, context))
                            .collect(Collectors.toList());
        } else if (ATTR_IS_CONTAINER.equals(name)) {
            attribute = MetaUtils.toMetaValues(node.isContainer());
        } else if (ATTR_IS_DOCUMENT.equals(name)) {
            attribute = MetaUtils.toMetaValues(node.isDocument());
        } if (ATTR_PARENT.equals(name)) {
            attribute = Collections.singletonList(new AlfNodeAttValue(node.getParent(), context));
        } else {
            Attribute nodeAtt = node.attribute(name);
            if (Attribute.Type.UNKNOWN.equals(nodeAtt.type())) {
                Optional<QName> attQname = context.getQName(name).map(GqlQName::getQName);
                if (attQname.isPresent()) {
                    VirtualScriptAttributes attributes = context.getService(VIRTUAL_SCRIPT_ATTS_ID);
                    if (attributes != null && attributes.provides(attQname.get())) {
                        Object value = attributes.getAttribute(new NodeRef(node.nodeRef()), attQname.get());
                        attribute = MetaUtils.toMetaValues(value);
                    }
                }
            }
            if (attribute == null) {
                attribute = nodeAtt.getValues()
                                   .stream()
                                   .map(v -> new AlfNodeAttValue(v, context))
                                   .collect(Collectors.toList());
            }
        }
        return attribute;
    }
}

