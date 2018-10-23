package ru.citeck.ecos.records.source.alfnode.meta;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;
import ru.citeck.ecos.graphql.meta.attribute.MetaReflectionAtt;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.graphql.node.GqlQName;

import java.util.*;
import java.util.stream.Collectors;

public class AlfNodeAttValue implements MetaValue {

    private static final Log logger = LogFactory.getLog(AlfNodeAttValue.class);

    private Object rawValue;
    private GqlAlfNode alfNode;
    private GqlQName qName;

    private GqlContext context;

    public AlfNodeAttValue(Object value, GqlContext context) {
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
            return Optional.of(new AlfNodeAtt(attribute, context));
        } else if (qName != null) {
            return Optional.of(new MetaReflectionAtt(qName, name));
        }
        return Optional.empty();
    }

    @Override
    public List<MetaAttribute> atts(String filter) {
        if (alfNode != null) {
            List<Attribute> attributes;
            if (StringUtils.isNotBlank(filter)) {
                attributes = alfNode.attributes(null, null);
            } else {
                String[] filters = filter.split(",");
                List<Attribute.Type> types = new ArrayList<>();
                for (String f : filters) {
                    try {
                        types.add(Attribute.Type.valueOf(f.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        logger.error("Attribute type " + f + " is unknown");
                    }
                }
                attributes = alfNode.attributes(types, null);
            }
            return attributes.stream()
                             .map(a -> new AlfNodeAtt(a, context))
                             .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

