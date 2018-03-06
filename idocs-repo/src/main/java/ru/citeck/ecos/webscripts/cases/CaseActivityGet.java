package ru.citeck.ecos.webscripts.cases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.cases.RemoteCaseModelService;
import ru.citeck.ecos.dto.CaseModelDto;
import ru.citeck.ecos.dto.StageDto;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.StagesModel;
import ru.citeck.ecos.template.TemplateNodeService;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Case activity get web-script
 */
public class CaseActivityGet extends DeclarativeWebScript {

    /**
     * Workspace prefix
     */
    protected static final String WORKSPACE_PREFIX = "workspace://SpacesStore/";

    /*
     * Request params
     */
    protected static final String PARAM_DOCUMENT_NODE_REF = "nodeRef";

    /**
     * Object mapper
     */
    protected ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Date format
     */
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


    /**
     * Remote case model service
     */
    protected RemoteCaseModelService remoteCaseModelService;

    /**
     * Node service
     */
    protected NodeService nodeService;

    /**
     * Template node service
     */
    protected TemplateNodeService templateNodeService;

    /**
     * Permission service
     */
    protected PermissionService permissionService;

    /**
     * Service registry
     */
    protected ServiceRegistry serviceRegistry;

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
        if (nodeService.exists(nodeRef)) {
            ObjectNode objectNode = createFromNodeReference(nodeRef);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("executionResult", objectNode.toString());
            return resultMap;
        } else {
            CaseModelDto dto = remoteCaseModelService.getCaseModelByNodeUUID(nodeRef.getId());
            Map<String, Object> resultMap = new HashMap<>();
            if (dto != null) {
                resultMap.put("executionResult", "{}");
                return resultMap;
            } else {
                ObjectNode objectNode = createFromRemoteCaseModel(dto);
                resultMap.put("executionResult", objectNode.toString());
                return resultMap;
            }
        }

    }

    /**
     * Create object node from node reference
     * @param nodeRef Node reference
     * @return Object node
     */
    protected ObjectNode createFromNodeReference(NodeRef nodeRef) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        QName type = nodeService.getType(nodeRef);

        objectNode.put("nodeRef", nodeRef.toString());
        objectNode.put("type", type.toPrefixString(serviceRegistry.getNamespaceService()));
        objectNode.put("index", (Integer) nodeService.getProperty(nodeRef, ActivityModel.PROP_INDEX));
        objectNode.put("title", (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
        objectNode.put("typeTitle", templateNodeService.getClassTitle(type.toString()));
        objectNode.put("description", (String) nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));

        /** Dates */
        if (nodeService.getProperty(nodeRef, ActivityModel.PROP_PLANNED_START_DATE) != null) {
            objectNode.put("plannedStartDate", dateFormat.format((Date) nodeService.getProperty(nodeRef, ActivityModel.PROP_PLANNED_START_DATE)));
        }
        if (nodeService.getProperty(nodeRef, ActivityModel.PROP_PLANNED_END_DATE) != null) {
            objectNode.put("plannedEndDate", dateFormat.format((Date) nodeService.getProperty(nodeRef, ActivityModel.PROP_PLANNED_END_DATE)));
        }
        Date actualStartDate = (Date) nodeService.getProperty(nodeRef, ActivityModel.PROP_ACTUAL_START_DATE);
        if (actualStartDate != null) {
            objectNode.put("actualStartDate", dateFormat.format(actualStartDate));
        }
        Date actualEndDate = (Date) nodeService.getProperty(nodeRef, ActivityModel.PROP_ACTUAL_END_DATE);
        if (actualEndDate != null) {
            objectNode.put("actualEndDate", dateFormat.format(actualEndDate));
        }
        objectNode.put("expectedPerformTime", (Integer) nodeService.getProperty(nodeRef, ActivityModel.PROP_EXPECTED_PERFORM_TIME));

        /** Flags */
        Boolean manualStarted = nodeService.getProperty(nodeRef, ActivityModel.PROP_MANUAL_STARTED) != null ?
                (Boolean) nodeService.getProperty(nodeRef, ActivityModel.PROP_MANUAL_STARTED) : false;
        Boolean manualStopped = nodeService.getProperty(nodeRef, ActivityModel.PROP_MANUAL_STOPPED) != null ?
                (Boolean) nodeService.getProperty(nodeRef, ActivityModel.PROP_MANUAL_STOPPED) : false;
        Boolean repeatable = nodeService.getProperty(nodeRef, ActivityModel.PROP_REPEATABLE) != null ?
                (Boolean) nodeService.getProperty(nodeRef, ActivityModel.PROP_REPEATABLE) : false;

        objectNode.put("startable", (manualStarted && repeatable) || actualStartDate == null);
        objectNode.put("stoppable", manualStopped && actualStartDate != null && actualEndDate == null);

        AccessStatus writePermission = permissionService.hasPermission(nodeRef, "Write");
        objectNode.put("editable", writePermission != null ? (writePermission == AccessStatus.ALLOWED) : false);

        AccessStatus deletePermission = permissionService.hasPermission(nodeRef, "Delete");
        objectNode.put("removable", deletePermission != null ? (deletePermission == AccessStatus.ALLOWED) : false);
        objectNode.put("composite", nodeService.hasAspect(nodeRef, ActivityModel.ASPECT_HAS_ACTIVITIES));
        return objectNode;
    }

    /**
     * Create object node from remote data transfer object
     * @param caseModelDto Remote case model data transfer object
     * @return Object node
     */
    protected ObjectNode createFromRemoteCaseModel(CaseModelDto caseModelDto) {
        QName type = getCaseModelType(caseModelDto);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("nodeRef", caseModelDto.getNodeUUID());
        objectNode.put("type", type.toPrefixString(serviceRegistry.getNamespaceService())); // temp
        objectNode.put("index", caseModelDto.getIndex());
        objectNode.put("title", caseModelDto.getTitle());
        objectNode.put("typeTitle", templateNodeService.getClassTitle(type.toString())); // temp
        objectNode.put("description", caseModelDto.getDescription());
        objectNode.put("plannedStartDate", caseModelDto.getPlannedStartDate() != null ? dateFormat.format(caseModelDto.getPlannedStartDate()) : null);
        objectNode.put("plannedEndDate", caseModelDto.getPlannedEndDate() != null ? dateFormat.format(caseModelDto.getPlannedEndDate()) : null);
        objectNode.put("actualStartDate", caseModelDto.getActualStartDate() != null ? dateFormat.format(caseModelDto.getActualStartDate()) : null);
        objectNode.put("actualEndDate", caseModelDto.getActualEndDate() != null ? dateFormat.format(caseModelDto.getActualEndDate()) : null);
        objectNode.put("expectedPerformTime", caseModelDto.getExpectedPerformTime());

        /** Flags */
        objectNode.put("startable", false);
        objectNode.put("stoppable", false);
        objectNode.put("editable", false);
        objectNode.put("removable", false);
        objectNode.put("composite", caseModelDto.getHasChildCases());
        return objectNode;
    }

    /**
     * Get case model type
     * @param caseModelDto Case model data transfer object
     * @return Type
     */
    private QName getCaseModelType(CaseModelDto caseModelDto) {
        if (caseModelDto instanceof StageDto) {
            return StagesModel.TYPE_STAGE;
        }
        return ActivityModel.TYPE_ACTIVITY;
    }

    /** Setters */

    public void setRemoteCaseModelService(RemoteCaseModelService remoteCaseModelService) {
        this.remoteCaseModelService = remoteCaseModelService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setTemplateNodeService(TemplateNodeService templateNodeService) {
        this.templateNodeService = templateNodeService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
