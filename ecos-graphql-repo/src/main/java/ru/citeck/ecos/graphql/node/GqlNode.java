package ru.citeck.ecos.graphql.node;

import graphql.annotations.annotationTypes.GraphQLDefaultValue;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.schema.DataFetchingEnvironment;
import lombok.Getter;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.GqlContext;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GqlNode {

    private NodeRef nodeRef;

    @Getter(lazy = true)
    private final QName type = evalType();

    @Getter(lazy = true)
    private final Map<QName, Serializable> properties = evalProperties();
    @Getter(lazy = true)
    private final Map<QName, List<NodeRef>> targetAssocs = evalTargetAssocs();
    @Getter(lazy = true)
    private final Map<QName, List<NodeRef>> childAssocs = evalChildAssocs();

    private Map<QName, List<NodeRef>> assocs;
    private Map<QName, Attribute> attributes;

    private GqlContext context;

    public GqlNode(NodeRef nodeRef, GqlContext context) {
        this.nodeRef = nodeRef;
        this.context = context;
    }

    @GraphQLField
    public String nodeRef() {
        return nodeRef.toString();
    }

    @GraphQLField
    public String type() {
        return getType().toPrefixString();
    }

    @GraphQLField
    public Attribute attribute(DataFetchingEnvironment env,
                               @GraphQLName("name") String name) {

        GqlContext context = env.getContext();
        QName qname = QName.resolveToQName(context.getNamespaceService(), name);

        return getAttributes().computeIfAbsent(qname, attName -> {
            QName prefixedName = attName.getPrefixedQName(context.getNamespaceService());
            return new Attribute(prefixedName, getAttributeType(prefixedName), this, context);
        });
    }

    @GraphQLField
    public List<Attribute> attributes(DataFetchingEnvironment env,
                                      @GraphQLName("types")
                                      @GraphQLDefaultValue(DefaultAttributesTypes.class)
                                      List<Attribute.Type> types) {

        Map<QName, Attribute> result = getAttributes();

        BiConsumer<Map<QName, ?>, Attribute.Type> addAttributes = (input, attType) -> input.forEach((key, value) -> {
            if (!result.containsKey(key)) {
                QName name = key.getPrefixedQName(context.getNamespaceService());
                Attribute attr = new Attribute(name, value, attType, this, env.getContext());
                result.put(name, attr);
            }
        });

        if (types.contains(Attribute.Type.PROP)) {
            addAttributes.accept(getProperties(), Attribute.Type.PROP);
        }

        if (types.contains(Attribute.Type.ASSOC)) {
            addAttributes.accept(getTargetAssocs(), Attribute.Type.ASSOC);
        }

        if (types.contains(Attribute.Type.CHILD_ASSOC)) {
            addAttributes.accept(getChildAssocs(), Attribute.Type.CHILD_ASSOC);
        }

        return new ArrayList<>(attributes.values());
    }

    public Object getAttributeValue(QName name, Attribute.Type type) {
        switch (type) {
            case PROP:
                return getProperties().get(name);
            case ASSOC:
                return getTargetAssoc(name);
            case CHILD_ASSOC:
                return getChildAssoc(name);
        }
        return null;
    }

    private List<NodeRef> getTargetAssoc(QName assocName) {
        return getAssocs().computeIfAbsent(assocName, name ->
            context.getNodeService()
                   .getTargetAssocs(nodeRef, name)
                   .stream()
                   .map(AssociationRef::getTargetRef)
                   .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    private List<NodeRef> getChildAssoc(QName assocName) {
        return getAssocs().computeIfAbsent(assocName, name ->
            context.getNodeService()
                   .getChildAssocs(nodeRef, name, q -> true)
                   .stream()
                   .map(ChildAssociationRef::getChildRef)
                   .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    private QName evalType() {
        return context.getNodeService()
                      .getType(nodeRef)
                      .getPrefixedQName(context.getNamespaceService());
    }

    private Map<QName, Serializable> evalProperties() {
        return context.getNodeService().getProperties(nodeRef);
    }

    private Map<QName, List<NodeRef>> evalTargetAssocs() {

        List<AssociationRef> assocRefs = context.getNodeService().getTargetAssocs(nodeRef, q -> true);

        Map<QName, List<NodeRef>> result = new HashMap<>();
        assocRefs.forEach(assocRef -> {
            List<NodeRef> nodes = result.computeIfAbsent(assocRef.getTypeQName(),
                                                         k -> new ArrayList<>(4));
            nodes.add(assocRef.getTargetRef());
        });

        result.forEach(getAssocs()::putIfAbsent);

        return result;
    }

    private Map<QName, List<NodeRef>> evalChildAssocs() {

        List<ChildAssociationRef> assocsRefs = context.getNodeService().getChildAssocs(nodeRef, q -> true, q -> true);

        Map<QName, List<NodeRef>> result = new HashMap<>();
        assocsRefs.forEach(assocRef -> {
            List<NodeRef> nodes = result.computeIfAbsent(assocRef.getTypeQName(),
                                                         k -> new ArrayList<>(4));
            nodes.add(assocRef.getChildRef());
        });

        result.forEach(getAssocs()::putIfAbsent);

        return result;
    }

    private Map<QName, Attribute> getAttributes() {
        if (attributes == null) {
            synchronized (this) {
                if (attributes == null) {
                    attributes = new ConcurrentHashMap<>();
                }
            }
        }
        return attributes;
    }

    private Map<QName, List<NodeRef>> getAssocs() {
        if (assocs == null) {
            synchronized (this) {
                if (assocs == null) {
                    assocs = new ConcurrentHashMap<>();
                }
            }
        }
        return assocs;
    }

    private Attribute.Type getAttributeType(QName name) {
        Attribute.Type result;
        PropertyDefinition propDef = context.getDictionaryService().getProperty(name);
        if (propDef == null) {
            AssociationDefinition assocDef = context.getDictionaryService().getAssociation(name);
            if (assocDef == null) {
                result = Attribute.Type.UNKNOWN;
            } else {
                result = assocDef.isChild() ? Attribute.Type.CHILD_ASSOC : Attribute.Type.ASSOC;
            }
        } else {
            result = Attribute.Type.PROP;
        }
        return result;
    }

    public static class DefaultAttributesTypes implements Supplier<Object> {
        @Override
        public Object get() {
            return Collections.singletonList(Attribute.Type.PROP);
        }
    }

    @Override
    public String toString() {
        return nodeRef.toString();
    }
}
