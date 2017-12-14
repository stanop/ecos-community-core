package ru.citeck.ecos.flowable.listeners.global.impl.task.create;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.DelegateTask;
import ru.citeck.ecos.deputy.DeputyService;
import ru.citeck.ecos.flowable.listeners.global.GlobalAssignmentTaskListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalCompleteTaskListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalCreateTaskListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.history.HistoryEventType;
import ru.citeck.ecos.history.HistoryService;
import ru.citeck.ecos.model.CasePerformModel;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.role.CaseRoleService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task history listener
 */
public class TaskHistoryListener implements GlobalCreateTaskListener, GlobalAssignmentTaskListener, GlobalCompleteTaskListener {

    private static final Log logger = LogFactory.getLog(TaskHistoryListener.class);
    /**
     * Constants
     */
    private static final String ENGINE_PREFIX = "flowable$";
    public static final String VAR_ADDITIONAL_EVENT_PROPERTIES = "event_additionalProperties";

    private static final Map<String, String> eventNames;
    static {
        eventNames = new HashMap<String, String>(3);
        eventNames.put(EVENTNAME_CREATE, HistoryEventType.TASK_CREATE);
        eventNames.put(EVENTNAME_ASSIGNMENT, HistoryEventType.TASK_ASSIGN);
        eventNames.put(EVENTNAME_COMPLETE, HistoryEventType.TASK_COMPLETE);
    }

    /**
     * Services
     */
    private NodeService nodeService;
    private HistoryService historyService;
    private NamespaceService namespaceService;
    private AuthorityService authorityService;
    private DeputyService deputyService;
    private CaseRoleService caseRoleService;
    private WorkflowService workflowService;
    private List<String> panelOfAuthorized;
    private WorkflowQNameConverter qNameConverter;

    /**
     * Property names
     */
    private String VAR_OUTCOME_PROPERTY_NAME;
    private String VAR_COMMENT;
    private String VAR_DESCRIPTION;


    /**
     * Init
     */
    public void init() {
        qNameConverter = new WorkflowQNameConverter(namespaceService);
        VAR_OUTCOME_PROPERTY_NAME = qNameConverter.mapQNameToName(WorkflowModel.PROP_OUTCOME_PROPERTY_NAME);
        VAR_COMMENT = qNameConverter.mapQNameToName(WorkflowModel.PROP_COMMENT);
        VAR_DESCRIPTION = qNameConverter.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
    }

    /**
     * Notify
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = eventNames.get(delegateTask.getEventName());
        if(eventName == null) {
            logger.warn("Unsupported flowable task event: " + delegateTask.getEventName());
            return;
        }
        NodeRef document = FlowableListenerUtils.getDocument(delegateTask.getExecution(), nodeService);
        if (document == null) {
            return;
        }

        /**
         * Collect properties
         */
        Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>();
        QName taskType = QName.createQName((String) delegateTask.getVariable(ActivitiConstants.PROP_TASK_FORM_KEY), namespaceService);
        QName outcomeProperty = (QName) delegateTask.getVariable(VAR_OUTCOME_PROPERTY_NAME);
        if(outcomeProperty == null) {
            outcomeProperty = WorkflowModel.PROP_OUTCOME;
        }
        String taskOutcome = (String) delegateTask.getVariable(qNameConverter.mapQNameToName(outcomeProperty));
        String taskComment = (String) delegateTask.getVariable(VAR_COMMENT);
        ArrayList<NodeRef> taskAttachments = FlowableListenerUtils.getTaskAttachments(delegateTask);
        String assignee = delegateTask.getAssignee();
        String originalOwner = (String) delegateTask.getVariableLocal("taskOriginalOwner");

        /**
         * Get assignee
         */
        if (assignee != null && !assignee.equals(originalOwner)) {
            if (originalOwner != null && deputyService.isAssistantUserByUser(originalOwner, assignee)) {
                eventProperties.put(QName.createQName("", "taskOriginalOwner"), authorityService.getAuthorityNodeRef(originalOwner));
            }
        }
        ArrayList<NodeRef> pooledActors = FlowableListenerUtils.getPooledActors(delegateTask, authorityService);
        Map<QName, Serializable> additionalProperties = getAdditionalProperties(delegateTask.getExecution());

        if(additionalProperties != null) {
            eventProperties.putAll(additionalProperties);
        }
        NodeRef bpmPackage = FlowableListenerUtils.getWorkflowPackage(delegateTask);
        List<AssociationRef> packageAssocs = nodeService.getSourceAssocs(bpmPackage, ICaseTaskModel.ASSOC_WORKFLOW_PACKAGE);

        String roleName;
        if (panelOfAuthorized != null && assignee != null && !panelOfAuthorized.isEmpty() && panelOfAuthorized.size() > 0) {
            List<NodeRef> listRoles = getListRoles(document);
            roleName = getAuthorizedName(panelOfAuthorized, listRoles, assignee) != null ?
                    getAuthorizedName(panelOfAuthorized, listRoles, assignee) :
                    getRoleName(packageAssocs, assignee, delegateTask.getId());
        } else {
            roleName = getRoleName(packageAssocs, assignee, delegateTask.getId());
            if (packageAssocs.size() > 0) {
                eventProperties.put(HistoryModel.PROP_CASE_TASK, packageAssocs.get(0).getSourceRef());
            }
        }

        /**
         * Save history event
         */
        eventProperties.put(HistoryModel.PROP_NAME, eventName);
        eventProperties.put(HistoryModel.PROP_TASK_INSTANCE_ID, ENGINE_PREFIX + delegateTask.getId());
        eventProperties.put(HistoryModel.PROP_TASK_TYPE, taskType);
        eventProperties.put(HistoryModel.PROP_TASK_OUTCOME, taskOutcome);
        eventProperties.put(HistoryModel.PROP_TASK_COMMENT, taskComment);
        eventProperties.put(HistoryModel.PROP_TASK_ATTACHMENTS, taskAttachments);
        eventProperties.put(HistoryModel.PROP_TASK_POOLED_ACTORS, pooledActors);
        eventProperties.put(HistoryModel.PROP_TASK_ROLE, roleName);

