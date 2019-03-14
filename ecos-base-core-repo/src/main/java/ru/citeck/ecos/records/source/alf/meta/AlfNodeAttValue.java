package ru.citeck.ecos.records.source.alf.meta;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.records.meta.MetaUtils;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.graphql.node.GqlQName;
import ru.citeck.ecos.records2.graphql.GqlContext;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.utils.DictUtils;

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
    public <T extends GqlContext> void init(T context) {
        init(context, null);
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

        return getString();
    }

    @Override
    public String getString() {
        if (alfNode != null) {
            return alfNode.displayName();
        } else if (qName != null) {
            return qName.classTitle();
        } else if (rawValue instanceof Date) {
            return ISO8601Utils.format((Date) rawValue);
        } else if (rawValue instanceof ContentData) {

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
                                value.init(context);
                                return value;
                            })
                            .collect(Collectors.toList());
        } else if (qName != null) {
            return MetaUtils.getReflectionValue(qName, name);
        }
        return null;
    }
}

