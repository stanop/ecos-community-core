package ru.citeck.ecos.flowable.services;

import org.flowable.task.api.Task;

import java.util.List;

/**
 * Flowable task service interface
 */
public interface FlowableTaskService {

    /**
     * Get task by task id
     * @param taskId Task id
     * @return Task
     */
    Task getTaskById(String taskId);

    /**
     * Get tasks by process instance id
     * @param processInstanceId Process instance id
     * @return List of tasks
     */
    List<Task> getTasksByProcessInstanceId(String processInstanceId);

    /**
     * Get tasks by process definition id
     * @param processDefinitionId Process definition id
     * @return List of tasks
     */
    List<Task> getTasksByProcessDefinitionId(String processDefinitionId);

}
