package ru.citeck.ecos.cases.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.cases.RemoteCaseModelService;
import ru.citeck.ecos.dto.CaseModelDto;
import ru.citeck.ecos.dto.StageDto;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.model.StagesModel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Remote case model service
 */
public class RemoteCaseModelServiceImpl implements RemoteCaseModelService {

    /**
     * Properties keys
     */
    private static final String CASE_MODELS_SERVICE_HOST = "citeck.remote.case.service.host";

    /**
     * Service methods constants
     */
    private static final String SAVE_CASE_MODELS_METHOD = "/case_models/save_case_models";
    private static final String GET_CASE_MODEL_BY_NODE_ID = "/case_models/case_model_by_id/";
    private static final String GET_CASE_MODELS_BY_DOCUMENT_ID = "/case_models/case_models_by_document_id/";
    private static final String GET_CASE_MODELS_BY_PARENT_CASE_MODEL_ID = "/case_models/case_models_by_parent_case_models_id/";

    /**
     * Object mapper
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Date format
     */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Datetime format
     */
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Dictionary service
     */
    private DictionaryService dictionaryService;

    /**
     * Transaction service
     */
    private TransactionService transactionService;

    /**
     * Res template
     */
    private RestTemplate restTemplate;


    /**
     * Global properties
     */
    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    /**
     * Get case models by node
     * @param nodeRef Node reference
     * @return List of case models node references
     */
    @Override
    public List<NodeRef> getCaseModelsByNode(NodeRef nodeRef) {
        List<ChildAssociationRef> caseAssocs = nodeService.getChildAssocs(nodeRef, null, ActivityModel.ASSOC_ACTIVITIES);
        List<NodeRef> result = new ArrayList<>(caseAssocs.size());
        for (ChildAssociationRef caseAssoc : caseAssocs) {
            NodeRef caseRef = caseAssoc.getChildRef();
            result.add(caseRef);
        }
        return result;
    }

