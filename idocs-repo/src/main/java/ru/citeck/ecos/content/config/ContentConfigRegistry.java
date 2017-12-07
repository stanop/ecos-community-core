package ru.citeck.ecos.content.config;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.content.config.dao.ConfigDAO;
import ru.citeck.ecos.search.ftsquery.BinOperator;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.LazyNodeRef;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Config registry. Allow to cache content parsing result and update it when content was changed
 * @param <T> type of parsed content data
 *
 * @author Pavel Simonov
 */
public class ContentConfigRegistry<T> {

    private static final Log logger = LogFactory.getLog(ContentConfigRegistry.class);
    private static final int MEMORY_LEAK_THRESHOLD = 100000;

    private LazyNodeRef rootRef;

    private QName configNodeType;
    private QName contentFieldName = ContentModel.PROP_CONTENT;

    private Date lastRootChangedDate = new Date(0);

    private Map<NodeRef, ConfigData<T>> configDataByNode = new ConcurrentHashMap<>();
    private Map<Map<QName, Serializable>, List<ConfigData<T>>> configDataByKeys = new ConcurrentHashMap<>();

    protected NodeService nodeService;
    protected ContentService contentService;
    protected SearchService searchService;
    protected DictionaryService dictionaryService;

    private ConfigDAO<T> configDAO;

    /**
     * Get config data by node with content.
     * Field with content specified by contentFieldName
     * @param nodeRef node with content which can be parsed by ConfigDAO
     * @return ConfigData with nodeRef passed by argument and parsing result
     */
    public Optional<ConfigData<T>> getConfig(NodeRef nodeRef) {
        ConfigData<T> config = getConfigImpl(nodeRef);
        if (config.updateData()) {
            return Optional.of(config);
        } else {
            configDataByNode.remove(nodeRef);
            return Optional.empty();
        }
    }

    /**
     * Get config data by properties values
     */
    public Optional<ConfigData<T>> getConfig(Map<QName, Serializable> keys) {
        List<ConfigData<T>> configs = getConfigs(keys);
        return Optional.ofNullable(configs.size() > 0 ? configs.get(0) : null);
    }

    /**
     * Get configs data by properties values
     */
    public List<ConfigData<T>> getConfigs(Map<QName, Serializable> keys) {

        checkChangeDate();

        Map<QName, Serializable> localKeys = new HashMap<>(keys);

        List<ConfigData<T>> persisted = configDataByKeys.computeIfAbsent(localKeys, this::searchConfigs);
        List<ConfigData<T>> result = persisted.stream()
                                              .filter(ConfigData::updateData)
                                              .collect(Collectors.toList());

        if (configDataByKeys.size() > MEMORY_LEAK_THRESHOLD) {
            logger.warn("Cache size increased to " + MEMORY_LEAK_THRESHOLD + " elements. Seems it is memory leak");
        }

        if (persisted.size() != result.size()) {
            configDataByKeys.put(localKeys, result);
        }

        return result;
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        configDataByNode.clear();
        configDataByKeys.clear();
    }

    private List<ConfigData<T>> searchConfigs(Map<QName, Serializable> keys) {
        return FTSQuery.create()
                       .parent(rootRef.getNodeRef()).and()
                       .type(configNodeType).and()
                       .values(keys, BinOperator.AND, true)
                       .transactional()
                       .query(searchService)
                       .stream()
                       .map(this::getConfigImpl)
                       .collect(Collectors.toList());
    }

    private ConfigData<T> getConfigImpl(NodeRef nodeRef) {
        return configDataByNode.computeIfAbsent(nodeRef, r -> new ConfigData<>(r, this));
    }

    private void checkChangeDate() {
        Date lastChanged = (Date) nodeService.getProperty(rootRef.getNodeRef(), ContentModel.PROP_MODIFIED);
        if (lastChanged.getTime() > lastRootChangedDate.getTime()) {
            configDataByKeys.clear();
            lastRootChangedDate = lastChanged;
        }
    }

    public void setRootNode(LazyNodeRef rootRef) {
        this.rootRef = rootRef;
    }

    public void setConfigNodeType(QName configNodeType) {
        this.configNodeType = configNodeType;
    }

    public QName getConfigNodeType() {
        return configNodeType;
    }

    public void setConfigDAO(ConfigDAO<T> configDAO) {
        this.configDAO = configDAO;
    }

    public ConfigDAO<T> getConfigDAO() {
        return configDAO;
    }

    public void setContentFieldName(QName contentFieldName) {
        this.contentFieldName = contentFieldName;
    }

    public QName getContentFieldName() {
        return contentFieldName;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.contentService = serviceRegistry.getContentService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
    }
}
