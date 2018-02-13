package ru.citeck.ecos.graphql.node;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.schema.DataFetchingEnvironment;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.GqlContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GqlNode {

    private NodeRef nodeRef;

    private Map<QName, Serializable> properties;
    private Map<QName, Attribute> attributes;

    private NodeService nodeService;
    private NamespaceService namespaceService;

    public GqlNode(NodeRef nodeRef, ServiceRegistry serviceRegistry) {
        this.nodeRef = nodeRef;
        nodeService = serviceRegistry.getNodeService();
        namespaceService = serviceRegistry.getNamespaceService();
    }

    @GraphQLField
    public String nodeRef() {
        return nodeRef.toString();
    }

    @GraphQLField
    public Attribute attribute(DataFetchingEnvironment env,
                               @GraphQLName("name") String name) {

        if (attributes == null) {
            attributes = new ConcurrentHashMap<>();
        }
        GqlContext context = env.getContext();
        QName qname = QName.resolveToQName(context.getNamespaceService(), name);

        return attributes.computeIfAbsent(qname, attName -> {
            QName prefixedName = attName.getPrefixedQName(context.getNamespaceService());
            return new Attribute(prefixedName, this, context);
        });
    }

    @GraphQLField
    public List<Attribute> attributes(DataFetchingEnvironment env) {

        if (attributes == null) {

            Map<QName, Serializable> properties = getProperties();
            attributes = new HashMap<>();

            properties.forEach((key, value) -> {
                QName name = key.getPrefixedQName(namespaceService);
                attributes.put(name, new Attribute(name, value, this, env.getContext()));
            });
        }

        return new ArrayList<>(attributes.values());
    }

    public Serializable getAttributeValue(QName name, Attribute.Type type) {
        switch (type) {
            case PROP:
                return getProperties().get(name);
            case ASSOC:
                return nodeService.getTargetAssocs(nodeRef, name)
                                  .stream()
                                  .map(AssociationRef::getTargetRef)
                                  .collect(Collectors.toCollection(ArrayList::new));
            case CHILD_ASSOC:
                return nodeService.getChildAssocs(nodeRef, name, q -> true)
                                  .stream()
                                  .map(ChildAssociationRef::getChildRef)
                                  .collect(Collectors.toCollection(ArrayList::new));
        }
        return null;
    }

    private Map<QName, Serializable> getProperties() {
        if (properties == null) {
            properties = nodeService.getProperties(nodeRef);
        }
        return properties;
    }

}
