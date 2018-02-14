package ru.citeck.ecos.graphql.node;

import graphql.annotations.annotationTypes.GraphQLField;
import lombok.Getter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.GqlContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Attribute {

    enum Type { ASSOC, CHILD_ASSOC, PROP, UNKNOWN }

    private Type type;
    private QName name;

    private Object rawValue;

    @Getter(lazy = true)
    private final List<?> values = evalValues();
    @Getter(lazy = true)
    private final List<GqlNode> nodes = evalNodes();

    private GqlContext context;
    private GqlNode scope;

    Attribute(QName name, Type type, GqlNode scope, GqlContext context) {
        this.scope = scope;
        this.name = name;
        this.type = type;
        this.context = context;
    }

    Attribute(QName name, Object value, Type type, GqlNode scope, GqlContext context) {
        this(name, type, scope, context);
        rawValue = value;
    }

    @GraphQLField
    public Attribute.Type type() {
        return type;
    }

    @GraphQLField
    public String name() {
        return name.toPrefixString();
    }

    @GraphQLField
    public Optional<String> value() {
        List<?> values = getValues();
        Object value = values.size() > 0 ? values.get(0) : null;
        return value != null ? Optional.of(value.toString()) : Optional.empty();
    }

    @GraphQLField
    public List<String> values() {
        return getValues().stream()
                          .map(v -> v != null ? v.toString() : null)
                          .collect(Collectors.toList());
    }

    @GraphQLField
    public List<GqlNode> nodes() {
        return getNodes();
    }

    @GraphQLField
    public Optional<GqlNode> node() {
        List<GqlNode> nodes = getNodes();
        return nodes.size() > 0 ? Optional.of(nodes.get(0)) : Optional.empty();
    }

    private List<GqlNode> evalNodes() {

        List<GqlNode> result = new ArrayList<>();

        getValues().forEach(value -> {
            if (value instanceof GqlNode) {
                result.add((GqlNode) value);
            } else if (value instanceof NodeRef) {
                result.add(context.getNode((NodeRef) value));
            } else if (value instanceof String && NodeRef.isNodeRef((String) value)) {
                result.add(context.getNode(new NodeRef((String) value)));
            }
        });
        return result;
    }

    private List<?> evalValues() {
        List<?> result;
        if (rawValue == null) {
            rawValue = scope.getAttributeValue(name, type);
        }
        if (rawValue instanceof List) {
            result = (List) rawValue;
        } else {
            result = rawValue != null ? Collections.singletonList(rawValue) : Collections.emptyList();
        }
        return result;
    }
}
