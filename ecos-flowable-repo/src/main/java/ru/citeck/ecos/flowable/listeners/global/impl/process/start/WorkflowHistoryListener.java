package ru.citeck.ecos.flowable.listeners.global.impl.process.start;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import ru.citeck.ecos.flowable.listeners.global.GlobalEndExecutionListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalStartExecutionListener;
import ru.citeck.ecos.history.HistoryService;
import ru.citeck.ecos.model.HistoryModel;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Workflow history listener
 */
public class WorkflowHistoryListener implements GlobalStartExecutionListener, GlobalEndExecutionListener {

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(WorkflowHistoryListener.class);

    /**
     * Constants
     */
    private static final String FLOWABLE_ENGINE_PREFIX = "flowable$";
    private static final Map<String, String> eventNames;
    private String VAR_PACKAGE;
    private String VAR_DESCRIPTION;

    static {
        eventNames = new HashMap<String, String>(3);
        eventNames.put(ExecutionListener.EVENTNAME_START, "workflow.start");
        eventNames.put(ExecutionListener.EVENTNAME_END, "workflow.end");
    }

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * History service
     */
    private HistoryService historyService;

    /**
     * Namespace service
     */
    private NamespaceService namespaceService;

    /**
     * Workflow service
     */
    private WorkflowService workflowService;

    /**
     * Init
     */
    public void init() {
        WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(namespaceService);
        VAR_PACKAGE = qNameConverter.mapQNameToName(WorkflowModel.ASSOC_PACKAGE);
        VAR_DESCRIPTION = qNameConverter.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
    }

    /**
     * Notify
     * @param delegateExecution Execution
     */
    @Override
    public void notify(DelegateExecution delegateExecution) {
        /** Event name */
        String eventName = eventNames.get(delegateExecution.getEventName());
        if(eventName == null) {
            logger.warn("Unsupported flowable execution event: " + delegateExecution.getEventName());
            return;
        }
        ExecutionEntity entity = (ExecutionEntity) delegateExecution;
        if(entity.isDeleted() && "cancelled".equals(entity.getDeleteReason())) {
            eventName += ".cancelled";
        }

        /** Initiator */
        String initiator = null;
        NodeRef initiatorRef = (NodeRef) delegateExecution.getVariable(WorkflowConstants.PROP_INITIATOR);
        if(initiatorRef != null && nodeService.exists(initiatorRef)) {
            initiator = (String) nodeService.getProperty(initiatorRef, ContentModel.PROP_USERNAME);
        }

        /** Workflow definition */
        WorkflowDefinition workflowDefinition = null;
        Object workflowDefinitionId = delegateExecution.getProcessDefinitionId();
        if(workflowDefinitionId != null) {
            workflowDefinition = workflowService.getDefinitionById(FLOWABLE_ENGINE_PREFIX + workflowDefinitionId);
            if(workflowDefinition == null) {
                logger.warn("Unknown workflow definition: " + workflowDefinitionId);
                return;
            }
        }

        /** Document */
        NodeRef document = null;
        NodeRef wfPackage = (NodeRef) delegateExecution.getVariable(VAR_PACKAGE);
        if(wfPackage != null) {
            List<ChildAssociationRef> packageAssocs = nodeService.getChildAssocs(wfPackage, WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
            if(packageAssocs != null && packageAssocs.size() > 0) {
                document = packageAssocs.get(0).getChildRef();
            }
        }

        /** Save history record */
        Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>(5);
        eventProperties.put(HistoryModel.PROP_NAME, eventName);
        eventProperties.put(HistoryModel.PROP_WORKFLOW_INSTANCE_ID, FLOWABLE_ENGINE_PREFIX + delegateExecution.getProcessInstanceId());
        eventProperties.put(HistoryModel.PROP_WORKFLOW_DESCRIPTION, (Serializable) delegateExecution.getVariable(VAR_DESCRIPTION));
        if(workflowDefinition != null) {
            eventProperties.put(HistoryModel.PROP_WORKFLOW_TYPE, workflowDefinition.getName());
        }
        eventProperties.put(HistoryModel.ASSOC_INITIATOR, initiator != null ? initiator : HistoryService.SYSTEM_USER);
        eventProperties.put(HistoryModel.ASSOC_DOCUMENT, document);
        historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
    }

    /**
     * Set node service
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set history service
     * @param historyService History service
     */
    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * Set namespace service
     * @param namespaceService Namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * Set workflow service
     * @param workflowService Workflow service
     */
    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }
}
