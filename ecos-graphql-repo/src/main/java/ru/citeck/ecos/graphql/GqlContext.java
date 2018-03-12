package ru.citeck.ecos.graphql;

import lombok.Getter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.graphql.node.GqlQName;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GqlContext {

    private Map<NodeRef, GqlAlfNode> nodes = new ConcurrentHashMap<>();
    private Map<QName, GqlQName> qnames = new ConcurrentHashMap<>();
    private Map<String, QName> qnameByString = new ConcurrentHashMap<>();

    private final ServiceRegistry serviceRegistry;

    @Getter
    private final DictionaryService dictionaryService;
    @Getter
    private final NamespaceService namespaceService;
    @Getter
    private final NodeService nodeService;

    GqlContext(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    public GqlAlfNode getNode(NodeRef nodeRef) {
        return nodes.computeIfAbsent(nodeRef, r -> new GqlAlfNode(r, this));
    }

    public GqlQName getQName(QName qname) {
        return qnames.computeIfAbsent(qname, r -> new GqlQName(qname, this));
    }

    public GqlQName getQName(String name) {
        return getQName(qnameByString.computeIfAbsent(name, n -> QName.resolveToQName(namespaceService, n)));
    }

    public SearchService getSearchService() {
        return serviceRegistry.getSearchService();
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(QName name) {
        return (T) serviceRegistry.getService(name);
    }
}
