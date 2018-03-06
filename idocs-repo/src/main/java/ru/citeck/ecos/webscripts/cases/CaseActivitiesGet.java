package ru.citeck.ecos.webscripts.cases;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.cases.RemoteCaseModelService;
import ru.citeck.ecos.dto.CaseModelDto;
import ru.citeck.ecos.model.IdocsModel;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Case activities get web-script
 */
public class CaseActivitiesGet extends CaseActivityGet {

    /**
     * Execute implementation
     * @param req Http-request
     * @param status Status
     * @param cache Cache
     * @return Map of attributes
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        /** Load node reference */
        String nodeRefUuid = req.getParameter(PARAM_DOCUMENT_NODE_REF);
        if (nodeRefUuid == null) {
            return Collections.emptyMap();
        }
        nodeRefUuid = nodeRefUuid.startsWith(WORKSPACE_PREFIX) ? nodeRefUuid : (WORKSPACE_PREFIX + nodeRefUuid);
        NodeRef nodeRef = new NodeRef(nodeRefUuid);
        /** Load and transform data */
        Map<String, Object> resultMap = new HashMap<>();
        if (areCaseModelsSent(nodeRef)) {
            List<CaseModelDto> caseModels = remoteCaseModelService.getCaseModelsByNodeRef(nodeRef);
            resultMap.put("executionResult", createArrayNodeFromRemoteCaseModels(caseModels).toString());
            return resultMap;
        } else {
            List<NodeRef> caseModels = remoteCaseModelService.getCaseModelsByNode(nodeRef);
            resultMap.put("executionResult", createArrayNodeFromNodeRefs(caseModels).toString());
            return resultMap;
        }
    }

    /**
     * Create array node from remote case models
     * @param models Remote case models
     * @return Array node
     */
    private ArrayNode createArrayNodeFromRemoteCaseModels(List<CaseModelDto> models) {
        ArrayNode result = objectMapper.createArrayNode();
        for (CaseModelDto caseModelDto : models) {
            result.add(createFromRemoteCaseModel(caseModelDto));
        }
        return result;
    }

    /**
     * Create array node from node references
     * @param nodeRefs List of node reference
     * @return Array node
     */
    private ArrayNode createArrayNodeFromNodeRefs(List<NodeRef> nodeRefs) {
        ArrayNode result = objectMapper.createArrayNode();
        for (NodeRef nodeRef : nodeRefs) {
            result.add(createFromNodeReference(nodeRef));
        }
        return result;
    }

    /**
     * Check - are case models sent
     * @param nodeRef Node reference (document)
     * @return Check result
     */
    private boolean areCaseModelsSent(NodeRef nodeRef) {
        if (nodeRef == null) {
            return false;
        }
        if (!nodeService.exists(nodeRef)) {
            return true;
        }
        Boolean sentFlag = (Boolean) nodeService.getProperty(nodeRef, IdocsModel.PROP_CASE_MODELS_SENT);
        return sentFlag != null ? sentFlag : false;
    }

}
