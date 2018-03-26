package ru.citeck.ecos.graphql;

import lombok.Getter;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.graphql.node.GqlQName;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GqlContext {

    private Map<Object, Optional<GqlAlfNode>> nodes = new ConcurrentHashMap<>();
    private Map<Object, Optional<GqlQName>> qnames = new ConcurrentHashMap<>();

    private final ServiceRegistry serviceRegistry;

    @Getter
    private final DictionaryService dictionaryService;
    @Getter
    private final NamespaceService namespaceService;
    @Getter
    private final NodeService nodeService;
    @Getter
    private final MessageService messageService;

    GqlContext(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.nodeService = serviceRegistry.getNodeService();
        this.messageService = serviceRegistry.getMessageService();
    }

    public List<GqlAlfNode> getNodes(Collection<?> keys) {
        return keys.stream()
                   .map(this::getNode)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .collect(Collectors.toList());
    }

    public Optional<GqlAlfNode> getNode(Object nodeRef) {
        if (nodeRef == null) {
            return Optional.empty();
        }
        return nodes.computeIfAbsent(nodeRef, value -> {
            Optional<GqlAlfNode> result;
            if (value instanceof GqlAlfNode) {
                result = Optional.of((GqlAlfNode) value);
            } else if (value instanceof NodeRef) {
                result = Optional.of(new GqlAlfNode((NodeRef) value, this));
            } else if (value instanceof String && NodeRef.isNodeRef((String) value)) {
                result = Optional.of(new GqlAlfNode(new NodeRef((String) value), this));
            } else {
                result = Optional.empty();
            }
            return result;
        });
    }

    public List<GqlQName> getQNames(Collection<?> keys) {
        return keys.stream()
                   .map(this::getQName)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .collect(Collectors.toList());
    }

    public Optional<GqlQName> getQName(Object qname) {
        if (qname == null) {
            return Optional.empty();
        }
        return qnames.computeIfAbsent(qname, value -> {
            Optional<GqlQName> result;
            if (value instanceof GqlQName) {
                result = Optional.of((GqlQName) value);
            } else if (value instanceof QName) {
                result = Optional.of(new GqlQName((QName) value, this));
            } else if (value instanceof String) {
                String str = (String) value;
                if (str.startsWith("{") || str.contains(":")) {
                    QName resolvedQName = QName.resolveToQName(namespaceService, str);
                    result = Optional.of(new GqlQName(resolvedQName, this));
                } else {
                    result = Optional.empty();
                }
            } else {
                result = Optional.empty();
            }
            return result;
        });
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
