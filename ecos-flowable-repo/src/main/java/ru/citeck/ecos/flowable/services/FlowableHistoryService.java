package ru.citeck.ecos.flowable.services;

import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;

import java.util.List;
import java.util.Map;

/**
 * Flowable history service interface
 */
public interface FlowableHistoryService {

    /**
     * Get history process instance by id
     * @param processInstanceId Process instance id
     * @return History process instance
     */
    HistoricProcessInstance getProcessInstanceById(String processInstanceId);

    /**
     * Get history task instance by id
     * @param taskId Task id
     * @return History task instance
     */
    HistoricTaskInstance getTaskInstanceById(String taskId);

    /**
     * Get history process instances by ids
     * @param processInstanceIds Process instance ids
     * @return History process instances
     */
    List<HistoricProcessInstance> getProcessInstancesByIds(List<String> processInstanceIds);

    /**
     * Get all completed process instances
     * @return List of process instances
     */
    List<HistoricProcessInstance> getAllCompletedProcessInstances();

    /**
     * Get all active process instances
     * @return List of process instances
     */
    List<HistoricProcessInstance> getAllActiveProcessInstances();

    /**
     * Get all process instances
     * @return List of process instances
     */
    List<HistoricProcessInstance> getAllProcessInstances();

    /**
     * Get process instances by query
     * @param workflowInstanceQuery Workflow query
     * @param maxItems Max items
     * @param skipCount Skip count
     * @return List of process instances
     */
    List<HistoricProcessInstance> getProcessInstancesByQuery(WorkflowInstanceQuery workflowInstanceQuery, int maxItems, int skipCount);

    /**
     * Get process instances by query
     * @param workflowInstanceQuery Workflow query
     * @return List of process instances
     */
    List<HistoricProcessInstance> getProcessInstancesByQuery(WorkflowInstanceQuery workflowInstanceQuery);

    /**
     * Get process instances count by query
     * @param workflowInstanceQuery Workflow query
     * @return Process instances count
     */
    long getProcessInstancesCountByQuery(WorkflowInstanceQuery workflowInstanceQuery);

    /**
     * Get tasks by workflow task query
     * @param workflowTaskQuery Workflow task query
     * @return List of tasks
     */
    List<HistoricTaskInstance> getTasksByQuery(WorkflowTaskQuery workflowTaskQuery);

    /**
     * Get tasks count by workflow task query
     * @param workflowTaskQuery Workflow task query
     * @return Tasks count
     */
    long getTasksCountByQuery(WorkflowTaskQuery workflowTaskQuery);

    /**
     * Get history activity instance
     * @param processInstanceId Process instance id
     * @param startActivityId Start activity id
     * @return History activity instance
     */
    HistoricActivityInstance getHistoryActivityInstance(String processInstanceId, String startActivityId);

    /**
     * Get variables by activity id
     * @param activityId Activity id
     * @return Map of variables
     */
    Map<String, Object> getVariablesByActivityId(String activityId);

}
