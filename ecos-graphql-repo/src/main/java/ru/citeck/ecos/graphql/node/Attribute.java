package ru.citeck.ecos.graphql.node;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.schema.DataFetchingEnvironment;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.GqlContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Attribute {

    enum Type { ASSOC, CHILD_ASSOC, PROP, OTHER }

    private Type type;

    private QName name;
    private GqlNode scope;
    private List<?> values;
    private List<GqlNode> nodes;

    private DictionaryService dictionaryService;

    Attribute(QName name, GqlNode scope, GqlContext context) {
        this.scope = scope;
        this.name = name;
        dictionaryService = context.getDictionaryService();
    }

    Attribute(QName name, Object value, GqlNode scope, GqlContext context) {
        this(name, scope, context);
        setValues(value);
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
    public List<GqlNode> nodes(DataFetchingEnvironment env) {
        if (nodes == null) {
            List<?> values = getValues();
            GqlContext context = env.getContext();
            nodes = values.stream()
                          .filter(v -> v instanceof NodeRef || v instanceof String && NodeRef.isNodeRef((String) v))
                          .map(r -> context.getNode(r instanceof NodeRef ? (NodeRef) r : new NodeRef((String) r)))
                          .collect(Collectors.toList());
        }
        return nodes;
    }

    @GraphQLField
    public Optional<GqlNode> node(DataFetchingEnvironment env) {
        List<GqlNode> nodes = nodes(env);
        return nodes.size() > 0 ? Optional.of(nodes.get(0)) : Optional.empty();
    }

    private List<?> getValues() {
        if (values == null) {
            setValues(scope.getAttributeValue(name, getType()));
        }
        return values;
    }

    private Type getType() {
        if (type == null) {
            PropertyDefinition propDef = dictionaryService.getProperty(name);
            if (propDef == null) {
                AssociationDefinition assocDef = dictionaryService.getAssociation(name);
                if (assocDef == null) {
                    type = Type.OTHER;
                } else {
                    type = assocDef.isChild() ? Type.CHILD_ASSOC : Type.ASSOC;
                }
            } else {
                type = Type.PROP;
            }
        }
        return type;
    }

    private void setValues(Object value) {
        if (value instanceof List) {
            values = (List) value;
        } else {
            values = value != null ? Collections.singletonList(value) : Collections.emptyList();
        }
    }
}
