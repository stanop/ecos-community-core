package ru.citeck.ecos.cases.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import ru.citeck.ecos.cases.RemoteCaseModelService;
import ru.citeck.ecos.cases.RemoteRestoreCaseModelService;
import ru.citeck.ecos.dto.CaseModelDto;

import java.util.List;

/**
 * Remote restore case model service
 */
public class RemoteRestoreCaseModelServiceImpl implements RemoteRestoreCaseModelService {

    private static final String WORKSPACE_PREFIX = "workspace://SpacesStore/";

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Remote case model service
     */
    private RemoteCaseModelService remoteCaseModelService;

    /**
     * Restore case models
     * @param documentRef Document reference
     */
    @Override
    public void restoreCaseModels(NodeRef documentRef) {
        List<CaseModelDto> caseModels = remoteCaseModelService.getCaseModelsByNodeRef(documentRef, true);
        for (CaseModelDto caseModelDto : caseModels) {
            List<CaseModelDto> childCaseModels = loadChildCases(caseModelDto);
            caseModelDto.setChildCases(childCaseModels);
        }
        /** Restore data */
    }

    /**
     * Load child cases
     * @param parentCaseModel Parent case model
     * @return List of child cases
     */
    private List<CaseModelDto> loadChildCases(CaseModelDto parentCaseModel) {
        List<CaseModelDto> caseModels = remoteCaseModelService.getCaseModelsByNodeRef(new NodeRef(WORKSPACE_PREFIX + parentCaseModel.getNodeUUID()), true);
        for (CaseModelDto caseModelDto : caseModels) {
            List<CaseModelDto> childCaseModels = loadChildCases(caseModelDto);
            caseModelDto.setChildCases(childCaseModels);
        }
        return caseModels;
    }
    /**
     * Set node service
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set remote case model service
     * @param remoteCaseModelService Remote case model service
     */
    public void setRemoteCaseModelService(RemoteCaseModelService remoteCaseModelService) {
        this.remoteCaseModelService = remoteCaseModelService;
    }
}
