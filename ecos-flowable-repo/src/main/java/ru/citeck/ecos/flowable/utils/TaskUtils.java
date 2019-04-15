package ru.citeck.ecos.flowable.utils;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.flowable.task.api.Task;
import ru.citeck.ecos.flowable.services.FlowableTaskService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TaskUtils {

    public static List<Task> getAllActiveTasksFromNode(
            NodeRef node, WorkflowService workflowService, FlowableTaskService flowableTaskService) {

        List<WorkflowInstance> workflowInstancesList = workflowService.getWorkflowsForContent(node, true);
        if (workflowInstancesList == null || workflowInstancesList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Task> activeTaskList = new LinkedList<>();
        for (WorkflowInstance instance : workflowInstancesList) {
            if (instance == null || !instance.isActive()) {
                continue;
            }

            String workflowInstanceId = instance.getId();
            List<Task> flowableTaskList = flowableTaskService.getTasksByProcessInstanceId(
                    workflowInstanceId.substring(workflowInstanceId.indexOf("$") + 1)
            );
            if (flowableTaskList == null || flowableTaskList.isEmpty()) {
                continue;
            }
            activeTaskList.addAll(flowableTaskList);
        }

        return new ArrayList<>(activeTaskList);
    }
}
