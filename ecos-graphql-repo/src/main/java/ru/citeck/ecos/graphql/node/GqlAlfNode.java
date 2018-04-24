package ru.citeck.ecos.graphql.node;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import lombok.Getter;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.GqlContext;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GqlAlfNode {

    private NodeRef nodeRef;

    @Getter(lazy = true)
    private final QName type = evalType();
    @Getter(lazy = true)
    private final Map<QName, Serializable> properties = evalProperties();
    @Getter(lazy = true)
    private final Map<QName, List<NodeRef>> targetAssocs = evalTargetAssocs();
    @Getter(lazy = true)
    private final Map<QName, List<NodeRef>> childAssocs = evalChildAssocs();
    @Getter(lazy = true)
    private final List<GqlQName> aspects = evalAspects();

    private Map<QName, List<NodeRef>> assocs = new ConcurrentHashMap<>();
    private Map<QName, Attribute> attributes = new ConcurrentHashMap<>();

    private GqlContext context;

    public GqlAlfNode(NodeRef nodeRef, GqlContext context) {
        this.nodeRef = nodeRef;
        this.context = context;
    }

    @GraphQLField
    public String displayName() {

        QName type = getType();

        if (type.equals(ContentModel.TYPE_PERSON)) {

            Attribute firstName = getAttribute(ContentModel.PROP_FIRSTNAME);
            Attribute lastName = getAttribute(ContentModel.PROP_LASTNAME);

            Optional value = firstName.value();
            StringBuilder result = new StringBuilder();
            if (value.isPresent()) {
                result.append(value.get());
            }
            value = lastName.value();
            if (value.isPresent()) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(value.get());
            }

            if (result.length() == 0) {
                Attribute userName = getAttribute(ContentModel.PROP_USERNAME);
                result.append(userName.value().orElse(nodeRef.toString()));
            }

            return result.toString();

        } else if (type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {

            Attribute displayName = getAttribute(ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
            Attribute authorityName = getAttribute(ContentModel.PROP_AUTHORITY_NAME);

            return displayName.value().orElseGet(() -> authorityName.value().orElse(null));

        } else {
            Attribute title = getAttribute(ContentModel.PROP_TITLE);
            Attribute name = getAttribute(ContentModel.PROP_NAME);
            return title.value().orElseGet(() -> name.value().orElse(null));
        }
    }

    @GraphQLField
    public List<GqlQName> aspects() {
        return getAspects();
    }

    @GraphQLField
    public List<String> aspectNames() {
        return getAspects().stream()
                           .map(GqlQName::shortName)
                           .collect(Collectors.toList());
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
    public Optional<GqlQName> typeQName() {
        return context.getQName(getType());
    }

    @GraphQLField
    @GraphQLName("isContainer")
    public boolean isContainer() {
        DictionaryService dd = context.getDictionaryService();
        QName type = getType();
        return dd.isSubClass(type, ContentModel.TYPE_FOLDER) &&
              !dd.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER);
    }

    @GraphQLField
    @GraphQLName("isDocument")
    public boolean isDocument() {
        DictionaryService dd = context.getDictionaryService();
        return dd.isSubClass(getType(), ContentModel.TYPE_CONTENT);
    }

    @GraphQLField
    public Attribute attribute(@GraphQLName("name") String name) {
        return getAttribute(QName.resolveToQName(context.getNamespaceService(), name));
    }

    private Attribute getAttribute(QName qname) {
        return attributes.computeIfAbsent(qname, attName -> {
            QName prefixedName = attName.getPrefixedQName(context.getNamespaceService());
            return new Attribute(prefixedName, getAttributeType(prefixedName), this, context);
        });
    }

    @GraphQLField
    public List<Attribute> attributes(@GraphQLName("types")
                                      List<Attribute.Type> types,
                                      @GraphQLName("names")
                                      List<String> names) {
        if (names != null) {
            List<Attribute> result = new ArrayList<>();
            names.forEach(name -> result.add(attribute(name)));
            return result;
        } else {
            if (types == null) {
                types = Collections.singletonList(Attribute.Type.PROP);
            }
            return getAttributesByTypes(types);
        }
    }

    private List<Attribute> getAttributesByTypes(List<Attribute.Type> types) {

        if (types.contains(Attribute.Type.PROP)) {
            addAttributes(getProperties(), Attribute.Type.PROP);
        }

        if (types.contains(Attribute.Type.ASSOC)) {
            addAttributes(getTargetAssocs(), Attribute.Type.ASSOC);
        }

        if (types.contains(Attribute.Type.CHILD_ASSOC)) {
            addAttributes(getChildAssocs(), Attribute.Type.CHILD_ASSOC);
        }

        return new ArrayList<>(attributes.values());
    }

    private void addAttributes(Map<QName, ?> toAdd, Attribute.Type attrType) {
        toAdd.forEach((key, value) -> addAttribute(key, value, attrType));
    }

    private void addAttribute(QName name, Object value, Attribute.Type attrType) {
        if (!attributes.containsKey(name)) {
            QName prefixedQName = name.getPrefixedQName(context.getNamespaceService());
            Attribute attr = new Attribute(prefixedQName, value, attrType, this, context);
            attributes.put(prefixedQName, attr);
        }
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
        return assocs.computeIfAbsent(assocName, name ->
            context.getNodeService()
                   .getTargetAssocs(nodeRef, name)
                   .stream()
                   .map(AssociationRef::getTargetRef)
                   .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    private List<NodeRef> getChildAssoc(QName assocName) {
        return assocs.computeIfAbsent(assocName, name ->
            context.getNodeService()
                   .getChildAssocs(nodeRef, name, q -> true)
                   .stream()
                   .map(ChildAssociationRef::getChildRef)
                   .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    private List<GqlQName> evalAspects() {
        return context.getQNames(context.getNodeService().getAspects(nodeRef));
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

        result.forEach(assocs::putIfAbsent);

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

        result.forEach(assocs::putIfAbsent);

        return result;
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

    @Override
    public String toString() {
        return nodeRef.toString();
    }
}
