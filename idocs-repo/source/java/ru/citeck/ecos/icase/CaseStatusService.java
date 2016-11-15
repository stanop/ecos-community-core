package ru.citeck.ecos.icase;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roman.Makarskiy on 11/14/2016.
 */
public interface CaseStatusService {

    /**
     * Set case status to document.
     *
     * @param document - document nodeRef
     * @param caseStatus - case status nodeRef
     */
    public void setCaseStatus(NodeRef document, NodeRef caseStatus);


    /**
     * Get case status by name.
     *
     * @param statusName - case status name
     * @return case status nodeRef
     */
    public NodeRef getCaseStatusByName(String statusName);

}
