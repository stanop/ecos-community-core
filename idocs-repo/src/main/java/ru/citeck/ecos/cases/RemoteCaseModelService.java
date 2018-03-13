package ru.citeck.ecos.cases;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.dto.CaseModelDto;

import java.util.List;

/**
 * Remote case model service interface
 */
public interface RemoteCaseModelService {

    /**
     * Get case models by node
     * @param nodeRef Node reference
     * @return List of case models node references
     */
    List<NodeRef> getCaseModelsByNode(NodeRef nodeRef);

    /**
     * Send and remove case models by document
     * @param documentRef Document reference
     */
    void sendAndRemoveCaseModelsByDocument(NodeRef documentRef);

    /**
     * Get case model by node uuid
     * @param nodeUUID Node uuid
     * @param verboseInformation Verbose information
     * @return Case model or null
     */
    CaseModelDto getCaseModelByNodeUUID(String nodeUUID,  Boolean verboseInformation);

    /**
     * Get case models by node reference
     * @param nodeRef Node reference
     * @param verboseInformation Verbose information
     * @return List of case model
     */
    List<CaseModelDto> getCaseModelsByNodeRef(NodeRef nodeRef, Boolean verboseInformation);
}
