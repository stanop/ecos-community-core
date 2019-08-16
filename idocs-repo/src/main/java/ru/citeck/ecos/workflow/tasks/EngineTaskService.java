package ru.citeck.ecos.workflow.tasks;

import java.util.Map;

public interface EngineTaskService {

    /**
     * @param taskId task to complete
     * @param transition task complete transition
     * @param variables variables to save in task
     * @param transientVariables variables to save in execution
     */
    void endTask(String taskId,
                 String transition,
                 Map<String, Object> variables,
                 Map<String, Object> transientVariables);

    TaskInfo getTaskInfo(String taskId);
}
