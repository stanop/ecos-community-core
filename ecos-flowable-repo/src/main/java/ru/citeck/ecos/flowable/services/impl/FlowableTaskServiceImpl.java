package ru.citeck.ecos.flowable.services.impl;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import ru.citeck.ecos.flowable.services.FlowableTaskService;

import java.util.List;

/**
 * Flowable task service
 */
public class FlowableTaskServiceImpl implements FlowableTaskService {

    /**
     * Task service
     */
    private TaskService taskService;

    /**
     * Set task service
     * @param taskService Task service
     */
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Get task by task id
     * @param taskId Task id
     * @return Task
     */
    @Override
    public Task getTaskById(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    /**
     * Get tasks by process instance id
     * @param processInstanceId Process instance id
     * @return List of tasks
     */
    @Override
    public List<Task> getTasksByProcessInstanceId(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }

    /**
     * Get tasks by process definition id
     * @param processDefinitionId Process definition id
     * @return List of tasks
     */
    @Override
    public List<Task> getTasksByProcessDefinitionId(String processDefinitionId) {
        return taskService.createTaskQuery().processDefinitionId(processDefinitionId).list();
    }
}


