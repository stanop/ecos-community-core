package ru.citeck.ecos.graphql.meta.alfnode;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;
import ru.citeck.ecos.graphql.meta.attribute.MetaReflectionAttribute;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.graphql.node.GqlQName;

import java.util.Date;
import java.util.Optional;

public class AlfNodeAttributeValue implements MetaValue {

    private Object rawValue;
    private GqlAlfNode alfNode;
    private GqlQName qName;

    private GqlContext context;

    public AlfNodeAttributeValue(Object value, GqlContext context) {
        if (value instanceof NodeRef) {
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
    public String id() {
        return alfNode != null ? alfNode.nodeRef() : null;
    }

    @Override
    public String str() {
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
    public Optional<MetaAttribute> att(String name) {
        if (alfNode != null) {
            Attribute attribute = alfNode.attribute(name);
            return Optional.of(new AlfNodeAttribute(attribute, context));
        } else if (qName != null) {
            return Optional.of(new MetaReflectionAttribute(qName, name));
        }
        return Optional.empty();
    }
}

