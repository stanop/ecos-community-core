package ru.citeck.ecos.records.source.alfnode.meta;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
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

    public AlfNodeAttValue(Object value, GqlContext context) {
        if (value instanceof GqlAlfNode) {
            alfNode = (GqlAlfNode) value;
        } else if (value instanceof NodeRef) {
            alfNode = context.getNode(value).orElse(null);
        } else if (value instanceof QName) {
            qName = context.getQName(value).orElse(null);
        } else if (value instanceof GqlQName) {
            qName = (GqlQName) value;
        }
        this.rawValue = value;
        this.context = context;
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
        }
        return rawValue.toString();
    }

    @Override
    public List<MetaValue> getAttribute(String name) {
        if (alfNode != null) {
            Attribute attribute = alfNode.attribute(name);
            return attribute.getValues()
                            .stream()
                            .map(v -> new AlfNodeAttValue(v, context))
                            .collect(Collectors.toList());
        } else if (qName != null) {
            return MetaUtils.getReflectionValue(qName, name);
        }
        return null;
    }
}

