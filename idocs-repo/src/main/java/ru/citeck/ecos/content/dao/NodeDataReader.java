package ru.citeck.ecos.content.dao;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Reader used to retrieve data from node when content is missed
 * Primary goal of this interface is backward compatibility with nodes without content
 * @param <T> type of retrieved data
 */
public interface NodeDataReader<T> {

    T getData(NodeRef nodeRef);

}
