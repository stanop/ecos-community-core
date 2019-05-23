package ru.citeck.ecos.records.source.alf.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.node.AlfNodeInfo;
import ru.citeck.ecos.node.DisplayNameService;
import ru.citeck.ecos.records.meta.MetaUtils;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.graphql.node.GqlQName;
import ru.citeck.ecos.records2.graphql.GqlContext;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.utils.DictUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class AlfNodeAttValue implements MetaValue {

    private Attribute att;

    private Object rawValue;
    private GqlAlfNode alfNode;
    private GqlQName qName;

    private AlfGqlContext context;

    public AlfNodeAttValue(Attribute att, Object value) {
        this.att = att;
        this.rawValue = value;
    }

    public AlfNodeAttValue(Object value) {
        this.rawValue = value;
    }

    @Override
    public <T extends GqlContext> void init(T context, MetaField field) {

        this.context = (AlfGqlContext) context;

        if (rawValue instanceof GqlAlfNode) {
            alfNode = (GqlAlfNode) rawValue;
        } else if (rawValue instanceof NodeRef) {
            alfNode = this.context.getNode(rawValue).orElse(null);
        } else if (rawValue instanceof QName) {
            qName = this.context.getQName(rawValue).orElse(null);
        } else if (rawValue instanceof GqlQName) {
            qName = (GqlQName) rawValue;
        }
    }

    @Override
    public String getId() {
        if (alfNode != null) {
            return alfNode.nodeRef();
        } else if (qName != null) {
            return qName.shortName();
        }
        return null;
    }

    @Override
    public String getDisplayName() {

        if (rawValue instanceof String && att != null && att.type() == Attribute.Type.PROP) {

            QName name = QName.resolveToQName(context.getNamespaceService(), att.name());
            if (name != null) {

                DictUtils dictUtils = context.getService(DictUtils.QNAME);
                return dictUtils.getPropertyDisplayName(att.getScopeType(), name, (String) rawValue);
            }
        }

        if (alfNode != null) {

            DisplayNameService displayNameService = context.getService(DisplayNameService.QNAME);
            return displayNameService.getDisplayName(new NodeInfo(alfNode));
        }
        if (qName != null) {
            return qName.classTitle();
        }

        return getString();
    }

    @Override
    public String getString() {

        if (rawValue == null) {
            return null;
        }
        if (alfNode != null) {
            //TODO: how can we more accurately find out what is the file control?
            if (Attribute.Type.CHILD_ASSOC.equals(att.type())) {
                Map<QName, Serializable> properties = alfNode.getProperties();

                ContentService contentService = context.getService("contentService");
                NodeRef nodeRef = new NodeRef(alfNode.nodeRef());
                ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

                JSONObject obj = new JSONObject();
                try {
                    //TODO: fix url
                    obj.put("url", "/share/page/card-details?nodeRef=" + alfNode.nodeRef());
                    obj.put("name", properties.get(ContentModel.PROP_NAME));

                    obj.put("size", reader.getSize());
                    //TODO: fill type,kind
                    obj.put("fileType", "category-document-type/cat-document-other");

                    JSONObject data = new JSONObject();
                    data.put("nodeRef", alfNode.nodeRef());

                    obj.put("data", data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                return obj.toString();
            }


            return alfNode.nodeRef();
        }
        if (qName != null) {
            return qName.shortName();
        }
        if (rawValue instanceof Date) {
            return ISO8601Utils.format((Date) rawValue);
        }
        if (rawValue instanceof ContentData) {
            ContentData data = (ContentData) rawValue;

            NodeService nd = this.context.getNodeService();
            NodeRef nodeRef = att.getScopeNodeRef();

            String name = (String) nd.getProperty(nodeRef, ContentModel.PROP_NAME);

            JSONArray array = new JSONArray();
            JSONObject obj = new JSONObject();
            try {
                //TODO: fix url
                obj.put("url", "/share/page/card-details?nodeRef=" + nodeRef.toString());
                obj.put("name", name);
                obj.put("size", data.getSize());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            array.put(obj);
            return array.toString();
        }
        return rawValue.toString();
    }

    @Override
    public Object getAttribute(String name, MetaField field) {
        if (alfNode != null) {
            Attribute attribute = alfNode.attribute(name);
            return attribute.getValues()
                    .stream()
                    .map(v -> {
                        AlfNodeAttValue value = new AlfNodeAttValue(v);
                        value.init(context, field);
                        return value;
                    })
                    .collect(Collectors.toList());
        } else if (qName != null) {
            return MetaUtils.getReflectionValue(qName, name);
        }
        return null;
    }

    public static class NodeInfo implements AlfNodeInfo {

        private GqlAlfNode alfNode;

        private NodeInfo(GqlAlfNode node) {
            alfNode = node;
        }

        @Override
        public QName getType() {
            return alfNode.getType();
        }

        @Override
        public NodeRef getNodeRef() {
            return new NodeRef(alfNode.nodeRef());
        }

        @Override
        public Map<QName, Serializable> getProperties() {
            return alfNode.getProperties();
        }
    }
}
