package ru.citeck.ecos.icase;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roman Makarskiy
 */
public interface CaseStatusService {

    /**
     * Set case status to document.
     *
     * @param document - document nodeRef
     * @param caseStatus - case status nodeRef
     */
    public void setStatus(NodeRef document, NodeRef caseStatus);


    /**
     * Get case status by name.
     *
     * @param statusName - case status name
     * @return case status nodeRef
     */
    public NodeRef getStatusByName(String statusName);

}
