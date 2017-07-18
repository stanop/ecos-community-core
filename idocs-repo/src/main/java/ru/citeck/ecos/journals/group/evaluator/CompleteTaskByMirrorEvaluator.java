package ru.citeck.ecos.journals.group.evaluator;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.journals.group.GroupActionEvaluator;

import java.util.Map;
import java.util.Objects;

/**
 * @author Pavel Simonov
 */
public class CompleteTaskByMirrorEvaluator extends GroupActionEvaluator {

    public static final String ACTION_ID = "complete-task-by-mirror";
    public static final String TASK_TYPE_KEY = "task-type";
    public static final String TRANSITION_ID = "transition";

    public static final String[] MANDATORY_PARAMS = {TASK_TYPE_KEY, TRANSITION_ID};

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public void invoke(NodeRef mirrorRef, Map<String, String> params) {
        String taskId = String.valueOf(nodeService.getProperty(mirrorRef, WorkflowModel.PROP_TASK_ID));
        String globalTaskId = ActivitiConstants.ENGINE_ID + "$" + taskId;
        workflowService.endTask(globalTaskId, params.get(TRANSITION_ID));
    }

    @Override
    public boolean isApplicable(NodeRef mirrorRef, Map<String, String> params) {

        Long taskId = (Long) nodeService.getProperty(mirrorRef, WorkflowModel.PROP_TASK_ID);

        if (taskId == null) {
            return false;
        }

        QName taskType = nodeService.getType(mirrorRef);
        QName paramTaskType = QName.resolveToQName(namespaceService, params.get(TASK_TYPE_KEY));

        return Objects.equals(paramTaskType, taskType);
    }

    @Override
    public String[] getMandatoryParams() {
        return MANDATORY_PARAMS;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
}

