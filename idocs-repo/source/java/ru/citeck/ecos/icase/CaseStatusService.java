package ru.citeck.ecos.icase;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roman Makarskiy
 * @author Pavel Simonov
 */
public interface CaseStatusService {

    /**
     * Set case status to document.
     *
     * @param document - document nodeRef
     * @param status - case status nodeRef
     */
    void setStatus(NodeRef document, NodeRef status);

    /**
     * Set case status to document
     * @throws IllegalArgumentException if status not found in system
     */
    void setStatus(NodeRef document, String status);

    /**
     * Get case status by name.
     *
     * @param statusName - case status name
     * @return case status nodeRef
     */
    NodeRef getStatusByName(String statusName);

    /**
     * Get case status
     * @return case status name or null if status doesn't exists in this case
     */
    String getStatus(NodeRef caseRef);

    /**
     * Get case status reference
     * @return case status nodeRef or null if status doesn't exists in this case
     */
    NodeRef getStatusRef(NodeRef caseRef);
}
