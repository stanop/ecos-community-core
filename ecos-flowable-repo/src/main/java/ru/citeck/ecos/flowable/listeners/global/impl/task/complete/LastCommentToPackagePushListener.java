package ru.citeck.ecos.flowable.listeners.global.impl.task.complete;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.flowable.task.service.delegate.DelegateTask;
import ru.citeck.ecos.flowable.listeners.global.GlobalCompleteTaskListener;
import ru.citeck.ecos.model.CiteckWorkflowModel;


public class LastCommentToPackagePushListener implements GlobalCompleteTaskListener {

    private NodeService nodeService;

    private WorkflowService workflowService;

    @Override
    public void notify(DelegateTask delegateTask) {
        WorkflowInstance workflowInstance = workflowService.getWorkflowById("flowable$" + delegateTask.getProcessInstanceId());
        if (workflowInstance == null) {
            return;
        }
        String lastComment = (String) delegateTask.getVariable("cwf_lastcomment");
        nodeService.setProperty(workflowInstance.getWorkflowPackage(),
                CiteckWorkflowModel.PROP_LASTCOMMENT, lastComment);
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
