package ru.citeck.ecos.webscripts.history;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.constants.DocumentHistoryConstants;
import ru.citeck.ecos.history.HistoryRemoteService;
import ru.citeck.ecos.history.impl.HistoryGetService;
import ru.citeck.ecos.model.IdocsModel;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Document history get web script
 */
public class DocumentHistoryGet extends DeclarativeWebScript {

    /**
     * Exclude event types
     */
    private static final String EXCLUDE_EVENT_TYPES_PROPERTY = "ecos.citeck.history.exclude.types";
    private static final String[] DEFAULT_EXCLUDE_EVENT_TYPES = {
        "task.create", "task.assign", "task.complete" , "workflow.start" , "workflow.end", "workflow.end.cancelled", "unknown-user"
    };

    /**
     * Date-time format
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Properties constants
     */
    private static final String ENABLED_REMOTE_HISTORY_SERVICE = "ecos.citeck.history.service.enabled";

    /** Constants */
    public static final String ALFRESCO_NAMESPACE = "http://www.alfresco.org/model/content/1.0";
    public static final String HISTORY_PROPERTY_NAME = "history";
    public static final String ATTRIBUTES_PROPERTY_NAME = "attributes";

    /**
     * Request params
     */
    private static final String DOCUMENT_NODE_REF = "nodeRef";

    /**
     * Global properties
     */
    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    /**
     * Services
     */
    private HistoryRemoteService historyRemoteService;

    private PersonService personService;

    private NodeService nodeService;

    private HistoryGetService historyGetService;

    @Autowired
    private ServiceRegistry serviceRegistry;

    /**
     * Execute implementation
     * @param req Http-request
     * @param status Status
     * @param cache Cache
     * @return Map of attributes
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        String nodeRefUuid = req.getParameter(DOCUMENT_NODE_REF);
        /** Check history event status */
        NodeRef documentRef = new NodeRef(nodeRefUuid);
        Boolean useNewHistory = (Boolean) nodeService.getProperty(documentRef, IdocsModel.DOCUMENT_USE_NEW_HISTORY);
        if ((useNewHistory == null || !useNewHistory) && isEnabledRemoteHistoryService()) {
            historyRemoteService.sendHistoryEventsByDocumentToRemoteService(documentRef);
        }
        /** Load data */
        List historyRecordMaps = null;
        if (isEnabledRemoteHistoryService()) {
            historyRecordMaps = historyRemoteService.getHistoryRecords(documentRef.getId());
        } else {
            historyRecordMaps = historyGetService.getHistoryEventsByDocumentRef(documentRef);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("jsonResult", createJsonResponse(historyRecordMaps));
        return result;
    }

    /**
     * Check - is remote history service enabled
     * @return Check result
     */
    private Boolean isEnabledRemoteHistoryService() {
        String propertyValue = properties.getProperty(ENABLED_REMOTE_HISTORY_SERVICE);
        if (propertyValue == null) {
            return false;
        } else {
            return Boolean.valueOf(propertyValue);
        }
    }


