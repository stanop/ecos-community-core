package ru.citeck.ecos.workflow.tasks;

import java.util.Map;

public interface EngineTaskService {

    void endTask(String taskId, String transition, Map<String, Object> variables);

    TaskInfo getTaskInfo(String taskId);
}