        eventProperties.put(HistoryModel.PROP_WORKFLOW_INSTANCE_ID, ENGINE_PREFIX + delegateTask.getProcessInstanceId());
        eventProperties.put(HistoryModel.PROP_WORKFLOW_DESCRIPTION, (Serializable) delegateTask.getExecution().getVariable(VAR_DESCRIPTION));
        eventProperties.put(HistoryModel.ASSOC_INITIATOR, assignee != null ? assignee : HistoryService.SYSTEM_USER);
        eventProperties.put(HistoryModel.ASSOC_DOCUMENT, document);
        historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
    }

    /**
     * Get additional properties
     * @param execution Execution
     * @return Map of additional properties
     */
    private Map<QName, Serializable> getAdditionalProperties(DelegateExecution execution) {
        Object additionalPropertiesObj = execution.getVariable(VAR_ADDITIONAL_EVENT_PROPERTIES);
        if(additionalPropertiesObj == null) {
            return null;
        }
        if(additionalPropertiesObj instanceof Map) {
            return convertProperties((Map) additionalPropertiesObj);
        }
        logger.warn("Unknown type of additional event properties: " + additionalPropertiesObj.getClass());
        return null;
    }

    /**
     * Convert properties
     * @param additionalProperties Additional properties
     * @return Converted properties
     */
    private Map<QName, Serializable> convertProperties(Map additionalProperties) {
        Map<QName, Serializable> result = new HashMap<QName, Serializable>(additionalProperties.size());
        for(Object key : additionalProperties.keySet()) {
            QName name = null;
            if(key instanceof String) {
                name = qNameConverter.mapNameToQName((String) key);
            } else if(key instanceof QName) {
                name = (QName) key;
            } else {
                logger.warn("Unknown type of key: " + key.getClass());
                continue;
            }
            result.put(name, (Serializable) additionalProperties.get(key));
        }
        return result;
    }

    /**
     * Get list of roles
     * @param document Document node reference
     * @return List of role nodes
     */
    private List<NodeRef> getListRoles(NodeRef document) {
        List<ChildAssociationRef> childsAssocRefs = nodeService.getChildAssocs(document, ICaseRoleModel.ASSOC_ROLES, RegexQNamePattern.MATCH_ALL);
        List<NodeRef> roles = new ArrayList<>();
        for (ChildAssociationRef childAssociationRef: childsAssocRefs) {
            roles.add(childAssociationRef.getChildRef());
        }
        return roles;
    }

    /**
     * Get authorized name
     * @param varNameRoles Role names
     * @param listRoles Roles
     * @param assignee Assignee
     * @return Authorized name
     */
    private String getAuthorizedName(List<String> varNameRoles, List<NodeRef> listRoles, String assignee) {
        for (NodeRef role: listRoles) {
            if (varNameRoles.contains(nodeService.getProperty(role, ICaseRoleModel.PROP_VARNAME))) {
                for(String varNameRole: varNameRoles) {
                    if (varNameRole.equals(nodeService.getProperty(role, ICaseRoleModel.PROP_VARNAME))) {
                        Map<NodeRef, NodeRef> delegates = caseRoleService.getDelegates(role);
                        for (Map.Entry<NodeRef, NodeRef> entry : delegates.entrySet()) {
                            if (authorityService.getAuthorityNodeRef(assignee).equals(entry.getValue())) {
                                return (String) nodeService.getProperty(entry.getKey(), ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get role name
     * @param packageAssocs Package assocs
     * @param assignee Assigne
     * @param taskId Task id
     * @return Role name
     */
    private String getRoleName(List<AssociationRef> packageAssocs, String assignee, String taskId) {
        String roleName = "";
        if (taskId != null) {
            WorkflowTask task = workflowService.getTaskById(ENGINE_PREFIX + taskId);
            if (task != null) {
                Map<QName, Serializable> properties = task.getProperties();
                if (properties.get(CasePerformModel.ASSOC_CASE_ROLE) != null) {
                    NodeRef role = (NodeRef) properties.get(CasePerformModel.ASSOC_CASE_ROLE);
                    if (role != null && nodeService.exists(role) && nodeService.getProperty(role, ContentModel.PROP_NAME) != null) {
                        roleName = (String) nodeService.getProperty(role, ContentModel.PROP_NAME);
                    }
                }
            }
        }

        if (roleName == null || roleName.equals("")) {
            if (packageAssocs.size() > 0) {
                NodeRef currentTask = packageAssocs.get(0).getSourceRef();
                List<AssociationRef> performerRoles = nodeService.getTargetAssocs(currentTask, CasePerformModel.ASSOC_PERFORMERS_ROLES);
                if (performerRoles != null && !performerRoles.isEmpty()) {
                    NodeRef firstRole = performerRoles.get(0).getTargetRef();
                    roleName = (String) nodeService.getProperty(firstRole, ContentModel.PROP_NAME);
                }
            }
            if (roleName.isEmpty()) {
                roleName = assignee;
            }
        }
        return roleName;
    }

    /** Setters */

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setDeputyService(DeputyService deputyService) {
        this.deputyService = deputyService;
    }

    public void setCaseRoleService(CaseRoleService caseRoleService) {
        this.caseRoleService = caseRoleService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setPanelOfAuthorized(List<String> panelOfAuthorized) {
        this.panelOfAuthorized = panelOfAuthorized;
    }

}
