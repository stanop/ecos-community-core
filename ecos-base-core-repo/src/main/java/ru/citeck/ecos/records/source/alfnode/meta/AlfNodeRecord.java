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
import ru.citeck.ecos.records.RecordConstants;
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
    private GqlContext context;

    public AlfNodeRecord(RecordRef recordRef) {
        this.recordRef = recordRef;
    }

    @Override
    public MetaValue init(GqlContext context) {
        this.context = context;
        this.node = context.getNode(RecordsUtils.toNodeRef(recordRef)).orElse(null);
        return this;
    }

    @Override
    public String getId() {
        return recordRef.toString();
    }

    @Override
    public String getString() {
        return node.displayName();
    }

    @Override
    public boolean hasAttribute(String attributeName) {

        Attribute nodeAtt = node.attribute(attributeName);

        if (Attribute.Type.UNKNOWN.equals(nodeAtt.type())) {
            return false;
        }

        List<?> values = nodeAtt.getValues();

        return values != null && !values.isEmpty();
    }

    @Override
    public Object getAttribute(String name) {

        Object attribute = null;

        switch (name) {
            case ATTR_ASPECTS:

                attribute = node.aspects()
                                .stream()
                                .map(a -> new AlfNodeAttValue(a).init(context))
                                .collect(Collectors.toList());
                break;

            case ATTR_IS_CONTAINER:

                attribute = MetaUtils.toMetaValues(node.isContainer(), context);
                break;

            case ATTR_IS_DOCUMENT:

                attribute = MetaUtils.toMetaValues(node.isDocument(), context);
                break;

            case ATTR_PARENT:
            case RecordConstants.ATT_PARENT:

                attribute = Collections.singletonList(new AlfNodeAttValue(node.getParent()).init(context));
                break;

            case RecordConstants.ATT_FORM_KEY:

                attribute = "alf_" + node.type();
                break;

            default:

                Attribute nodeAtt = node.attribute(name);
                if (Attribute.Type.UNKNOWN.equals(nodeAtt.type())) {
                    Optional<QName> attQname = context.getQName(name).map(GqlQName::getQName);
                    if (attQname.isPresent()) {
                        VirtualScriptAttributes attributes = context.getService(VIRTUAL_SCRIPT_ATTS_ID);
                        if (attributes != null && attributes.provides(attQname.get())) {
                            Object value = attributes.getAttribute(new NodeRef(node.nodeRef()), attQname.get());
                            attribute = MetaUtils.toMetaValues(value, context);
                        }
                    }
                }
                if (attribute == null) {
                    attribute = nodeAtt.getValues()
                                       .stream()
                                       .map(v -> new AlfNodeAttValue(v).init(context))
                                       .collect(Collectors.toList());
                }
        }

        return attribute;
    }
}

