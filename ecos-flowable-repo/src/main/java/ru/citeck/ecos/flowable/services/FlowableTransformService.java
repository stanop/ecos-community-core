package ru.citeck.ecos.flowable.services;

import org.alfresco.service.cmr.workflow.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricTaskInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;

import java.util.List;

/**
 * Flowable transform service interface
 */
public interface FlowableTransformService {

    /**
     * Transform process definition to alfresco workflow definition
     * @param processDefinition Process definition
     * @return Workflow definition
     */
    WorkflowDefinition transformProcessDefinition(ProcessDefinition processDefinition);

    /**
     * Transform process definitions to alfresco workflow definitions
     * @param processDefinitions Process definitions
     * @return Workflow definitions
     */
    List<WorkflowDefinition> transformProcessDefinitions(List<ProcessDefinition> processDefinitions);

    /**
     * Transform start task
     * @param processDefinition Process definition
     * @param processInstance Process instance
     * @return Workflow task
     */
    WorkflowTask transformStartTask(ProcessDefinition processDefinition, ProcessInstance processInstance);

    /**
     * Transform start task
     * @param processDefinition Process definition
     * @param processInstance History process instance
     * @return Workflow task
     */
    WorkflowTask transformStartTask(ProcessDefinition processDefinition, HistoricProcessInstance processInstance);

    /**
     * Transform start task definition
     * @param processDefinition Process definition
     * @return Workflow task definition
     */
    WorkflowTaskDefinition transformStartTaskDefinition(ProcessDefinition processDefinition);

    /**
     * Transform task to workflow task definition
     * @param task Task
     * @return Workflow task definition
     */
    WorkflowTaskDefinition transformTaskDefinition(Task task);

    /**
     * Transform task to workflow task definition
     * @param task Task
     * @return Workflow task definition
     */
    WorkflowTaskDefinition transformTaskDefinition(HistoricTaskInstance task);

    /**
     * Transform tasks to workflow task definitions
     * @param tasks Tasks
     * @return Workflow task definitions
     */
    List<WorkflowTaskDefinition> transformTaskDefinitions(List<Task> tasks);

    /**
     * Transform process instance to alfresco workflow path
     * @param processInstance Process instance
     * @return Workflow path
     */
    WorkflowPath transformProcessInstanceToWorkflowPath(ProcessInstance processInstance);

    /**
     * Transform process instance to alfresco workflow path
     * @param processInstance Process instance
     * @return Workflow path
     */
    WorkflowPath transformHistoryProcessInstanceToWorkflowPath(HistoricProcessInstance processInstance);

    /**
     * Transform process instance to alfresco workflow instance
     * @param processInstance Process instance
     * @return Workflow instance
     */
    WorkflowInstance transformProcessInstanceToWorkflowInstance(ProcessInstance processInstance);

    /**
     * Transform process instances to alfresco workflow instances
     * @param processInstances Process instances
     * @return Workflow instances
     */
    List<WorkflowInstance> transformProcessInstancesToWorkflowInstances(List<ProcessInstance> processInstances);

    /**
     * Transform history process instance to alfresco workflow instance
     * @param processInstance Process instance
     * @return Workflow instance
     */
    WorkflowInstance transformHistoryProcessInstanceToWorkflowInstance(HistoricProcessInstance processInstance);

    /**
     * Transform process instances to alfresco workflow instances
     * @param processInstances Process instances
     * @return Workflow instances
     */
    List<WorkflowInstance> transformHistoryProcessInstancesToWorkflowInstances(List<HistoricProcessInstance> processInstances);

    /**
     * Transform task to alfresco workflow task
     * @param task Task
     * @return Workflow task
     */
    WorkflowTask transformTask(Task task);

    /**
     * Transform task to alfresco workflow task
     * @param task Task
     * @return Workflow task
     */
    WorkflowTask transformTask(HistoricTaskInstance task);

    /**
     * Transform tasks to alfresco workflow tasks
     * @param tasks Tasks
     * @return Workflow tasks
     */
    List<WorkflowTask> transformTasks(List<Task> tasks);

    /**
     * Transform tasks to alfresco workflow tasks
     * @param tasks Tasks
     * @return Workflow tasks
     */
    List<WorkflowTask> transformHistoryTasks(List<HistoricTaskInstance> tasks);
}
