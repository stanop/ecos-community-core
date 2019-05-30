package ru.citeck.ecos.records.source.alf.meta;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.QName;
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
            return alfNode.nodeRef();
        }
        if (qName != null) {
            return qName.shortName();
        }
        if (rawValue instanceof Date) {
            return ISO8601Utils.format((Date) rawValue);
        }
        if (rawValue instanceof ContentData) {

            String contentUrl = ((ContentData) rawValue).getContentUrl();
            ContentService contentService = context.getServiceRegistry().getContentService();

            return AuthenticationUtil.runAsSystem(() -> {
                ContentReader reader = contentService.getRawReader(contentUrl);
                return reader.exists() ? reader.getContentString() : null;
            });
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
        } else if (rawValue instanceof ContentData) {

            if ("previewUrl".equals(name)) {

                ContentData data = (ContentData) rawValue;
                String mimetype = data.getMimetype();
                if (mimetype == null || att == null) {
                    return null;
                }
                NodeRef scopeRef = att.getScopeNodeRef();
                if (scopeRef == null) {
                    return null;
                }

                String url = "alfresco/api/node/workspace/SpacesStore/" + scopeRef.getId() + "/content";

                switch (mimetype) {
                    case MimetypeMap.MIMETYPE_PDF:
                    case MimetypeMap.MIMETYPE_IMAGE_PNG:
                    case MimetypeMap.MIMETYPE_IMAGE_JPEG:
                    case MimetypeMap.MIMETYPE_IMAGE_GIF:
                        break;
                    default:
                        String thumbnailType = getThumbnailType(data);
                        if (thumbnailType != null) {
                            url += "/thumbnails/" + thumbnailType;
                        } else {
                            url = null;
                        }
                }

                return url != null ? url + "?c=force" : null;
            }
        }
        return null;
    }

    private String getThumbnailType(ContentData data) {

        ThumbnailService thumbnailService = context.getServiceRegistry().getThumbnailService();
        if (thumbnailService == null) {
            return null;
        }
        ThumbnailRegistry registry = thumbnailService.getThumbnailRegistry();
        if (registry == null) {
            return null;
        }

        List<ThumbnailDefinition> definitions = registry.getThumbnailDefinitions(data.getMimetype(), data.getSize());
        if (definitions == null) {
            definitions = Collections.emptyList();
        }

        for (ThumbnailDefinition definition : definitions) {
            if ("pdf".equals(definition.getName())) {
                return "pdf";
            }
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

