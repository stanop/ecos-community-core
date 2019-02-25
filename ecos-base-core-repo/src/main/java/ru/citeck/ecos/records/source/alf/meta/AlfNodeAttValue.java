package ru.citeck.ecos.records.source.alf.meta;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.MetaUtils;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.graphql.node.GqlQName;

import java.util.*;
import java.util.stream.Collectors;

public class AlfNodeAttValue implements MetaValue {

    private Object rawValue;
    private GqlAlfNode alfNode;
    private GqlQName qName;

    private GqlContext context;

    public AlfNodeAttValue(Object value) {
        this.rawValue = value;
    }

    @Override
    public MetaValue init(GqlContext context) {

        this.context = context;

        if (rawValue instanceof GqlAlfNode) {
            alfNode = (GqlAlfNode) rawValue;
        } else if (rawValue instanceof NodeRef) {
            alfNode = context.getNode(rawValue).orElse(null);
        } else if (rawValue instanceof QName) {
            qName = context.getQName(rawValue).orElse(null);
        } else if (rawValue instanceof GqlQName) {
            qName = (GqlQName) rawValue;
        }

        return this;
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
    public List<MetaValue> getAttribute(String name) {
        if (alfNode != null) {
            Attribute attribute = alfNode.attribute(name);
            return attribute.getValues()
                            .stream()
                            .map(v -> new AlfNodeAttValue(v).init(context))
                            .collect(Collectors.toList());
        } else if (qName != null) {
            return MetaUtils.getReflectionValue(qName, name);
        }
        return null;
    }
}

