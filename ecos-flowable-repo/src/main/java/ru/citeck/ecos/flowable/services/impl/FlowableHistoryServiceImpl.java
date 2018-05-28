package ru.citeck.ecos.flowable.services.impl;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.*;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import ru.citeck.ecos.flowable.services.FlowableHistoryService;

import java.util.*;

/**
 * Flowable history service
 */
public class FlowableHistoryServiceImpl implements FlowableHistoryService {

    /**
     * Constants
     */
    private static final String ENGINE_PREFIX = "flowable$";
    private static final QName INITIATOR_QNAME = QName.createQName("initiator");
    private static final QName INITIATOR_USERNAME_QNAME = QName.createQName("initiator_username");
    private static final QName PRIORITY_QNAME = WorkflowModel.PROP_WORKFLOW_PRIORITY;
    private static final QName DUE_DATE_QNAME = WorkflowModel.PROP_WORKFLOW_DUE_DATE;
    private static final String PRIORITY_VARIABLE_NAME = "bpm_workflowPriority";
    private static final String DUE_DATE_VARIABLE_NAME = "bpm_workflowDueDate";

    /**
     * History service
     */
    private HistoryService historyService;

    /**
     * Person service
     */
    private PersonService personService;


    /**
     * Set history service
     *
     * @param historyService History service
     */
    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * Set person service
     *
     * @param personService Person service
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Get history process instance by id
     *
     * @param processInstanceId Process instance id
     * @return History process instance
     */
    @Override
    public HistoricProcessInstance getProcessInstanceById(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    /**
     * Get history task instance by id
     *
     * @param taskId Task id
     * @return History task instance
     */
    @Override
    public HistoricTaskInstance getTaskInstanceById(String taskId) {
        return historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
    }

    /**
     * Get history process instances by ids
     *
     * @param processInstanceIds Process instance ids
     * @return History process instances
     */
    @Override
    public List<HistoricProcessInstance> getProcessInstancesByIds(List<String> processInstanceIds) {
        if (CollectionUtils.isNotEmpty(processInstanceIds)) {
            return historyService.createHistoricProcessInstanceQuery().processInstanceIds(new HashSet<>(processInstanceIds)).list();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get all completed process instances
     *
     * @return List of process instances
     */
    @Override
    public List<HistoricProcessInstance> getAllCompletedProcessInstances() {
        return historyService.createHistoricProcessInstanceQuery().finished().list();
    }

    /**
     * Get all active process instances
     *
     * @return List of process instances
     */
    @Override
    public List<HistoricProcessInstance> getAllActiveProcessInstances() {
        return historyService.createHistoricProcessInstanceQuery().unfinished().list();
    }

    /**
     * Get all process instances
     *
     * @return List of process instances
     */
    @Override
    public List<HistoricProcessInstance> getAllProcessInstances() {
        return historyService.createHistoricProcessInstanceQuery().list();
    }

    /**
     * Get process instances by query
     *
     * @param workflowInstanceQuery Workflow query
     * @param maxItems              Max items
     * @param skipCount             Skip count
     * @return List of process instances
     */
    @Override
    public List<HistoricProcessInstance> getProcessInstancesByQuery(WorkflowInstanceQuery workflowInstanceQuery, int maxItems, int skipCount) {
        HistoricProcessInstanceQuery query = buildProcessInstanceQuery(workflowInstanceQuery);
        List<HistoricProcessInstance> historicProcessInstances = query.listPage(skipCount, maxItems);
        return historicProcessInstances;
    }

    /**
     * Get process instances by query
     *
     * @param workflowInstanceQuery Workflow query
     * @return List of process instances
     */
    @Override
    public List<HistoricProcessInstance> getProcessInstancesByQuery(WorkflowInstanceQuery workflowInstanceQuery) {
        HistoricProcessInstanceQuery query = buildProcessInstanceQuery(workflowInstanceQuery);
        List<HistoricProcessInstance> historicProcessInstances = query.list();
        return historicProcessInstances;
    }

    /**
     * Get process instances count by query
     *
     * @param workflowInstanceQuery Workflow query
     * @return Process instances count
     */
    @Override
    public long getProcessInstancesCountByQuery(WorkflowInstanceQuery workflowInstanceQuery) {
        HistoricProcessInstanceQuery query = buildProcessInstanceQuery(workflowInstanceQuery);
        return query.count();
    }

    /**
     * Build process instance query
     *
     * @param workflowInstanceQuery Workflow instance query
     * @return History process instance query
     */
    private HistoricProcessInstanceQuery buildProcessInstanceQuery(WorkflowInstanceQuery workflowInstanceQuery) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        // Workflow definition
        if (workflowInstanceQuery.getWorkflowDefinitionId() != null) {
            query = query.processDefinitionId(getLocalValue(workflowInstanceQuery.getWorkflowDefinitionId()));
        }
        // Start after
        if (workflowInstanceQuery.getStartAfter() != null) {
            query = query.startedAfter(workflowInstanceQuery.getStartAfter());
        }
        // Start before
        if (workflowInstanceQuery.getStartBefore() != null) {
            query = query.startedBefore(workflowInstanceQuery.getStartBefore());
        }
        // Active status
        if (workflowInstanceQuery.getActive() != null) {
            query = workflowInstanceQuery.getActive() ? query.unfinished() : query.finished();
        }

        if (workflowInstanceQuery.getCustomProps() != null) {
            // Initiator
            if (workflowInstanceQuery.getCustomProps().containsKey(INITIATOR_QNAME)) {
                NodeRef initiator = (NodeRef) workflowInstanceQuery.getCustomProps().get(INITIATOR_QNAME);
                PersonService.PersonInfo personInfo = personService.getPerson(initiator);
                query = query.variableValueEquals(INITIATOR_USERNAME_QNAME.getLocalName(), personInfo.getUserName());
            }
            // Priority
            if (workflowInstanceQuery.getCustomProps().containsKey(PRIORITY_QNAME)) {
                String priorityValue = (String) workflowInstanceQuery.getCustomProps().get(PRIORITY_QNAME);
                query = query.variableValueEquals(PRIORITY_VARIABLE_NAME, Integer.valueOf(priorityValue));
            }
            // Due date
            if (workflowInstanceQuery.getCustomProps().containsKey(DUE_DATE_QNAME)) {
                Object datesProperty = workflowInstanceQuery.getCustomProps().get(DUE_DATE_QNAME);
                if (datesProperty != null) {
                    Map<WorkflowInstanceQuery.DatePosition, Date> dates = (Map<WorkflowInstanceQuery.DatePosition, Date>) datesProperty;
                    if (dates.get(WorkflowInstanceQuery.DatePosition.BEFORE) != null) {
                        query = query.variableValueLessThanOrEqual(DUE_DATE_VARIABLE_NAME, dates.get(WorkflowInstanceQuery.DatePosition.BEFORE));
                    }
                    if (dates.get(WorkflowInstanceQuery.DatePosition.AFTER) != null) {
                        query = query.variableValueGreaterThanOrEqual(DUE_DATE_VARIABLE_NAME, dates.get(WorkflowInstanceQuery.DatePosition.AFTER));
                    }
                } else {
                    query.variableValueEquals(DUE_DATE_VARIABLE_NAME, null);
                }
            }
        }

        return query;
    }

    /**
     * Get tasks by workflow task query
     *
     * @param workflowTaskQuery Workflow task query
     * @return List of tasks
     */
    @Override
    public List<HistoricTaskInstance> getTasksByQuery(WorkflowTaskQuery workflowTaskQuery) {
        return buildTaskQuery(workflowTaskQuery).list();
    }

    /**
     * Get tasks count by workflow task query
     *
     * @param workflowTaskQuery Workflow task query
     * @return Tasks count
     */
    @Override
    public long getTasksCountByQuery(WorkflowTaskQuery workflowTaskQuery) {
        return buildTaskQuery(workflowTaskQuery).count();
    }

    /**
     * Build task query
     *
     * @param workflowTaskQuery Workflow task query
     * @return Task query
     */
    private HistoricTaskInstanceQuery buildTaskQuery(WorkflowTaskQuery workflowTaskQuery) {
        HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery();
        // Process instance id
        if (workflowTaskQuery.getProcessId() != null) {
            taskInstanceQuery = taskInstanceQuery.processInstanceId(workflowTaskQuery.getProcessId());
        }
        // State
        if (workflowTaskQuery.getTaskState() != null) {
            if (workflowTaskQuery.getTaskState() == WorkflowTaskState.IN_PROGRESS) {
                taskInstanceQuery = taskInstanceQuery.unfinished();
            } else {
                taskInstanceQuery = taskInstanceQuery.finished();
            }
        }
        return taskInstanceQuery;
    }

    /**
     * Get history activity instance
     *
     * @param processInstanceId Process instance id
     * @param startActivityId   Start activity id
     * @return History activity instance
     */
    @Override
    public HistoricActivityInstance getHistoryActivityInstance(String processInstanceId, String startActivityId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityId(startActivityId)
                .singleResult();
    }

    /**
     * Get variables by activity id
     *
     * @param activityId Activity id
     * @return Map of variables
     */
    @Override
    public Map<String, Object> getVariablesByActivityId(String activityId) {
        HistoricDetailQuery query = historyService.createHistoricDetailQuery().activityInstanceId(activityId);
        return getHistoricVariables(query);
    }

    /**
     * Het history variables
     *
     * @param query History detail query
     * @return Map of parameters
     */
    private Map<String, Object> getHistoricVariables(HistoricDetailQuery query) {
        List<HistoricDetail> historicDetails = query.variableUpdates().list();
        return convertHistoricDetails(historicDetails);
    }

    /**
     * Convert a list of history details to a map with key-value pairs.
     *
     * @param details History details
     * @return Map of parameters
     */
    public Map<String, Object> convertHistoricDetails(List<HistoricDetail> details) {
        HashMap<String, HistoricVariableUpdate> updateMap = new HashMap<>();
        HistoricVariableUpdate previous;
        HistoricVariableUpdate current;
        boolean isMoreRecent;
        for (HistoricDetail detail : details) {
            current = (HistoricVariableUpdate) detail;
            previous = updateMap.get(current.getVariableName());
            if (previous == null) {
                isMoreRecent = true;
            } else {
                if (current.getTime().equals(previous.getTime())) {
                    if (current.getRevision() == previous.getRevision()) {
                        isMoreRecent = Long.valueOf(current.getId()).longValue() > Long.valueOf(previous.getId()).longValue();
                    } else {
                        isMoreRecent = current.getRevision() > previous.getRevision();
                    }
                } else {
                    isMoreRecent = current.getTime().after(previous.getTime());
                }
            }
            if (isMoreRecent) {
                updateMap.put(current.getVariableName(), current);
            }
        }
        HashMap<String, Object> variables = new HashMap<>();
        for (Map.Entry<String, HistoricVariableUpdate> entry : updateMap.entrySet()) {
            variables.put(entry.getKey(), entry.getValue().getValue());
        }
        return variables;
    }

    /**
     * Get local value
     *
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
