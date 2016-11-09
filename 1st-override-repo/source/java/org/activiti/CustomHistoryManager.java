package org.activiti;

import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;

import java.util.Date;
import java.util.Map;

public class CustomHistoryManager extends HistoryManager {

    @Override
    public void recordProcessInstanceEnd(String processInstanceId, String deleteReason, String activityId) {
        super.recordProcessInstanceEnd(processInstanceId, deleteReason, activityId);
    }

    @Override
    public void recordProcessInstanceStart(ExecutionEntity processInstance) {
        super.recordProcessInstanceStart(processInstance);
    }

    @Override
    public void recordSubProcessInstanceStart(ExecutionEntity parentExecution, ExecutionEntity subProcessInstance) {
        super.recordSubProcessInstanceStart(parentExecution, subProcessInstance);
    }

    @Override
    public void recordActivityStart(ExecutionEntity executionEntity) {
    }

    @Override
    public void recordActivityEnd(ExecutionEntity executionEntity) {
    }

    @Override
    public void recordStartEventEnded(String executionId, String activityId) {
    }

    @Override
    public void recordExecutionReplacedBy(ExecutionEntity execution, InterpretableExecution replacedBy) {
    }

    @Override
    public void recordProcessDefinitionChange(String processInstanceId, String processDefinitionId) {
    }

    @Override
    public void recordTaskCreated(TaskEntity task, ExecutionEntity execution) {
    }

    @Override
    public void recordTaskAssignment(TaskEntity task) {
    }

    @Override
    public void recordTaskClaim(String taskId) {
    }

    @Override
    public void recordTaskId(TaskEntity task) {
    }

    @Override
    public void recordTaskEnd(String taskId, String deleteReason) {
        super.recordTaskEnd(taskId, deleteReason);
    }

    @Override
    public void recordTaskAssigneeChange(String taskId, String assignee) {
    }

    @Override
    public void recordTaskOwnerChange(String taskId, String owner) {
    }

    @Override
    public void recordTaskNameChange(String taskId, String taskName) {
        super.recordTaskNameChange(taskId, taskName);
    }

    @Override
    public void recordTaskDescriptionChange(String taskId, String description) {
    }

    @Override
    public void recordTaskDueDateChange(String taskId, Date dueDate) {
    }

    @Override
    public void recordTaskPriorityChange(String taskId, int priority) {
    }

    @Override
    public void recordTaskParentTaskIdChange(String taskId, String parentTaskId) {
    }

    @Override
    public void recordTaskExecutionIdChange(String taskId, String executionId) {
    }

    @Override
    public void recordTaskDefinitionKeyChange(TaskEntity task, String taskDefinitionKey) {
    }

    @Override
    public void recordVariableCreate(VariableInstanceEntity variable) {

    }

    @Override
    public void recordHistoricDetailVariableCreate(VariableInstanceEntity variable, ExecutionEntity sourceActivityExecution, boolean useActivityId) {
    }

    @Override
    public void recordVariableUpdate(VariableInstanceEntity variable) {
    }

    @Override
    public void createIdentityLinkComment(String taskId, String userId, String groupId, String type, boolean create) {
    }

    @Override
    public void createIdentityLinkComment(String taskId, String userId, String groupId, String type, boolean create, boolean forceNullUserId) {
    }

    @Override
    public void createAttachmentComment(String taskId, String processInstanceId, String attachmentName, boolean create) {
    }

    @Override
    public void reportFormPropertiesSubmitted(ExecutionEntity processInstance, Map<String, String> properties, String taskId) {
    }

    @Override
    public void recordIdentityLinkCreated(IdentityLinkEntity identityLink) {
    }

    @Override
    public void deleteHistoricIdentityLink(String id) {
    }

}
