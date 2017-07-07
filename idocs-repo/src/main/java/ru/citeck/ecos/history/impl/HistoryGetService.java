package ru.citeck.ecos.history.impl;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;
import java.util.Map;

/**
 * History get service interface
 */
public interface HistoryGetService {

    /**
     * Get history events by document refs
     * @param documentNodeRef Document node reference
     * @return List of transformed events (for web script)
     */
    List<Map> getHistoryEventsByDocumentRef(NodeRef documentNodeRef);
}
