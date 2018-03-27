package ru.citeck.ecos.flowable.listeners.global.impl.task.create;

import org.flowable.engine.delegate.DelegateTask;
import ru.citeck.ecos.flowable.listeners.global.GlobalAssignmentTaskListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalCompleteTaskListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalCreateTaskListener;
import ru.citeck.ecos.workflow.mirror.WorkflowMirrorService;

/**
 * Flowable mirror listener
 */
public class MirrorListener implements GlobalCreateTaskListener, GlobalAssignmentTaskListener, GlobalCompleteTaskListener {

    /**
     * Constants
     */
    private static final String ENGINE_PREFIX = "flowable$";

    /**
     * Workflow mirror service
     */
    private WorkflowMirrorService workflowMirrorService;

    /**
     * Notify
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        workflowMirrorService.mirrorTask(ENGINE_PREFIX + delegateTask.getId());
    }

    /**
     * Set workflow mirror service
     * @param workflowMirrorService Workflow mirror service
     */
    public void setWorkflowMirrorService(WorkflowMirrorService workflowMirrorService) {
        this.workflowMirrorService = workflowMirrorService;
    }
}
