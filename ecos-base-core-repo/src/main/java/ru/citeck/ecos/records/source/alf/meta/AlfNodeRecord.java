package ru.citeck.ecos.records.source.alf.meta;

import lombok.Getter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.attr.prov.VirtualScriptAttributes;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.records.meta.MetaUtils;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.graphql.node.GqlQName;
import ru.citeck.ecos.records.RecordConstants;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records.source.alf.AlfNodeMetaEdge;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.graphql.GqlContext;
import ru.citeck.ecos.records2.graphql.meta.value.MetaEdge;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;

import java.util.*;
import java.util.stream.Collectors;

public class AlfNodeRecord implements MetaValue {

    private static final String VIRTUAL_SCRIPT_ATTS_ID = "virtualScriptAttributesProvider";

    public static final String ATTR_ASPECTS = "attr:aspects";
    public static final String ATTR_IS_DOCUMENT = "attr:isDocument";
    public static final String ATTR_IS_CONTAINER = "attr:isContainer";
    public static final String ATTR_PARENT = "attr:parent";
    public static final String ATTR_PERMISSIONS = "permissions";

    private NodeRef nodeRef;
    private RecordRef recordRef;
    private GqlAlfNode node;
    private AlfGqlContext context;

    @Getter(lazy = true)
    private final Permissions permissions = new Permissions();

    public AlfNodeRecord(RecordRef recordRef) {
        this.recordRef = recordRef;
    }

    @Override
    public <T extends GqlContext> void init(T context) {
        this.context = (AlfGqlContext) context;
        this.nodeRef = RecordsUtils.toNodeRef(recordRef);
        this.node = this.context.getNode(nodeRef).orElse(null);
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
    public boolean has(String name) {

        Attribute nodeAtt = node.attribute(name);

        if (Attribute.Type.UNKNOWN.equals(nodeAtt.type())) {
            return false;
        }

        List<?> values = nodeAtt.getValues();

        return values != null && !values.isEmpty();
    }

    @Override
    public List<? extends MetaValue> getAttribute(String name) {

       List<? extends MetaValue> attribute = null;

        switch (name) {
            case ATTR_ASPECTS:

                attribute = node.aspects()
                                .stream()
                                .map(this::toAlfNodeAtt)
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

                AlfNodeAttValue parentValue = new AlfNodeAttValue(node.getParent());
                parentValue.init(context);
                attribute = Collections.singletonList(parentValue);

                break;

            case RecordConstants.ATT_FORM_KEY:

                attribute = Collections.singletonList(new AlfNodeAttValue("alf_" + node.type()));
                break;

            case RecordConstants.ATT_VIEW_FORM_KEY:

                attribute = Collections.singletonList(new AlfNodeAttValue("alf_" + node.type() + "_view"));
                break;

            case ATTR_PERMISSIONS:

                return Collections.singletonList(getPermissions());

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
                                       .map(v -> toAlfNodeAtt(nodeAtt, v))
                                       .collect(Collectors.toList());
                }
        }

        return attribute != null ? attribute : Collections.emptyList();
    }

    @Override
    public MetaEdge getEdge(String name) {
        return new AlfNodeMetaEdge(context, name, this);
    }

    private MetaValue toAlfNodeAtt(Attribute att, Object value) {
        MetaValue result = new AlfNodeAttValue(att, value);
        result.init(context);
        return result;
    }

    private MetaValue toAlfNodeAtt(Object value) {
        MetaValue result = new AlfNodeAttValue(value);
        result.init(context);
        return result;
    }

    public class Permissions implements MetaValue {

        @Override
        public String getString() {
            return null;
        }

        @Override
        public boolean has(String permission) {
            if (nodeRef == null) {
                return false;
            }
            PermissionService permissionService = context.getServiceRegistry().getPermissionService();
            AccessStatus accessStatus = permissionService.hasPermission(nodeRef, permission);
            return AccessStatus.ALLOWED.equals(accessStatus);
        }
    }
}

