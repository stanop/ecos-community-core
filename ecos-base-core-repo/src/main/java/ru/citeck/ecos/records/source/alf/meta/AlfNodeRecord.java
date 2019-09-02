package ru.citeck.ecos.records.source.alf.meta;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.attr.prov.VirtualScriptAttributes;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.node.AlfNodeContentPathRegistry;
import ru.citeck.ecos.node.AlfNodeInfo;
import ru.citeck.ecos.node.DisplayNameService;
import ru.citeck.ecos.records.meta.MetaUtils;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.graphql.node.GqlQName;
import ru.citeck.ecos.records.source.alf.file.FileRepresentation;
import ru.citeck.ecos.records.source.common.MLTextValue;
import ru.citeck.ecos.records2.QueryContext;
import ru.citeck.ecos.records2.RecordConstants;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records.source.alf.AlfNodeMetaEdge;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.graphql.meta.value.MetaEdge;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.state.ItemsUpdateState;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class AlfNodeRecord implements MetaValue {

    private static final String VIRTUAL_SCRIPT_ATTS_ID = "virtualScriptAttributesProvider";
    private static final String DEFAULT_VERSION_LABEL = "1.0";

    public static final String ATTR_ASPECTS = "attr:aspects";
    public static final String ATTR_IS_DOCUMENT = "attr:isDocument";
    public static final String ATTR_IS_CONTAINER = "attr:isContainer";
    public static final String ATTR_PARENT = "attr:parent";
    public static final String ATTR_PERMISSIONS = "permissions";
    public static final String ATTR_PENDING_UPDATE = "pendingUpdate";
    public static final String ATTR_VERSION = "version";

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
    public <T extends QueryContext> void init(T context, MetaField field) {
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
        return node.nodeRef();
    }

    @Override
    public String getDisplayName() {
        DisplayNameService displayNameService = context.getService(DisplayNameService.QNAME);
        return displayNameService.getDisplayName(new NodeInfo());
    }

    @Override
    public boolean has(String name) {

        if ("_content".equals(name)) {
            AlfNodeContentPathRegistry contentPath = context.getService(AlfNodeContentPathRegistry.QNAME);
            String path = contentPath.getContentPath(new NodeInfo());
            if (path == null) {
                path = "cm:content";
            }
            RecordsService recordsService = context.getRecordsService();
            if (recordsService == null) {
                return false;
            }
            if (path.indexOf('.') == -1) {
                if ("_content".equals(path)) {
                    return false;
                }
                return has(path);
            }
            String query = AlfNodeUtils.resolveHasContentPathQuery(path);
            return Boolean.TRUE.toString().equals(recordsService.getAttribute(recordRef, query).asText());
        }

        Attribute nodeAtt = node.attribute(name);

        if (Attribute.Type.UNKNOWN.equals(nodeAtt.type())) {
            return false;
        }

        List<?> values = nodeAtt.getValues();

        return values != null && !values.isEmpty();
    }

    @Override
    public List<? extends MetaValue> getAttribute(String name, MetaField field) {

        if (node == null) {
            return Collections.emptyList();
        }

        List<? extends MetaValue> attribute = null;

        switch (name) {

            case RecordConstants.ATT_TYPE:

                attribute = MetaUtils.toMetaValues(node.type(), context, field);
                break;

            case ATTR_ASPECTS:

                attribute = node.aspects()
                                .stream()
                                .map(o -> toMetaValue(null, o, field))
                                .collect(Collectors.toList());
                break;

            case ATTR_IS_CONTAINER:

                attribute = MetaUtils.toMetaValues(node.isContainer(), context, field);
                break;

            case ATTR_IS_DOCUMENT:

                attribute = MetaUtils.toMetaValues(node.isDocument(), context, field);
                break;

            case ATTR_PARENT:
            case RecordConstants.ATT_PARENT:

                AlfNodeAttValue parentValue = new AlfNodeAttValue(node.getParent());
                parentValue.init(context, field);
                attribute = Collections.singletonList(parentValue);

                break;

            case RecordConstants.ATT_FORM_KEY:
            case RecordConstants.ATT_DASHBOARD_KEY:

                attribute = MetaUtils.toMetaValues(getFormAndDashboardKeys(), context, field);
                break;

            case RecordConstants.ATT_DASHBOARD_TYPE:

                attribute = Collections.singletonList(new AlfNodeAttValue("case-details"));
                break;

            case ATTR_PERMISSIONS:

                return Collections.singletonList(getPermissions());

            case "previewInfo":

                AlfNodeContentPathRegistry contentPath = context.getService(AlfNodeContentPathRegistry.QNAME);
                String path = contentPath.getContentPath(new NodeInfo());
                RecordsService recordsService = context.getRecordsService();
                if (recordsService == null) {
                    return null;
                }
                JsonNode previewInfo = recordsService.getAttribute(recordRef, path + ".previewInfo?json");
                return MetaUtils.toMetaValues(previewInfo, context, field);

            case ATTR_PENDING_UPDATE:

                ItemsUpdateState service = context.getService("ecos.itemsUpdateState");
                boolean pendingUpdate = service.isPendingUpdate(new NodeRef(node.nodeRef()));
                attribute = Collections.singletonList(toMetaValue(null, pendingUpdate, field));
                break;

            case ATTR_VERSION:

                VersionService versionService = context.getServiceRegistry().getVersionService();
                Version currentVersion = versionService.getCurrentVersion(new NodeRef(node.nodeRef()));
                String versionLabel = currentVersion != null && StringUtils.isNotBlank(currentVersion.getVersionLabel())
                        ? currentVersion.getVersionLabel() : DEFAULT_VERSION_LABEL;
                attribute = Collections.singletonList(toMetaValue(null, versionLabel, field));
                break;

            default:

                Attribute nodeAtt = node.attribute(name);
                if (Attribute.Type.UNKNOWN.equals(nodeAtt.type())) {
                    Optional<QName> attQname = context.getQName(name).map(GqlQName::getQName);
                    if (attQname.isPresent()) {
                        VirtualScriptAttributes attributes = context.getService(VIRTUAL_SCRIPT_ATTS_ID);
                        if (attributes != null && attributes.provides(attQname.get())) {
                            Object value = attributes.getAttribute(new NodeRef(node.nodeRef()), attQname.get());
                            attribute = MetaUtils.toMetaValues(value, context, field);
                        }
                    }
                }
                if (attribute == null) {
                    attribute = nodeAtt.getValues()
                            .stream()
                            .map(v -> toMetaValue(nodeAtt, v, field))
                            .collect(Collectors.toList());
                }
        }

        return attribute != null ? attribute : Collections.emptyList();
    }

    @Override
    public Object getAs(String type) {
        if (node != null) {
            return FileRepresentation.fromAlfNode(node, context);
        }
        return null;
    }

    private List<KeyWithDisp> getFormAndDashboardKeys() {

        List<KeyWithDisp> keys = new ArrayList<>();

        NodeRef type = getNodeRefFromProp("tk:type");

        if (type != null) {
            String typeTitle = getNodeRefDisplayName(type);

            NodeRef kind = getNodeRefFromProp("tk:kind");
            if (kind != null) {

                String kindTitle = getNodeRefDisplayName(kind);

                String value = String.format("%s/%s", type.getId(), kind.getId());
                String disp = String.format("%s - %s", typeTitle, kindTitle);

                keys.add(new KeyWithDisp(value, disp));
            }

            keys.add(new KeyWithDisp(type.getId(), typeTitle));
        }

        String alfTypeKey = "alf_" + node.type();
        String alfTypeTitle = node.typeQName().map(GqlQName::classTitle).orElse(alfTypeKey);
        alfTypeTitle = "A: " + alfTypeTitle;

        keys.add(new KeyWithDisp(alfTypeKey, alfTypeTitle));

        return keys;
    }

    private String getNodeRefDisplayName(NodeRef nodeRef) {
        if (nodeRef == null) {
            return "null";
        }
        RecordRef ref = RecordRef.create("", nodeRef.toString());
        JsonNode value = context.getRecordsService().getAttribute(ref, ".disp");
        return value != null ? value.asText() : nodeRef.getId();
    }

    private NodeRef getNodeRefFromProp(String propName) {
        Attribute att = node.attribute(propName);
        String value = null;
        if (att != null) {
            value = att.value().orElse(null);
        }
        return value != null && NodeRef.isNodeRef(value) ? new NodeRef(value) : null;
    }

    @Override
    public MetaEdge getEdge(String name, MetaField field) {
        QName type = null;
        if (node != null) {
            type = node.getType();
        }
        return new AlfNodeMetaEdge(context, type, name, this);
    }

    private MetaValue toMetaValue(Attribute att, Object value, MetaField field) {
        MetaValue metaValue;
        if (value instanceof NodeRef) {
            metaValue = new AlfNodeRecord(RecordRef.valueOf(value.toString()));
        } else if (value instanceof MLText) {
            metaValue = new MLTextValue((MLText) value);
        } else {
            if (att != null) {
                metaValue = new AlfNodeAttValue(att, value);
            } else {
                metaValue = new AlfNodeAttValue(value);
            }
        }
        metaValue.init(context, field);
        return metaValue;
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

    public class NodeInfo implements AlfNodeInfo {

        @Override
        public QName getType() {
            return node.getType();
        }

        @Override
        public NodeRef getNodeRef() {
            return new NodeRef(node.nodeRef());
        }

        @Override
        public Map<QName, Serializable> getProperties() {

            Map<QName, Serializable> props = node.getProperties();

            if (MLPropertyInterceptor.isMLAware()) {
                return props;
            }
            Map<QName, Serializable> result = new HashMap<>();

            for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
                Serializable value = entry.getValue();
                if (value instanceof MLText) {
                    result.put(entry.getKey(), ((MLText) value).getClosestValue(I18NUtil.getLocale()));
                } else {
                    result.put(entry.getKey(), value);
                }
            }

            return result;
        }
    }

    public static class KeyWithDisp implements MetaValue {

        String value;
        String disp;

        public KeyWithDisp(String value, String disp) {
            this.value = value;
            this.disp = disp;
        }

        @Override
        public String getString() {
            return value;
        }

        @Override
        public String getDisplayName() {
            return disp;
        }
    }
}