    /**
     * Send and remove case models by document
     * @param documentRef Document reference
     */
    @Override
    public void sendAndRemoveCaseModelsByDocument(NodeRef documentRef) {
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            /** Create json array */
            for (NodeRef caseRef : getCaseModelsByNode(documentRef)) {
                ObjectNode objectNode = createObjectNodeFromCaseModel(caseRef);
                if (objectNode != null) {
                    arrayNode.add(objectNode);
                }
                nodeService.deleteNode(caseRef);
            }
            nodeService.setProperty(documentRef, IdocsModel.PROP_CASE_MODELS_SENT, true);
            /** Send request */
            postForObject(SAVE_CASE_MODELS_METHOD, arrayNode.toString(), String.class);
            return null;
        });
    }

    /**
     * Create object node from case model
     * @param caseModelRef Case model reference
     * @return Object node
     */
    private ObjectNode createObjectNodeFromCaseModel(NodeRef caseModelRef) {
        if (caseModelRef == null) {
            return null;
        }
        /** Base properties */
        ObjectNode objectNode = objectMapper.createObjectNode();
        String dtoType = getCaseModelType(nodeService.getType(caseModelRef));
        objectNode.put("dtoType", dtoType);
        if (nodeService.getProperty(caseModelRef, ContentModel.PROP_CREATED) != null) {
            objectNode.put("created", dateTimeFormat.format((Date) nodeService.getProperty(caseModelRef, ContentModel.PROP_CREATED)));
        }
        objectNode.put("creator", (String) nodeService.getProperty(caseModelRef, ContentModel.PROP_CREATOR));
        if (nodeService.getProperty(caseModelRef, ContentModel.PROP_MODIFIED) != null) {
            objectNode.put("modified", dateTimeFormat.format((Date) nodeService.getProperty(caseModelRef, ContentModel.PROP_MODIFIED)));
        }
        objectNode.put("modifier", (String) nodeService.getProperty(caseModelRef, ContentModel.PROP_MODIFIER));
        objectNode.put("title", (String) nodeService.getProperty(caseModelRef, ContentModel.PROP_TITLE));
        objectNode.put("description", (String) nodeService.getProperty(caseModelRef, ContentModel.PROP_DESCRIPTION));
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(caseModelRef);
        if (parentAssoc != null) {
            if (parentAssoc.getParentRef() != null) {
                if (dictionaryService.isSubClass(
                        nodeService.getType(parentAssoc.getParentRef()),
                        IdocsModel.TYPE_DOC)) {
                    objectNode.put("documentId", parentAssoc.getParentRef().getId());
                }
            }
        }
        objectNode.put("nodeUUID", caseModelRef.getId());
        if (nodeService.getProperty(caseModelRef, ActivityModel.PROP_PLANNED_START_DATE) != null) {
            objectNode.put("plannedStartDate", dateFormat.format((Date) nodeService.getProperty(caseModelRef, ActivityModel.PROP_PLANNED_START_DATE)));
        }
        if (nodeService.getProperty(caseModelRef, ActivityModel.PROP_PLANNED_END_DATE) != null) {
            objectNode.put("plannedEndDate", dateFormat.format((Date) nodeService.getProperty(caseModelRef, ActivityModel.PROP_PLANNED_END_DATE)));
        }
        if (nodeService.getProperty(caseModelRef, ActivityModel.PROP_ACTUAL_START_DATE) != null) {
            objectNode.put("actualStartDate", dateFormat.format((Date) nodeService.getProperty(caseModelRef, ActivityModel.PROP_ACTUAL_START_DATE)));
        }
        if (nodeService.getProperty(caseModelRef, ActivityModel.PROP_ACTUAL_END_DATE) != null) {
            objectNode.put("actualEndDate", dateFormat.format((Date) nodeService.getProperty(caseModelRef, ActivityModel.PROP_ACTUAL_END_DATE)));
        }
        objectNode.put("expectedPerformTime", (Integer) nodeService.getProperty(caseModelRef, ActivityModel.PROP_EXPECTED_PERFORM_TIME));
        objectNode.put("manualStarted", (Boolean) nodeService.getProperty(caseModelRef, ActivityModel.PROP_MANUAL_STARTED));
        objectNode.put("manualStopped", (Boolean) nodeService.getProperty(caseModelRef, ActivityModel.PROP_MANUAL_STOPPED));
        objectNode.put("index", (Integer) nodeService.getProperty(caseModelRef, ActivityModel.PROP_INDEX));
        objectNode.put("autoEvents", (Boolean) nodeService.getProperty(caseModelRef, ActivityModel.PROP_AUTO_EVENTS));
        objectNode.put("repeatable", (Boolean) nodeService.getProperty(caseModelRef, ActivityModel.PROP_REPEATABLE));
        objectNode.put("typeVersion", (Integer) nodeService.getProperty(caseModelRef, ActivityModel.PROP_TYPE_VERSION));
        /** Child activities */
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (NodeRef childCaseRef : getCaseModelsByNode(caseModelRef)) {
            ObjectNode childCaseNode = createObjectNodeFromCaseModel(childCaseRef);
            if (childCaseNode != null) {
                arrayNode.add(childCaseNode);
            }
            nodeService.deleteNode(childCaseRef);
        }
        objectNode.put("childCases", arrayNode);
        return objectNode;
    }

    /**
     * Fill additional info
     * @param dtoType Dto type
     * @param caseModelRef Case model node reference
     * @param objectNode Object node
     */
    private void fillAdditionalInfo(String dtoType, NodeRef caseModelRef, ObjectNode objectNode) {
        if (StageDto.DTO_TYPE.equals(dtoType)) {
            fillAdditionalStageInfo(caseModelRef, objectNode);
        }
    }

    /**
     * Fill additional stage info
     * @param caseModelRef Case model node reference
     * @param objectNode Object node
     */
    private void fillAdditionalStageInfo(NodeRef caseModelRef, ObjectNode objectNode) {
        objectNode.put("documentStatus", (String) nodeService.getProperty(caseModelRef, StagesModel.PROP_DOCUMENT_STATUS));
    }

    /**
     * Get case model type
     * @param nodeType Node type
     * @return Case model type as string
     */
    private String getCaseModelType(QName nodeType) {
        if (nodeType == null) {
            throw new RuntimeException("Node Type must not be null");
        }
        if (dictionaryService.isSubClass(nodeType, StagesModel.TYPE_STAGE)) {
            return StageDto.DTO_TYPE;
        }
        return CaseModelDto.DTO_TYPE;
    }

    /**
     * Get case model by node uuid
     * @param nodeUUID Node uuid
     * @return Case model or null
     */
    @Override
    public CaseModelDto getCaseModelByNodeUUID(String nodeUUID) {
        return getForObject(GET_CASE_MODEL_BY_NODE_ID + nodeUUID, new LinkedMultiValueMap<>(), CaseModelDto.class);
    }

    /**
     * Get case models by node reference
     * @param nodeRef Node reference
     * @return List of case model
     */
    @Override
    public List<CaseModelDto> getCaseModelsByNodeRef(NodeRef nodeRef) {
        if (nodeRef == null) {
            return Collections.emptyList();
        }
        if (nodeService.exists(nodeRef)) {
            if (dictionaryService.isSubClass(
                    nodeService.getType(nodeRef),
                    IdocsModel.TYPE_DOC)) {
                String result = getForObject(GET_CASE_MODELS_BY_DOCUMENT_ID + nodeRef.getId(), new LinkedMultiValueMap<>(), String.class);
                return convertJsonToCaseModels(result);
            } else {
                return Collections.emptyList();
            }
        } else {
            String result = getForObject(GET_CASE_MODELS_BY_PARENT_CASE_MODEL_ID + nodeRef.getId(), new LinkedMultiValueMap<>(), String.class);
            return convertJsonToCaseModels(result);
        }
    }

    /**
     * Convert json to case models
     * @param jsonString Json string
     * @return List of case models
     */
    private List<CaseModelDto> convertJsonToCaseModels(String jsonString) {
        try {
            CaseModelDto[] parseResult = objectMapper.readValue(jsonString, CaseModelDto[].class);
            return Arrays.asList(parseResult);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Get for object
     * @param serviceMethod Service method
     * @param params Params
     * @param objectClass Return object class
     * @param <T>
     * @return Return object
     */
    protected <T> T getForObject(String serviceMethod, MultiValueMap<String, Object> params, Class<T> objectClass) {
        return restTemplate.getForObject(properties.getProperty(CASE_MODELS_SERVICE_HOST) + serviceMethod, objectClass, params);
    }

    /**
     * Post for object
     * @param serviceMethod Service method
     * @param requestBody Request body
     * @param objectClass Return object class
     * @param <T>
     * @return Return object
     */
    protected <T> T postForObject(String serviceMethod, String requestBody, Class<T> objectClass) {
        /** Header */
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        /** Body params */
        HttpEntity requestEntity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(properties.getProperty(CASE_MODELS_SERVICE_HOST) + serviceMethod, requestEntity, objectClass);
    }

    /**
     * Set node service
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set dictionary service
     * @param dictionaryService Dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set transaction service
     * @param transactionService Transaction service
     */
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Set rest template
     * @param restTemplate Rest template
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}

