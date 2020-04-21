package ru.citeck.ecos.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CachingEcosConfigService implements EcosConfigService {

    protected final Log logger = LogFactory.getLog(this.getClass());

    private NodeService nodeService;
    private SearchService searchService;
    private NamespaceService namespaceService;

    private LoadingCache<String, Optional<NodeRef>> configRefByKey;

    private NodeRef configRoot;
    private String configNamespace;
    private QName configTypeQName;
    private QName configKeyQName;
    private QName configValueQName;


    public CachingEcosConfigService() {
        configRefByKey = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .maximumSize(200)
            .build(CacheLoader.from(this::findConfigRef));
    }


    public void clearCache() {
        configRefByKey.invalidateAll();
    }


    @Override
    public Object getParamValue(final String key) {
        return AuthenticationUtil.runAsSystem(() -> {
            Optional<NodeRef> config = getConfigRef(key);
            return config.map(ref -> nodeService.getProperty(ref, configValueQName)).orElse(null);
        });
    }

    @Override
    @Deprecated
    public Object getParamValue(final String key, String rootPath) {
        return getParamValue(key);
    }

    @Override
    public void setValue(final String key, final String value) {
        AuthenticationUtil.runAsSystem(() -> {
            Optional<NodeRef> config = getConfigRef(key);

            if (!config.isPresent()) {
                throw new NoSuchConfigException("Cannot find config by key: " + key);
            }

            nodeService.setProperty(config.get(), configValueQName, value);
            return null;
        });
    }

    @Override
    @Deprecated
    public void setValue(final String key, final String value, String rootPath) {
        setValue(key, value);
    }

    @Override
    public NodeRef createConfig(Map<QName, Serializable> properties) {
        String configKey = (String) properties.get(configKeyQName);
        ParameterCheck.mandatory(configKeyQName.getLocalName(), configKey);

        Optional<NodeRef> configRef = getConfigRef(configKey);
        if (configRef.isPresent()) {
            return configRef.get();
        }

        QName assocQName = QName.createQName(configNamespace, configKey);
        ChildAssociationRef configRefAssoc = nodeService.createNode(getConfigRoot(),
            ContentModel.ASSOC_CONTAINS,
            assocQName,
            configTypeQName,
            properties);

        configRefByKey.invalidate(configKey);

        return configRefAssoc.getChildRef();
    }

    @Override
    public void removeConfig(String key) {
        ParameterCheck.mandatory("key", key);

        Optional<NodeRef> configRef = getConfigRef(key);
        if (!configRef.isPresent()) {
            return;
        }

        RepoUtils.deleteNode(configRef.get(), nodeService);
    }

    @Override
    public Optional<NodeRef> getConfigRef(String key) {
        Optional<NodeRef> configRef = configRefByKey.getUnchecked(key);
        if (!configRef.isPresent() || !nodeService.exists(configRef.get())) {
            configRefByKey.invalidate(key);
            configRef = configRefByKey.getUnchecked(key);
        }
        return configRef;
    }

    private Optional<NodeRef> findConfigRef(String key) {
        return AuthenticationUtil.runAsSystem(() -> {
            try {
                return FTSQuery.create()
                    .type(configTypeQName).and()
                    .exact(configKeyQName, key)
                    .transactional()
                    .queryOne(searchService);
            } catch (Exception e) {
                if (RetryingTransactionHelper.extractRetryCause(e) != null) {
                    throw e;
                }
                List<ChildAssociationRef> configs;
                configs = nodeService.getChildAssocsByPropertyValue(getConfigRoot(), configKeyQName, key);
                return configs.stream().map(ChildAssociationRef::getChildRef).findFirst();
            }
        });
    }

    @Override
    public NodeRef getConfigRoot() {
        return configRoot;
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Autowired
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Autowired
    @Qualifier("namespaceService")
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    @Required
    public void setConfigRoot(String configRoot) {
        if (NodeRef.isNodeRef(configRoot)) {
            this.configRoot = new NodeRef(configRoot);
        } else {
            NodeRef rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            List<NodeRef> nodeRefs = this.searchService.selectNodes(
                rootNode, configRoot, null, namespaceService, false);
            if (CollectionUtils.isEmpty(nodeRefs)) {
                throw new IllegalArgumentException("Can not be founded nodes for path: " + configRoot);
            }
            this.configRoot = nodeRefs.get(0);
        }
    }

    @Required
    public void setConfigNamespace(String configNamespace) {
        this.configNamespace = configNamespace;
    }

    @Required
    public void setConfigTypeQName(QName configTypeQName) {
        this.configTypeQName = configTypeQName;
    }

    @Required
    public void setConfigKeyQName(QName configKeyQName) {
        this.configKeyQName = configKeyQName;
    }

    @Required
    public void setConfigValueQName(QName configValueQName) {
        this.configValueQName = configValueQName;
    }

    private class NoSuchConfigException extends Exception {
        NoSuchConfigException(String message) {
            super(message);
        }
    }

}
