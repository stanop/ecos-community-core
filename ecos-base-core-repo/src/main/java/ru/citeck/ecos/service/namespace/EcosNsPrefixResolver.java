package ru.citeck.ecos.service.namespace;

import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EcosNsPrefixResolver implements NamespacePrefixResolver {

    private final Map<String, String> nsByPrefix = new ConcurrentHashMap<>();
    private final Map<String, String> prefixByNs = new ConcurrentHashMap<>();

    @Autowired
    private NamespaceService namespaceService;

    @Override
    public String getNamespaceURI(String prefix) throws NamespaceException {
        String result = nsByPrefix.get(prefix);
        if (result == null) {
            result = namespaceService.getNamespaceURI(prefix);
        }
        return result;
    }

    @Override
    public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException {
        String localPrefix = prefixByNs.get(namespaceURI);
        if (StringUtils.isNotBlank(localPrefix)) {
            return Collections.singleton(localPrefix);
        }
        return namespaceService.getPrefixes(namespaceURI);
    }

    @Override
    public Collection<String> getPrefixes() {
        Set<String> prefixes = new HashSet<>(namespaceService.getPrefixes());
        prefixes.addAll(nsByPrefix.keySet());
        return prefixes;
    }

    @Override
    public Collection<String> getURIs() {
        Set<String> result = new HashSet<>(namespaceService.getURIs());
        result.addAll(nsByPrefix.values());
        return result;
    }

    @Autowired(required = false)
    public void setProviders(List<EcosNsPrefixProvider> providers) {
        providers.forEach(prov -> prov.getPrefixesByNsURI().forEach(this::registerNamespace));
    }

    public void registerNamespace(String namespace, String prefix) {
        this.prefixByNs.put(namespace, prefix);
        this.nsByPrefix.put(prefix, namespace);
    }
}
