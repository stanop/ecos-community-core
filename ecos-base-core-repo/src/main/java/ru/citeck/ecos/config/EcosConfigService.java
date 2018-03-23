package ru.citeck.ecos.config;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ConfigModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author Valentin Skeeba
 * @author Roman Makarskiy
 */
public class EcosConfigService {

    private static final Log logger = LogFactory.getLog(EcosConfigService.class);

    private static final String DEFAULT_ROOT_PATH_TO_CONFIGS = "/app:company_home/app:dictionary";

    private SearchService searchService;
    private NodeService nodeService;
    private NamespaceService namespaceService;

    /**
     * It returns a config value. Config node is searched by {@code SearchService}.
     *
     * @param key ecos config key
     * @return {@code Object} value
     */
    public Object getParamValue(final String key) {
        return AuthenticationUtil.runAsSystem(() -> {
            Object result = null;
            NodeRef config = getConfigRef(key);

            if (config != null && nodeService.exists(config)) {
                result = nodeService.getProperty(config, ConfigModel.PROP_VALUE);
            }

            return result;
        });
    }

    /**
     * It returns a config value. Config node is searched through children of {@code rootPath} with
     * type {@code ConfigModel.TYPE_ECOS_CONFIG}.
     * <p>
     * You can use this method, while {@code SearchService} is not available,
     * for example - while repository is bootstrapping.
     * <p>
     * If {@code rootPath} is null, is used default root path - {@code DEFAULT_ROOT_PATH_TO_CONFIGS}.
     *
     * @param key      ecos config key
     * @param rootPath root path
     * @return {@code Object} value
     */
    public Object getParamValue(final String key, String rootPath) {
        return AuthenticationUtil.runAsSystem(() -> {
            Object result = null;
            NodeRef config = getConfigRef(key, rootPath);

            if (config != null && nodeService.exists(config)) {
                result = nodeService.getProperty(config, ConfigModel.PROP_VALUE);
            }

            return result;
        });
    }

    /**
     * Set config value. Config node is searched by {@code SearchService}.
     *
     * @param key   ecos config key
     * @param value new value
     */
    public void setValue(final String key, final String value) {
        AuthenticationUtil.runAsSystem(() -> {
            NodeRef config = getConfigRef(key);

            if (config == null || !nodeService.exists(config)) {
                throw new NoSuchConfigException("Cannot find config by key: " + key);
            }

            nodeService.setProperty(config, ConfigModel.PROP_VALUE, value);
            return null;
        });
    }

    /**
     * Set a config value. Config node is searched through children of {@code rootPath} with
     * type {@code ConfigModel.TYPE_ECOS_CONFIG}.
     * <p>
     * You can use this method, while {@code SearchService} is not available,
     * for example - while repository is bootstrapping.
     * <p>
     * If {@code rootPath} is null, is used default root path - {@code DEFAULT_ROOT_PATH_TO_CONFIGS}.
     *
     * @param key      ecos config key
     * @param value    new value
     * @param rootPath root path
     */
    public void setValue(final String key, final String value, String rootPath) {
        AuthenticationUtil.runAsSystem(() -> {
            NodeRef config = getConfigRef(key, rootPath);

            if (config == null || !nodeService.exists(config)) {
                throw new NoSuchConfigException("Cannot find config by key: " + key + " and rootPath: " + rootPath);
            }

            nodeService.setProperty(config, ConfigModel.PROP_VALUE, value);
            return null;
        });
    }

    private NodeRef getConfigRef(final String key) {
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef result = null;
            try {
                SearchParameters searchParameters = new SearchParameters();
                searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
                searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                searchParameters.setQueryConsistency(QueryConsistency.TRANSACTIONAL);
                searchParameters.setLimitBy(LimitBy.UNLIMITED);
                searchParameters.setLimit(0);
                searchParameters.setMaxPermissionChecks(Integer.MAX_VALUE);
                searchParameters.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
                searchParameters.setMaxItems(-1);
                searchParameters.setQuery("TYPE:\"" + ConfigModel.TYPE_ECOS_CONFIG + "\" AND =@" + ConfigModel.PROP_KEY
                        + ":" + key);

                ResultSet searchResults = searchService.query(searchParameters);
                if (searchResults.getNodeRefs() != null && !searchResults.getNodeRefs().isEmpty()) {
                    result = searchResults.getNodeRef(0);
                }
            } catch (Exception e) {
                logger.error("Error while getting config" + key, e);
                throw e;
            }
            return result;
        });
    }

    private NodeRef getConfigRef(final String key, final String rootPath) {
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef root = StringUtils.isBlank(rootPath) ? getRootFolder(DEFAULT_ROOT_PATH_TO_CONFIGS)
                    : getRootFolder(rootPath);

            if (logger.isDebugEnabled()) {
                logger.debug("root: " + root);
            }

            List<NodeRef> ecosConfigs = RepoUtils.getChildrenByType(root, ConfigModel.TYPE_ECOS_CONFIG, nodeService);

            for (NodeRef config : ecosConfigs) {
                String currentKey = RepoUtils.getProperty(config, ConfigModel.PROP_KEY, String.class, nodeService);

                if (logger.isDebugEnabled()) {
                    logger.debug("Current key: " + currentKey);
                }

                if (Objects.equals(key, currentKey)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Return: " + config);
                    }
                    return config;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Nothing...return null");
            }
            return null;
        });
    }

    private NodeRef getRootFolder(String xPath) {
        NodeRef rootRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        List<NodeRef> refs = searchService.selectNodes(rootRef, xPath, null,
                namespaceService, false);

        if (refs.size() != 1) {
            return null;
        } else {
            return refs.get(0);
        }
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private class NoSuchConfigException extends Exception {
        NoSuchConfigException(String message) {
            super(message);
        }
    }
}
