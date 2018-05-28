package ru.citeck.ecos.flowable;

import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.TaskComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.converters.FlowablePropertyConverter;
import ru.citeck.ecos.flowable.services.*;
import ru.citeck.ecos.flowable.utils.FlowableWorkflowPropertyHandlerRegistry;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Flowable task component
 */
public class FlowableTaskComponent implements TaskComponent, InitializingBean {


    /**
     * Constants
     */
    private static final String FLOWABLE_ENGINE_NAME = "flowable";
    private static final String ENGINE_PREFIX = "flowable$";
    /**
     * BPN engine registry
     */
    private BPMEngineRegistry bpmEngineRegistry;

    /**
     * Workflow admin service
     */
    private WorkflowAdminService workflowAdminService;

    /**
     * Task service
     */
    @Autowired
    private TaskService taskService;

    /**
     * Flowable task service
     */
    @Autowired
    private FlowableTaskService flowableTaskService;

    /**
     * Flowable history service
     */
    @Autowired
    private FlowableHistoryService flowableHistoryService;

    /**
     * Flowable process instance service
     */
    @Autowired
    private FlowableProcessInstanceService flowableProcessInstanceService;

    /**
     * Flowable transform service
     */
    @Autowired
    private FlowableTransformService flowableTransformService;

    /**
     * Flowable process definition service
     */
    @Autowired
    private FlowableProcessDefinitionService flowableProcessDefinitionService;

    /**
     * Flowable property converter
     */
    @Autowired
    private FlowablePropertyConverter flowablePropertyConverter;

    /**
     * Workflow property handler registry
     */
    @Autowired
    @Qualifier("flowableWorkflowPropertyHandlerRegistry")
    private FlowableWorkflowPropertyHandlerRegistry workflowPropertyHandlerRegistry;

    /**
     * Runtime service
     */
    @Autowired
    private RuntimeService runtimeService;

    /**
     * Set BPN engine registry
     * @param bpmEngineRegistry BPN engine registry
     */
    public void setBpmEngineRegistry(BPMEngineRegistry bpmEngineRegistry) {
        this.bpmEngineRegistry = bpmEngineRegistry;
    }

    /**
     * Set Workflow admin service
     * @param workflowAdminService Workflow admin service
     */
    public void setWorkflowAdminService(WorkflowAdminService workflowAdminService) {
        this.workflowAdminService = workflowAdminService;
    }

