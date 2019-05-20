package ru.citeck.ecos.flowable.listeners.global.impl.task.complete;

import org.flowable.task.service.delegate.DelegateTask;
import ru.citeck.ecos.flowable.listeners.global.GlobalCompleteTaskListener;
import ru.citeck.ecos.workflow.mirror.WorkflowMirrorService;

import static ru.citeck.ecos.flowable.constants.FlowableConstants.ENGINE_PREFIX;

/**
 * Flowable complete task mirror listener
 */
public class CompleteTaskMirrorListener implements GlobalCompleteTaskListener {

    private WorkflowMirrorService workflowMirrorService;

    /**
     * Notify
     *
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        workflowMirrorService.mirrorTask(ENGINE_PREFIX + delegateTask.getId(), false);
    }

    /**
     * Set workflow mirror service
     *
     * @param workflowMirrorService Workflow mirror service
     */
    public void setWorkflowMirrorService(WorkflowMirrorService workflowMirrorService) {
        this.workflowMirrorService = workflowMirrorService;
    }
}
