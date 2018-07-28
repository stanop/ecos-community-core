package ru.citeck.ecos.journals.group.action;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import ru.citeck.ecos.journals.action.group.GroupActionProcFactory;
import ru.citeck.ecos.journals.action.group.GroupActionProcessor;
import ru.citeck.ecos.journals.action.group.GroupActionResult;
import ru.citeck.ecos.journals.action.group.TxnGroupActionProcessor;
import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.Map;
import java.util.Objects;

/**
 * @author Pavel Simonov
 */
public class CompleteTaskByMirrorAction implements GroupActionProcFactory {

    public static final String ACTION_ID = "complete-task-by-mirror";
    public static final String TASK_TYPE_KEY = "task-type";
    public static final String TRANSITION_ID = "transition";

    public static final String[] MANDATORY_PARAMS = {TASK_TYPE_KEY, TRANSITION_ID};

    private TransactionService transactionService;
    private WorkflowService workflowService;
    private NamespaceService namespaceService;
    private NodeService nodeService;

    public CompleteTaskByMirrorAction(ServiceRegistry serviceRegistry,
                                      TransactionService transactionService) {
        this.transactionService = transactionService;
        this.workflowService = serviceRegistry.getWorkflowService();
        this.nodeService = serviceRegistry.getNodeService();
        this.namespaceService = serviceRegistry.getNamespaceService();
    }

    @Override
    public GroupActionProcessor createProcessor(Map<String, String> params) {
        return new Processor(params);
    }

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String[] getMandatoryParams() {
        return MANDATORY_PARAMS;
    }

    private class Processor extends TxnGroupActionProcessor {

        Map<String, String> params;

        public Processor(Map<String, String> params) {
            super(transactionService);
            this.params = params;
        }

        @Override
        public GroupActionResult processImpl(RemoteNodeRef mirrorRef) {
            Object taskIdObj = nodeService.getProperty(mirrorRef.getNodeRef(), WorkflowModel.PROP_TASK_ID);
            String taskId = String.valueOf(taskIdObj);
            String globalTaskId = ActivitiConstants.ENGINE_ID + "$" + taskId;
            workflowService.endTask(globalTaskId, params.get(TRANSITION_ID));
            return new GroupActionResult(GroupActionResult.STATUS_OK);
        }

        @Override
        public boolean isApplicable(RemoteNodeRef mirrorRef) {

            Long taskId = (Long) nodeService.getProperty(mirrorRef.getNodeRef(), WorkflowModel.PROP_TASK_ID);

            if (taskId == null) {
                return false;
            }

            QName taskType = nodeService.getType(mirrorRef.getNodeRef());
            QName paramTaskType = QName.resolveToQName(namespaceService, params.get(TASK_TYPE_KEY));

            return Objects.equals(paramTaskType, taskType);
        }
    }
}

