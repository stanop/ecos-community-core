package ru.citeck.ecos.content;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.content.dao.ContentDAO;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RepoContentDAO<T> {

    /**
     * Get config data by node with content.
     * Field with content specified by contentFieldName
     * @param nodeRef node with content which can be parsed by ConfigDAO
     * @return ConfigData with nodeRef passed by argument and parsing result
     */
    Optional<ContentData<T>> getContentData(NodeRef nodeRef);

    /**
     * Get configs data by properties values
     */
    List<ContentData<T>> getContentData(Map<QName, Serializable> keys, boolean ignoreWithoutData);

    NodeRef createNode(Map<QName, Serializable> properties);

    ContentDAO<T> getContentDAO();

    default List<ContentData<T>> getContentData(Map<QName, Serializable> keys) {
        return getContentData(keys, true);
    }

    default Optional<ContentData<T>> getFirstContentData(Map<QName, Serializable> keys) {
        return getFirstContentData(keys, true);
    }

    default Optional<ContentData<T>> getFirstContentData(Map<QName, Serializable> keys, boolean ignoreWithoutData) {
        List<ContentData<T>> configs = getContentData(keys, ignoreWithoutData);
        return Optional.ofNullable(configs.size() > 0 ? configs.get(0) : null);
    }

    void clearCache();
}
