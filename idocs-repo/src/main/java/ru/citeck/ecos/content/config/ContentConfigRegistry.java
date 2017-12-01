package ru.citeck.ecos.content.config;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.content.config.parser.ConfigParser;
import ru.citeck.ecos.search.ftsquery.BinOperator;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.LazyNodeRef;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ContentConfigRegistry<T> {

    private LazyNodeRef rootRef;
    private QName configNodeType;

    private Date lastRootChangedDate = new Date(0);

    private Map<NodeRef, ConfigData<T>> configDataByNode = new ConcurrentHashMap<>();
    private Map<Map<QName, Serializable>, List<ConfigData<T>>> configDataByKeys = new ConcurrentHashMap<>();

    protected NodeService nodeService;
    protected SearchService searchService;
    protected ContentService contentService;
    protected DictionaryService dictionaryService;

    private ConfigParser<T> parser;

    public ConfigData<T> getConfig(Map<QName, Serializable> keys) {
        List<ConfigData<T>> configs = getConfigsImpl(keys);
        return configs.size() > 0 ? configs.get(0).updateData(parser, contentService) : null;
    }

    public List<ConfigData<T>> getConfigs(Map<QName, Serializable> keys) {
        List<ConfigData<T>> configs = getConfigsImpl(keys);
        configs.forEach(d -> d.updateData(parser, contentService));
        return configs;
    }

    private List<ConfigData<T>> getConfigsImpl(Map<QName, Serializable> keys) {
        checkChangeDate();
        return configDataByKeys.computeIfAbsent(keys, this::searchConfigs);
    }

    private List<ConfigData<T>> searchConfigs(Map<QName, Serializable> keys) {
        return FTSQuery.create()
                       .parent(rootRef.getNodeRef()).and()
                       .type(configNodeType).and()
                       .values(keys, BinOperator.AND, true)
                       .transactional()
                       .query(searchService)
                       .stream()
                       .map(r -> configDataByNode.computeIfAbsent(r, ConfigData<T>::new))
                       .collect(Collectors.toList());
    }

    private void checkChangeDate() {
        Date lastChanged = (Date) nodeService.getProperty(rootRef.getNodeRef(), ContentModel.PROP_MODIFIED);
        if (lastChanged.getTime() > lastRootChangedDate.getTime()) {
            synchronized (this) {
                if (lastChanged.getTime() > lastRootChangedDate.getTime()) {
                    configDataByKeys.clear();
                    lastRootChangedDate = lastChanged;
                }
            }
        }
    }

    public void setRootNode(LazyNodeRef rootRef) {
        this.rootRef = rootRef;
    }

    public void setConfigNodeType(QName configNodeType) {
        this.configNodeType = configNodeType;
    }

    public void setParser(ConfigParser<T> parser) {
        this.parser = parser;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.contentService = serviceRegistry.getContentService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
    }
}
