package ru.citeck.ecos.graphql.node;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import lombok.Getter;
import org.alfresco.service.namespace.QName;
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
        Object value = values.size() > 0 ? values.get(0) : null;
        if (value != null) {
            if (value instanceof Date) {
                return Optional.of(ISO8601Utils.format((Date) value));
            }
            return Optional.of(value.toString());
        }

        return Optional.empty();
    }

    public List<String> values() {
        return getValues().stream()
                          .map(v -> v != null ? v.toString() : null)
                          .collect(Collectors.toList());
    }

    public List<GqlQName> qnames() {
        return context.getQNames(getValues());
    }

    public Optional<GqlQName> qname() {
        List<GqlQName> qnames = qnames();
        return qnames.size() > 0 ? Optional.of(qnames.get(0)) : Optional.empty();
    }

    public List<GqlAlfNode> nodes() {
        return context.getNodes(getValues());
    }

    public Optional<GqlAlfNode> node() {
        List<GqlAlfNode> nodes = nodes();
        return nodes.size() > 0 ? Optional.of(nodes.get(0)) : Optional.empty();
    }

    private List<?> evalValues() {
        List<?> result;
        if (rawValue == null) {
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
