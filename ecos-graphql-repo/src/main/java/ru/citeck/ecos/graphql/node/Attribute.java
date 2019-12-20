package ru.citeck.ecos.graphql.node;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import lombok.Getter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.graphql.AlfGqlContext;

import java.util.*;
import java.util.stream.Collectors;

public class Attribute {

    public enum Type { ASSOC, CHILD_ASSOC, PROP, UNKNOWN }

    private Type type;
    private QName name;

    private Object rawValue;

    @Getter(lazy = true)
    private final List<?> values = evalValues();

    private AlfGqlContext context;
    private GqlAlfNode scope;

    Attribute(QName name, Type type, GqlAlfNode scope, AlfGqlContext context) {
        this.scope = scope;
        this.name = name;
        this.type = type;
        this.context = context;
    }

    Attribute(QName name, Object value, Type type, GqlAlfNode scope, AlfGqlContext context) {
        this(name, type, scope, context);
        rawValue = value;
    }

    public Type type() {
        return type;
    }

    public String name() {
        return name.toPrefixString();
    }

    public Optional<String> value() {
        List<?> values = getValues();
        Object value = !values.isEmpty() ? values.get(0) : null;
        return Optional.ofNullable(getAsString(value));
    }

    public List<String> values() {
        return getValues().stream()
                          .map(v -> Optional.ofNullable(getAsString(v)))
                          .filter(Optional::isPresent)
                          .map(Optional::get)
                          .collect(Collectors.toList());
    }

    private String getAsString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return ISO8601Utils.format((Date) value);
        } else if (value instanceof MLText) {
            return ((MLText) value).getClosestValue(I18NUtil.getLocale());
        }
        return value.toString();
    }

    public List<GqlQName> qnames() {
        return context.getQNames(getValues());
    }

    public Optional<GqlQName> qname() {
        List<GqlQName> qnames = qnames();
        return !qnames.isEmpty() ? Optional.of(qnames.get(0)) : Optional.empty();
    }

    public List<GqlAlfNode> nodes() {
        return context.getNodes(getValues());
    }

    public Optional<GqlAlfNode> node() {
        List<GqlAlfNode> nodes = nodes();
        return !nodes.isEmpty() ? Optional.of(nodes.get(0)) : Optional.empty();
    }

    public QName getScopeType() {
        return scope != null ? scope.getType() : null;
    }

    public NodeRef getScopeNodeRef() {
        if (scope == null) {
            return null;
        }
        String nodeRef = scope.nodeRef();
        return nodeRef != null ? new NodeRef(nodeRef) : null;
    }

    private List<?> evalValues() {
        List<?> result;
        if (rawValue == null && scope != null && type != null) {
            rawValue = scope.getAttributeValue(name, type);
        }
        if (rawValue instanceof List) {
            result = ((List<?>) rawValue).stream()
                                         .filter(Objects::nonNull)
                                         .collect(Collectors.toList());
        } else {
            result = rawValue != null ? Collections.singletonList(rawValue) : Collections.emptyList();
        }
        return result;
    }
}
