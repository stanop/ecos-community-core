package ru.citeck.ecos.content;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.content.dao.ContentDAO;
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
public class RepoContentDAO<T> {

    private static final Log logger = LogFactory.getLog(RepoContentDAO.class);
    private static final int MEMORY_LEAK_THRESHOLD = 100000;

    private LazyNodeRef rootRef;

    private QName configNodeType;
    private QName contentFieldName = ContentModel.PROP_CONTENT;
    private QName childAssocType = ContentModel.ASSOC_CONTAINS;

    private Date lastRootChangedDate = new Date(0);

    private Map<NodeRef, ContentData<T>> configDataByNode = new ConcurrentHashMap<>();
    private Map<Map<QName, Serializable>, List<ContentData<T>>> configDataByKeys = new ConcurrentHashMap<>();

    protected NodeService nodeService;
    protected ContentService contentService;
    protected SearchService searchService;
    protected DictionaryService dictionaryService;

    private ContentDAO<T> contentDAO;

    /**
     * Get config data by node with content.
     * Field with content specified by contentFieldName
     * @param nodeRef node with content which can be parsed by ConfigDAO
     * @return ConfigData with nodeRef passed by argument and parsing result
     */
    public Optional<ContentData<T>> getFirstContentData(NodeRef nodeRef) {
        ContentData<T> config = getContentDataImpl(nodeRef);
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
    public Optional<ContentData<T>> getFirstContentData(Map<QName, Serializable> keys) {
        List<ContentData<T>> configs = getContentData(keys);
        return Optional.ofNullable(configs.size() > 0 ? configs.get(0) : null);
    }

    /**
     * Get configs data by properties values
     */
    public List<ContentData<T>> getContentData(Map<QName, Serializable> keys) {

        checkChangeDate();

        Map<QName, Serializable> localKeys = new HashMap<>(keys);

        List<ContentData<T>> persisted = configDataByKeys.computeIfAbsent(localKeys, this::searchData);
        List<ContentData<T>> result = persisted.stream()
                                              .filter(ContentData::updateData)
                                              .collect(Collectors.toList());

        if (configDataByKeys.size() > MEMORY_LEAK_THRESHOLD) {
            logger.warn("Cache size increased to " + MEMORY_LEAK_THRESHOLD + " elements. Seems it is memory leak");
        }

        if (persisted.size() != result.size()) {
            configDataByKeys.put(localKeys, result);
        }

        return result;
    }

    public NodeRef createNode(Map<QName, Serializable> properties) {
        String name = (String) properties.get(ContentModel.PROP_NAME);
        if (name == null) {
            name = "contentData";
        }
        QName assocName = QName.createQName(childAssocType.getNamespaceURI(), name);
        return nodeService.createNode(rootRef.getNodeRef(),
                                      childAssocType,
                                      assocName,
                                      configNodeType,
                                      properties).getChildRef();
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        configDataByNode.clear();
        configDataByKeys.clear();
    }

    private List<ContentData<T>> searchData(Map<QName, Serializable> keys) {

        Map<QName, Serializable> notNullProps = new HashMap<>();
        Set<QName> nullProps = new HashSet<>();

        for (QName key : keys.keySet()) {
            if (keys.get(key) != null) {
                notNullProps.put(key, keys.get(key));
            } else {
                nullProps.add(key);
            }
        }

        return FTSQuery.create()
                       .parent(rootRef.getNodeRef()).and()
                       .type(configNodeType).and()
                       .values(notNullProps, BinOperator.AND, true)
                       .transactional()
                       .query(searchService)
                       .stream()
                       .filter(ref -> {
                           for (QName propName : nullProps) {
                               Serializable value = nodeService.getProperty(ref, propName);
                               if (value != null && (!(value instanceof String) ||
                                                     StringUtils.isNotBlank((String) value))) {
                                   return false;
                               }
                           }
                           return true;
                       })
                       .map(this::getContentDataImpl)
                       .collect(Collectors.toList());
    }

    private ContentData<T> getContentDataImpl(NodeRef nodeRef) {
        return configDataByNode.computeIfAbsent(nodeRef, r -> new ContentData<>(r, this));
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

    public void setContentDAO(ContentDAO<T> contentDAO) {
        this.contentDAO = contentDAO;
    }

    public ContentDAO<T> getContentDAO() {
        return contentDAO;
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

    public void setChildAssocType(QName childAssocType) {
        this.childAssocType = childAssocType;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.contentService = serviceRegistry.getContentService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
    }
}
