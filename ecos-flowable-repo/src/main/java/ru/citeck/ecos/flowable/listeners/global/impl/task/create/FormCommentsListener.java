package ru.citeck.ecos.flowable.listeners.global.impl.task.create;

import org.flowable.engine.TaskService;
import org.flowable.task.service.delegate.DelegateTask;
import ru.citeck.ecos.flowable.listeners.global.GlobalAssignmentTaskListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalCompleteTaskListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalCreateTaskListener;
import ru.citeck.ecos.flowable.services.FlowableCustomCommentService;

import java.util.List;

/**
 * Form comments listener
 */
public class FormCommentsListener implements GlobalCreateTaskListener, GlobalAssignmentTaskListener, GlobalCompleteTaskListener {

    /**
     * Task service
     */
    private TaskService taskService;

    /**
     * Flowable custom comment service
     */
    private FlowableCustomCommentService flowableCustomCommentService;


    /**
     * Notify
     * @param delegateTask Delegate task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        setFirstComment(delegateTask);
    }

    /**
     * Set first comments
     * @param delegateTask Delegate task
     */
    private void setFirstComment(DelegateTask delegateTask) {
        List<String> commentFieldIds = flowableCustomCommentService.getFieldIdsByProcessDefinitionId(delegateTask.getProcessDefinitionId());
        commentFieldIds.forEach(commentFieldId -> {
            String comments = (String) taskService.getVariable(delegateTask.getId(), commentFieldId);
            if (comments != null) {
                taskService.setVariable(delegateTask.getId(), "cwf_lastcomment", comments);
            }
        });
    }

    /** Setters */

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void setFlowableCustomCommentService(FlowableCustomCommentService flowableCustomCommentService) {
        this.flowableCustomCommentService = flowableCustomCommentService;
    }
}
