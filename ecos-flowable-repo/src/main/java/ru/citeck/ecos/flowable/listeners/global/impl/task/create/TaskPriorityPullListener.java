package ru.citeck.ecos.flowable.listeners.global.impl.task.create;

import org.flowable.task.service.delegate.DelegateTask;
import ru.citeck.ecos.flowable.listeners.global.GlobalCreateTaskListener;

/**
 * Task priority pull listener
 */
public class TaskPriorityPullListener implements GlobalCreateTaskListener {

    /**
     * Notify
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        Object workflowPriority = delegateTask.getVariable("bpm_workflowPriority");
        if(workflowPriority != null && workflowPriority instanceof Integer) {
            delegateTask.setPriority((Integer) workflowPriority);
        }
    }
}
