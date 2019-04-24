package ru.citeck.ecos.flowable.services;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ChangeActivityStateBuilder;
import org.flowable.task.api.Task;
import ru.citeck.ecos.flowable.utils.TaskUtils;
import ru.citeck.ecos.workflow.mirror.WorkflowMirrorService;

import java.util.LinkedList;
import java.util.List;

public class RollbackFlowableTasksService {
    private static final String FLOWABLE_PREFIX = "flowable$";

    private RuntimeService runtimeService;
    private WorkflowService workflowService;
    private WorkflowMirrorService workflowMirrorService;
    private FlowableTaskService flowableTaskService;

    public boolean rollbackTasks(NodeRef node, List<String> newActivityIds) {
        List<Task> currentTasks = TaskUtils.getAllActiveTasksFromNode(node, workflowService, flowableTaskService);
        if (currentTasks.isEmpty()) {
            return false;
        }

        Task firstTask = currentTasks.get(0);
        ChangeActivityStateBuilder changeActivityStateBuilder = runtimeService.createChangeActivityStateBuilder();
        changeActivityStateBuilder.processInstanceId(firstTask.getProcessInstanceId());

        if (currentTasks.size() == 1) {
            changeActivityStateBuilder.moveSingleExecutionToActivityIds(firstTask.getExecutionId(), newActivityIds);
            changeActivityStateBuilder.changeState();
        }

        if (currentTasks.size() > 1) {
            List<String> currentTaskIDs = new LinkedList<>();
            for (Task task : currentTasks) {
                currentTaskIDs.add(task.getExecutionId());
            }
            int currentTaskIDsSize = currentTaskIDs.size();
            int newTaskIDsSize = newActivityIds.size();

            if (currentTaskIDsSize > newTaskIDsSize) {
                for (int taskIdIndex = 0; taskIdIndex < newTaskIDsSize - 1; ++taskIdIndex) {
                    changeActivityStateBuilder.moveActivityIdTo(
                            currentTaskIDs.get(taskIdIndex),
                            newActivityIds.get(taskIdIndex)
                    );
                    changeActivityStateBuilder.changeState();
                }
                changeActivityStateBuilder.moveExecutionsToSingleActivityId(
                        currentTaskIDs.subList(newTaskIDsSize - 1, currentTaskIDsSize),
                        newActivityIds.get(newTaskIDsSize - 1)
                );
                changeActivityStateBuilder.changeState();
            }

            if (currentTaskIDsSize < newTaskIDsSize) {
                for (int taskIdIndex = 0; taskIdIndex < currentTaskIDsSize - 1; ++taskIdIndex) {
                    changeActivityStateBuilder.moveActivityIdTo(
                            currentTaskIDs.get(taskIdIndex),
                            newActivityIds.get(taskIdIndex)
                    );
                    changeActivityStateBuilder.changeState();
                }
                changeActivityStateBuilder.moveSingleExecutionToActivityIds(
                        currentTaskIDs.get(currentTaskIDsSize - 1),
                        newActivityIds.subList(currentTaskIDsSize - 1, newTaskIDsSize)
                );
                changeActivityStateBuilder.changeState();
            }

            if (currentTaskIDsSize == newTaskIDsSize) {
                for (int taskIdIndex = 0; taskIdIndex < currentTaskIDsSize; ++taskIdIndex) {
                    changeActivityStateBuilder.moveActivityIdTo(
                            currentTaskIDs.get(taskIdIndex),
                            newActivityIds.get(taskIdIndex)
                    );
                    changeActivityStateBuilder.changeState();
                }
            }
        }
        for (Task task : currentTasks) {
            workflowMirrorService.mirrorTask(FLOWABLE_PREFIX + task.getId());
        }
        currentTasks = TaskUtils.getAllActiveTasksFromNode(node, workflowService, flowableTaskService);
        return currentTasks.size() == newActivityIds.size();
    }

    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setWorkflowMirrorService(WorkflowMirrorService workflowMirrorService) {
        this.workflowMirrorService = workflowMirrorService;
    }

    public void setFlowableTaskService(FlowableTaskService flowableTaskService) {
        this.flowableTaskService = flowableTaskService;
    }
}
