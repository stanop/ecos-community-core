package ru.citeck.ecos.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ConfigModel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Valentin Skeeba
 * @author Roman Makarskiy
 */
public class EcosConfigService {

    private static final Log logger = LogFactory.getLog(EcosConfigService.class);

    private static final NodeRef CONFIGS_ROOT = new NodeRef("workspace://SpacesStore/ecos-config-root");

    private NodeService nodeService;

    private LoadingCache<String, Optional<NodeRef>> configRefByKey;

    public EcosConfigService() {
        configRefByKey = CacheBuilder.newBuilder()
                                     .expireAfterAccess(30, TimeUnit.SECONDS)
                                     .maximumSize(200)
                                     .build(CacheLoader.from(this::findConfigRef));
    }

    /**
     * It returns a config value. Config node is searched by {@code SearchService}.
     *
     * @param key ecos config key
     * @return {@code Object} value
     */
    public Object getParamValue(final String key) {
        return AuthenticationUtil.runAsSystem(() -> {
            Optional<NodeRef> config = getConfigRef(key);
            return config.map(ref -> nodeService.getProperty(ref, ConfigModel.PROP_VALUE)).orElse(null);
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
     * @deprecated use getParamValue(String key) instead
     *
     * @param key      ecos config key
     * @param rootPath root path
     * @return {@code Object} value
     */
    public Object getParamValue(final String key, String rootPath) {
        return getParamValue(key);
    }

    /**
     * Set config value. Config node is searched by {@code SearchService}.
     *
     * @param key   ecos config key
     * @param value new value
     */
    public void setValue(final String key, final String value) {
        AuthenticationUtil.runAsSystem(() -> {
            Optional<NodeRef> config = getConfigRef(key);

            if (!config.isPresent()) {
                throw new NoSuchConfigException("Cannot find config by key: " + key);
            }

            nodeService.setProperty(config.get(), ConfigModel.PROP_VALUE, value);
            return null;
        });
    }

    public NodeRef createConfig(Map<QName, Serializable> properties) {

        String configKey = (String) properties.get(ConfigModel.PROP_KEY);
        ParameterCheck.mandatory(ConfigModel.PROP_KEY.getLocalName(), configKey);

        Optional<NodeRef> configRef = getConfigRef(configKey);
        if (configRef.isPresent()) {
            return configRef.get();
        }

        QName assocQName = QName.createQName(ConfigModel.NAMESPACE, configKey);
        ChildAssociationRef configRefAssoc = nodeService.createNode(CONFIGS_ROOT,
                                                                    ContentModel.ASSOC_CONTAINS,
                                                                    assocQName,
                                                                    ConfigModel.TYPE_ECOS_CONFIG,
                                                                    properties);

        configRefByKey.invalidate(configKey);

        return configRefAssoc.getChildRef();
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
     * @deprecated Use setValue(String key, String value) instead
     *
     * @param key      ecos config key
     * @param value    new value
     * @param rootPath root path
     */
    public void setValue(final String key, final String value, String rootPath) {
        setValue(key, value);
    }

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
            List<ChildAssociationRef> configs;
            configs = nodeService.getChildAssocsByPropertyValue(CONFIGS_ROOT, ConfigModel.PROP_KEY, key);
            return configs.stream().map(ChildAssociationRef::getChildRef).findFirst();
        });
    }

    public void clearCache() {
        configRefByKey.invalidateAll();
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public NodeRef getConfigsRoot() {
        return CONFIGS_ROOT;
    }

    private class NoSuchConfigException extends Exception {
        NoSuchConfigException(String message) {
            super(message);
        }
    }
}