    /**
     * Create json response
     * @param historyRecordMaps History records maps
     * @return Json string
     */
    private String createJsonResponse(List<Map> historyRecordMaps) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode resultObjectNode = objectMapper.createObjectNode();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        /** Transform records */
        for (Map<String, Object> historyRecordMap : historyRecordMaps ) {
            String eventType = (String) historyRecordMap.get(DocumentHistoryConstants.EVENT_TYPE.getValue());
            if (ArrayUtils.contains(getExcludeEventTypes(), eventType)) {
                continue;
            }
            ObjectNode recordObjectNode = objectMapper.createObjectNode();
            recordObjectNode.put(DocumentHistoryConstants.NODE_REF.getKey(), (String) historyRecordMap.get(DocumentHistoryConstants.NODE_REF.getValue()));
            ObjectNode attributesNode = objectMapper.createObjectNode();
            /** Populate object */
            Date date = new Date((Long) historyRecordMap.get(DocumentHistoryConstants.DOCUMENT_DATE.getValue()));
            ZoneOffset offset = ZoneOffset.systemDefault().getRules().getOffset(Instant.now());
            OffsetDateTime offsetDateTime = date.toInstant().atOffset(offset);
            attributesNode.put(DocumentHistoryConstants.DOCUMENT_DATE.getKey(), offsetDateTime.toString());
            attributesNode.put(DocumentHistoryConstants.DOCUMENT_VERSION.getKey(), (String) historyRecordMap.get(DocumentHistoryConstants.DOCUMENT_VERSION.getValue()));
            attributesNode.put(DocumentHistoryConstants.COMMENTS.getKey(), (String)historyRecordMap.get(DocumentHistoryConstants.COMMENTS.getValue()));
            attributesNode.put(DocumentHistoryConstants.EVENT_TYPE.getKey(), (String) historyRecordMap.get(DocumentHistoryConstants.EVENT_TYPE.getValue()));
            attributesNode.put(DocumentHistoryConstants.TASK_ROLE.getKey(), (String) historyRecordMap.get(DocumentHistoryConstants.TASK_ROLE.getValue()));
            attributesNode.put(DocumentHistoryConstants.TASK_OUTCOME.getKey(), (String) historyRecordMap.get(DocumentHistoryConstants.TASK_OUTCOME.getValue())); // good
            String taskType = (String) historyRecordMap.get(DocumentHistoryConstants.TASK_TYPE.getValue());
            if (StringUtils.isNotEmpty(taskType)) {
                QName taskTypeValue = QName.createQName(taskType);
                if (taskTypeValue != null) {
                    ObjectNode taskTypeNode = objectMapper.createObjectNode();
                    taskTypeNode.put("fullQName", taskType.toString());
                    taskTypeNode.put("shortQName", taskTypeValue.toPrefixString(serviceRegistry.getNamespaceService()));
                    attributesNode.put(DocumentHistoryConstants.TASK_TYPE.getKey(), taskTypeNode);
                }

            }
            /** User */
            NodeRef userNodeRef = personService.getPerson((String) historyRecordMap.get(DocumentHistoryConstants.EVENT_INITIATOR.getValue()));
            if (userNodeRef != null) {
                attributesNode.put(DocumentHistoryConstants.EVENT_INITIATOR.getKey(), createUserNode(userNodeRef));
            }
            /** Add history node to result */
            recordObjectNode.put(ATTRIBUTES_PROPERTY_NAME, attributesNode);
            arrayNode.add(recordObjectNode);
        }
        resultObjectNode.put(HISTORY_PROPERTY_NAME, arrayNode);
        return resultObjectNode.toString();
    }

    /**
     * Create user node
     * @param userNodeRef User node reference
     * @return Array node
     */
    private ArrayNode createUserNode(NodeRef userNodeRef) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode result = objectMapper.createArrayNode();
        ObjectNode userNode = objectMapper.createObjectNode();
        userNode.put("nodeRef", userNodeRef.toString());
        userNode.put("type", "cm:person");
        userNode.put("cm:userName", (String) nodeService.getProperty(userNodeRef, QName.createQName(ALFRESCO_NAMESPACE, "userName")));
        userNode.put("cm:firstName", (String) nodeService.getProperty(userNodeRef, QName.createQName(ALFRESCO_NAMESPACE, "firstName")));
        userNode.put("cm:lastName", (String) nodeService.getProperty(userNodeRef, QName.createQName(ALFRESCO_NAMESPACE, "lastName")));
        userNode.put("cm:middleName", (String) nodeService.getProperty(userNodeRef, QName.createQName(ALFRESCO_NAMESPACE, "middleName")));
        String displayName = userNode.get("cm:lastName") + " " + userNode.get("cm:firstName") + " " + userNode.get("cm:middleName");
        userNode.put("displayName", displayName.trim());
        result.add(userNode);
        return result;

    }

    /**
     * Get exclude event types
     * @return
     */
    private String[] getExcludeEventTypes() {
        String rawValue = properties.getProperty(EXCLUDE_EVENT_TYPES_PROPERTY);
        if (rawValue == null) {
            return DEFAULT_EXCLUDE_EVENT_TYPES;
        } else {
            return rawValue.trim().split(",");
        }
    }

    public void setHistoryRemoteService(HistoryRemoteService historyRemoteService) {
        this.historyRemoteService = historyRemoteService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setHistoryGetService(HistoryGetService historyGetService) {
        this.historyGetService = historyGetService;
    }
}
