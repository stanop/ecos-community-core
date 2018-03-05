package ru.citeck.ecos.graphql;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.node.GqlAlfNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GqlContext {

    private Map<NodeRef, GqlAlfNode> nodes = new ConcurrentHashMap<>();

    private ServiceRegistry serviceRegistry;

    public GqlContext(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public GqlAlfNode getNode(NodeRef nodeRef) {
        return nodes.computeIfAbsent(nodeRef, r -> new GqlAlfNode(r, this));
    }

    public DictionaryService getDictionaryService() {
        return serviceRegistry.getDictionaryService();
    }

    public NamespaceService getNamespaceService() {
        return serviceRegistry.getNamespaceService();
    }

    public SearchService getSearchService() {
        return serviceRegistry.getSearchService();
    }

    public NodeService getNodeService() {
        return serviceRegistry.getNodeService();
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public <T> T getService(QName name) {
        return (T) serviceRegistry.getService(name);
    }
}
