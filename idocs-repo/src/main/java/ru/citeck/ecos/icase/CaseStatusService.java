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
     * @param status   - case status nodeRef
     */
    void setStatus(NodeRef document, NodeRef status);

    /**
     * Set case status to document
     *
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
     *
     * @return case status name or null if status doesn't exists in this case
     */
    String getStatus(NodeRef caseRef);

    /**
     * Get case status before
     *
     * @return case status before name or null if status doesn't exists in this case
     */
    String getStatusBefore(NodeRef caseRef);

    /**
     * Get case status reference
     *
     * @return case status nodeRef or null if status doesn't exists in this case
     */
    NodeRef getStatusRef(NodeRef caseRef);

    /**
     * Get case status before reference
     *
     * @return case status before nodeRef or null if status doesn't exists in this case
     */
    NodeRef getStatusBeforeRef(NodeRef caseRef);

    /**
     * Get case status reference from primary parent
     *
     * @param childRef - nodeRef of child
     * @return case status nodeRef or null if status doesn't exists in the primary parent
     */
    NodeRef getStatusRefFromPrimaryParent(NodeRef childRef);
}