    /**
     * After properties set
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (taskService == null) {
            return;
        }
        bpmEngineRegistry.registerTaskComponent(FLOWABLE_ENGINE_NAME, this);
        workflowAdminService.setEngineEnabled(FLOWABLE_ENGINE_NAME, true);
        workflowAdminService.setEngineVisibility(FLOWABLE_ENGINE_NAME, true);
    }


    /**
     * Gets a Task by unique Id
     * @param taskId the task id
     * @return the task
     */
    @Override
    public WorkflowTask getTaskById(String taskId) {
        String localId = getLocalValue(taskId);
        if (localId.startsWith(FlowableConstants.START_TASK_PREFIX)) {
            String processInstanceId = localId.substring(FlowableConstants.START_TASK_PREFIX.length());
            ProcessInstance processInstance = flowableProcessInstanceService.getProcessInstanceById(processInstanceId);
            if (processInstance != null) {
                ProcessDefinition processDefinition = flowableProcessDefinitionService.getProcessDefinitionById(processInstance.getProcessDefinitionId());
                return flowableTransformService.transformStartTask(processDefinition, processInstance);
            } else {
                HistoricProcessInstance historicProcessInstance = flowableHistoryService.getProcessInstanceById(processInstanceId);
                ProcessDefinition processDefinition = flowableProcessDefinitionService.getProcessDefinitionById(historicProcessInstance.getProcessDefinitionId());
                return flowableTransformService.transformStartTask(processDefinition, historicProcessInstance);
            }
        } else {
            Task task = flowableTaskService.getTaskById(localId);
            if (task != null) {
                return flowableTransformService.transformTask(task);
            } else {
                HistoricTaskInstance historicTaskInstance = flowableHistoryService.getTaskInstanceById(localId);
                if (historicTaskInstance != null) {
                    return flowableTransformService.transformTask(historicTaskInstance);
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * Gets all tasks assigned to the specified authority
     *
     * @param authority          the authority
     * @param state              filter by specified workflow task state
     * @param lazyInitialization hint in order to return partially-initialized entities
     * @return the list of assigned tasks
     */
    @Override
    public List<WorkflowTask> getAssignedTasks(String authority, WorkflowTaskState state, boolean lazyInitialization) {
        return Collections.EMPTY_LIST;
    }

    /**
     * Gets the pooled tasks available to the specified authority
     *
     * @param authorities        the list of authorities
     * @param lazyInitialization hint in order to return partially-initialized entities
     * @return the list of pooled tasks
     */
    @Override
    public List<WorkflowTask> getPooledTasks(List<String> authorities, boolean lazyInitialization) {
        return Collections.EMPTY_LIST;
    }

    /**
     * @param query
     * @deprecated Use overloaded method with the {@code sameSession} parameter
     * (this method defaults the parameter to {@code false}).
     */
    @Override
    public List<WorkflowTask> queryTasks(WorkflowTaskQuery query) {
        query.setProcessId(getLocalValue(query.getProcessId()));
        List<HistoricTaskInstance> tasks = flowableHistoryService.getTasksByQuery(query);
        return flowableTransformService.transformHistoryTasks(tasks);
    }

    /**
     * Query for tasks
     *
     * @param query       the filter by which tasks are queried
     * @param sameSession indicates that the returned {@link WorkflowTask} elements will be used in
     *                    the same session. If {@code true}, the returned List will be a lazy loaded list
     *                    providing greater performance.
     * @return the list of tasks matching the specified query
     */
    @Override
    public List<WorkflowTask> queryTasks(WorkflowTaskQuery query, boolean sameSession) {
        query.setProcessId(getLocalValue(query.getProcessId()));
        List<HistoricTaskInstance> tasks = flowableHistoryService.getTasksByQuery(query);
        return flowableTransformService.transformHistoryTasks(tasks);
    }

    /**
     * Count the number of active tasks that match the given query.
     *
     * @param query the filter by which tasks are queried
     * @return number of matching tasks.
     */
    @Override
    public long countTasks(WorkflowTaskQuery query) {
        query.setProcessId(getLocalValue(query.getProcessId()));
        return flowableHistoryService.getTasksCountByQuery(query);
    }

    /**
     * Update the Properties and Associations of a Task
     *
     * @param taskId     the task id to update
     * @param properties the map of properties to set on the task (or null, if none to set)
     * @param add        the map of items to associate with the task (or null, if none to add)
     * @param remove     the map of items to dis-associate with the task (or null, if none to remove)
     * @return the update task
     */
    @Override
    public WorkflowTask updateTask(String taskId, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add, Map<QName, List<NodeRef>> remove) {
        Task taskInstance = flowableTaskService.getTaskById(getLocalValue(taskId));
        if (taskInstance != null) {
            WorkflowTask task = flowableTransformService.transformTask(taskInstance);
            Map<QName, Serializable> newProperties = flowablePropertyConverter.getNewTaskProperties(taskInstance, properties, add, remove);
            Map<String, Object> transformedProperties = workflowPropertyHandlerRegistry.handleVariablesToSet(newProperties, task.getDefinition().getMetadata(),
                    null, Void.class);
            taskService.setVariablesLocal(getLocalValue(taskId), transformedProperties);
            flowablePropertyConverter.setTaskOwner(taskInstance, properties);
            return getTaskById(taskId);
        } else {
            return null;
        }
    }

    /**
     * Start the specified Task
     * <p>
     * Note: this is an optional task operation.  It may be used to track
     * when work started on a task as well as resume a suspended task.
     *
     * @param taskId the task to start
     * @return the updated task
     */
    @Override
    public WorkflowTask startTask(String taskId) {
        return null;
    }

    /**
     * Suspend the specified Task
     *
     * @param taskId String
     * @return the update task
     */
    @Override
    public WorkflowTask suspendTask(String taskId) {
        return null;
    }

    /**
     * End the Task (i.e. complete the task)
     *
     * @param taskId       the task id to end
     * @param transitionId the task transition id to take on completion (or null, for the default transition)
     * @return the updated task
     */
    @Override
    public WorkflowTask endTask(String taskId, String transitionId) {
        String localId = getLocalValue(taskId);
        if (localId.startsWith(FlowableConstants.START_TASK_PREFIX)) {
            return endStartTask(localId);
        } else {
            return endNormalTask(localId);
        }
    }

    /**
     * End start task
     * @param taskId Task id
     * @return Workflow task
     */
    private WorkflowTask endStartTask(String taskId) {
        String processInstanceId = taskId.substring(FlowableConstants.START_TASK_PREFIX.length());
        runtimeService.setVariable(processInstanceId, FlowableConstants.PROP_START_TASK_END_DATE, new Date());
        ProcessInstance processInstance = flowableProcessInstanceService.getProcessInstanceById(processInstanceId);
        ProcessDefinition processDefinition = flowableProcessDefinitionService.getProcessDefinitionById(processInstance.getProcessDefinitionId());
        return flowableTransformService.transformStartTask(processDefinition, processInstance);
    }

    /**
     * End normal task
     * @param taskId Task id
     * @return Workflow task
     */
    private WorkflowTask endNormalTask(String taskId) {
        Task task = flowableTaskService.getTaskById(taskId);
        if (task != null) {
            WorkflowTask endedTask = flowableTransformService.transformTask(task);
            taskService.complete(task.getId());
            return endedTask;
        } else {
            return null;
        }
    }

    /**
     * Gets all active timers for the specified workflow
     * @param workflowInstanceId
     * @return the list of active timers
     */
    @Override
    public WorkflowTask getStartTask(String workflowInstanceId) {
        /** Load process instance */
        ProcessInstance processInstance = flowableProcessInstanceService.getProcessInstanceById(getLocalValue(workflowInstanceId));
        if (processInstance != null) {
            return flowableTransformService.transformStartTask(
                    flowableProcessDefinitionService.getProcessDefinitionById(processInstance.getProcessDefinitionId()), processInstance);
        } else {
            HistoricProcessInstance historicProcessInstance = flowableHistoryService.getProcessInstanceById(getLocalValue(workflowInstanceId));
            return flowableTransformService.transformStartTask(
                    flowableProcessDefinitionService.getProcessDefinitionById(historicProcessInstance.getProcessDefinitionId()), historicProcessInstance);
        }
    }

    /**
     * Gets all start tasks for the specified workflow
     *
     * @param workflowInstanceIds
     * @param sameSession
     * @return the list of start tasks
     */
    @Override
    public List<WorkflowTask> getStartTasks(List<String> workflowInstanceIds, boolean sameSession) {
        return Collections.EMPTY_LIST;
    }

    /**
     * Get local value
     * @param rawValue Raw value
     * @return Local value
     */
    private String getLocalValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        return rawValue.startsWith(ENGINE_PREFIX) ? rawValue.substring(ENGINE_PREFIX.length()) : rawValue;
    }


}
