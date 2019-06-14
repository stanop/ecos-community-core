package ru.citeck.ecos.config;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public interface EcosConfigService {

    /**
     * It returns a config value. Config node is searched by {@code SearchService}.
     *
     * @param key ecos config key
     * @return {@code Object} value
     */
    Object getParamValue(final String key);

    /**
     * It returns a config value. Config node is searched through children of {@code rootPath} with
     * type {@code ConfigModel.TYPE_ECOS_CONFIG}.
     * <p>
     * You can use this method, while {@code SearchService} is not available,
     * for example - while repository is bootstrapping.
     *
     * @param key      ecos config key
     * @param rootPath root path
     * @return {@code Object} value
     * @deprecated use getParamValue(String key) instead
     */
    @Deprecated
    Object getParamValue(final String key, String rootPath);

    /**
     * Set config value. Config node is searched by {@code SearchService}.
     *
     * @param key   ecos config key
     * @param value new value
     */
    void setValue(final String key, final String value);

    /**
     * Set a config value. Config node is searched through children of {@code rootPath} with
     * type {@code ConfigModel.TYPE_ECOS_CONFIG}.
     * <p>
     * You can use this method, while {@code SearchService} is not available,
     * for example - while repository is bootstrapping.
     *
     * @param key      ecos config key
     * @param value    new value
     * @param rootPath root path
     * @deprecated Use setValue(String key, String value) instead
     */
    @Deprecated
    void setValue(final String key, final String value, String rootPath);

    NodeRef createConfig(Map<QName, Serializable> properties);

    void removeConfig(final String key);

    Optional<NodeRef> getConfigRef(String key);

    NodeRef getConfigRoot();

}
