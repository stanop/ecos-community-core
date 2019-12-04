package ru.citeck.ecos.flowable.services.impl;

import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.flowable.engine.FormService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.form.StartFormData;
import org.flowable.engine.form.TaskFormData;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.converters.FlowablePropertyConverter;
import ru.citeck.ecos.flowable.services.FlowableHistoryService;
import ru.citeck.ecos.flowable.services.FlowableProcessDefinitionService;
import ru.citeck.ecos.flowable.services.FlowableProcessInstanceService;
import ru.citeck.ecos.flowable.services.FlowableTransformService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static ru.citeck.ecos.flowable.constants.FlowableConstants.ENGINE_PREFIX;

/**
 * Flowable transform service
 */
public class FlowableTransformServiceImpl implements FlowableTransformService {

    private static final String DEFAULT_TASK_VIEW_TYPE = "wfcf:defaultTask";

    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private FlowableProcessDefinitionService flowableProcessDefinitionService;
    private FormService formService;
    private FlowableProcessInstanceService flowableProcessInstanceService;
    private FlowableHistoryService flowableHistoryService;
    private FlowablePropertyConverter flowablePropertyConverter;
    private RuntimeService runtimeService;

    /**
     * Transform process definition to alfresco workflow definition
     *
     * @param processDefinition Process definition
     * @return Workflow definition
     */
    @Override
    public WorkflowDefinition transformProcessDefinition(ProcessDefinition processDefinition) {
        /* Workflow definition */
        return new WorkflowDefinition(
                ENGINE_PREFIX + processDefinition.getId(),
                ENGINE_PREFIX + processDefinition.getKey(),
                new Integer(processDefinition.getVersion()).toString(),
                processDefinition.getName(),
                processDefinition.getName(),
                transformStartTaskDefinition(processDefinition)
        );
    }

    /**
     * Transform process definitions to alfresco workflow definitions
     *
     * @param processDefinitions Process definitions
     * @return Workflow definitions
     */
    @Override
    public List<WorkflowDefinition> transformProcessDefinitions(List<ProcessDefinition> processDefinitions) {
        List<WorkflowDefinition> result = new ArrayList<>(processDefinitions.size());
        for (ProcessDefinition processDefinition : processDefinitions) {
            result.add(transformProcessDefinition(processDefinition));
        }
        return result;
    }

    /**
     * Transform start task
     *
     * @param processDefinition Process definition
     * @param processInstance   Process instance
     * @return Workflow task
     */
    @Override
    public WorkflowTask transformStartTask(ProcessDefinition processDefinition, ProcessInstance processInstance) {
        WorkflowTaskDefinition taskDefinition = transformStartTaskDefinition(processDefinition);
        HistoricProcessInstance historicProcessInstance = flowableHistoryService.getProcessInstanceById(processInstance.getId());
        return new WorkflowTask(
                ENGINE_PREFIX + FlowableConstants.START_TASK_PREFIX + processInstance.getId(),
                taskDefinition,
                taskDefinition.getId(),
                processDefinition.getName(),
                taskDefinition.getId(),
                historicProcessInstance.getEndTime() != null ? WorkflowTaskState.COMPLETED : WorkflowTaskState.IN_PROGRESS,
                transformProcessInstanceToWorkflowPath(processInstance),
                flowablePropertyConverter.getStartTaskProperties(
                        historicProcessInstance, taskDefinition.getMetadata(), historicProcessInstance.getEndTime() != null)
        );
    }

    /**
     * Transform start task
     *
     * @param processDefinition Process definition
     * @param processInstance   History process instance
     * @return Workflow task
     */
    @Override
    public WorkflowTask transformStartTask(ProcessDefinition processDefinition, HistoricProcessInstance processInstance) {
        WorkflowTaskDefinition taskDefinition = transformStartTaskDefinition(processDefinition);
        return new WorkflowTask(
                ENGINE_PREFIX + FlowableConstants.START_TASK_PREFIX + processInstance.getId(),
                taskDefinition,
                taskDefinition.getId(),
                processDefinition.getName(),
                taskDefinition.getId(),
                processInstance.getEndTime() != null ? WorkflowTaskState.COMPLETED : WorkflowTaskState.IN_PROGRESS,
                transformHistoryProcessInstanceToWorkflowPath(processInstance),
                flowablePropertyConverter.getStartTaskProperties(
                        processInstance, taskDefinition.getMetadata(), processInstance.getEndTime() != null)
        );
    }

    /**
     * Transform start task definition
     *
     * @param processDefinition Process definition
     * @return Workflow task definition
     */
    @Override
    public WorkflowTaskDefinition transformStartTaskDefinition(ProcessDefinition processDefinition) {
        StartFormData startFormData = formService.getStartFormData(processDefinition.getId());
        /* Task node */
        WorkflowNode taskNode = new WorkflowNode(
                startFormData.getProcessDefinition().getKey(),
                startFormData.getProcessDefinition().getName(),
                startFormData.getProcessDefinition().getKey(),
                "",
                true
        );
        /* Start task definition */
        TypeDefinition typeDefinition = getTypeDefinition(startFormData != null ? startFormData.getFormKey() : null);
        if (typeDefinition == null) {
            typeDefinition = getTypeDefinition(DEFAULT_TASK_VIEW_TYPE);
        }
        return new WorkflowTaskDefinition(
                startFormData.getFormKey() != null ? startFormData.getFormKey() : DEFAULT_TASK_VIEW_TYPE,
                taskNode, typeDefinition);
    }

    /**
     * Transform task to workflow task definition
     *
     * @param task Task
     * @return Workflow task definition
     */
    @Override
    public WorkflowTaskDefinition transformTaskDefinition(Task task) {
        /* Task node */
        WorkflowNode taskNode = new WorkflowNode(
                task.getTaskDefinitionKey(),
                task.getName(),
                task.getTaskDefinitionKey(),
                "",
                true
        );
        /* Task definition */
        TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        TypeDefinition typeDefinition = getTypeDefinition(taskFormData != null ? taskFormData.getFormKey() : null);
        if (typeDefinition == null) {
            typeDefinition = getTypeDefinition(DEFAULT_TASK_VIEW_TYPE);
        }
        return new WorkflowTaskDefinition(
                taskFormData.getFormKey() != null ? taskFormData.getFormKey() : DEFAULT_TASK_VIEW_TYPE,
                taskNode,
                typeDefinition
        );
    }

    /**
     * Transform task to workflow task definition
     *
     * @param task Task
     * @return Workflow task definition
     */
    @Override
    public WorkflowTaskDefinition transformTaskDefinition(HistoricTaskInstance task) {
        /* Task node */
        WorkflowNode taskNode = new WorkflowNode(
                task.getTaskDefinitionKey(),
                task.getName(),
                task.getTaskDefinitionKey(),
                "",
                true
        );
        /* Task definition */
        TypeDefinition typeDefinition = getTypeDefinition(task.getFormKey());
        if (typeDefinition == null) {
            typeDefinition = getTypeDefinition(DEFAULT_TASK_VIEW_TYPE);
        }
        return new WorkflowTaskDefinition(
                task.getFormKey() != null ? task.getFormKey() : DEFAULT_TASK_VIEW_TYPE,
                taskNode,
                typeDefinition
        );
    }

    /**
     * Transform tasks to workflow task definitions
     *
     * @param tasks Tasks
     * @return Workflow task definitions
     */
    @Override
    public List<WorkflowTaskDefinition> transformTaskDefinitions(List<Task> tasks) {
        List<WorkflowTaskDefinition> result = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            result.add(transformTaskDefinition(task));
        }
        return result;
    }

    /**
     * Transform process instance to alfresco workflow path
     *
     * @param processInstance Process instance
     * @return Workflow path
     */
    @Override
    public WorkflowPath transformProcessInstanceToWorkflowPath(ProcessInstance processInstance) {
        if (processInstance != null) {
            return new WorkflowPath(
                    ENGINE_PREFIX + processInstance.getId(),
                    transformProcessInstanceToWorkflowInstance(processInstance),
                    null,
                    !processInstance.isEnded()
            );
        } else {
            return null;
        }
    }

    /**
     * Transform process instance to alfresco workflow path
     *
     * @param processInstance Process instance
     * @return Workflow path
     */
    @Override
    public WorkflowPath transformHistoryProcessInstanceToWorkflowPath(HistoricProcessInstance processInstance) {
        if (processInstance != null) {
            /* Workflow path */
            return new WorkflowPath(
                    ENGINE_PREFIX + processInstance.getId(),
                    transformHistoryProcessInstanceToWorkflowInstance(processInstance),
                    null,
                    processInstance.getEndTime() != null
            );
        } else {
            return null;
        }
    }

    /**
     * Transform process instance to alfresco workflow instance
     *
     * @param processInstance Process instance
     * @return Workflow instance
     */
    @Override
    public WorkflowInstance transformProcessInstanceToWorkflowInstance(ProcessInstance processInstance) {
        if (processInstance != null) {
            HistoricProcessInstance historicProcessInstance = flowableHistoryService.getProcessInstanceById(processInstance.getId());
            WorkflowInstance result = new WorkflowInstance(
                    ENGINE_PREFIX + processInstance.getId(),
                    transformProcessDefinition(flowableProcessDefinitionService.getProcessDefinitionById(processInstance.getProcessDefinitionId())),
                    (String) getProcessVariable(processInstance.getId(), "bpm_workflowDescription"),
                    (NodeRef) getProcessVariable(processInstance.getId(), "initiator"),
                    (NodeRef) getProcessVariable(processInstance.getId(), "bpm_package"),
                    null,
                    !processInstance.isEnded(),
                    processInstance.getStartTime(),
                    historicProcessInstance != null ? historicProcessInstance.getEndTime() : null
            );
            result.dueDate = (Date) getProcessVariable(processInstance.getId(), "bpm_workflowDueDate");
            return result;
        } else {
            return null;
        }
    }

    /**
     * Transform process instances to alfresco workflow instances
     *
     * @param processInstances Process instances
     * @return Workflow instances
     */
    @Override
    public List<WorkflowInstance> transformProcessInstancesToWorkflowInstances(List<ProcessInstance> processInstances) {
        List<WorkflowInstance> result = new ArrayList<>(processInstances.size());
        for (ProcessInstance processInstance : processInstances) {
            result.add(transformProcessInstanceToWorkflowInstance(processInstance));
        }
        return result;
    }

    /**
     * Transform history process instance to alfresco workflow instance
     *
     * @param processInstance Process instance
     * @return Workflow instance
     */
    @Override
    public WorkflowInstance transformHistoryProcessInstanceToWorkflowInstance(HistoricProcessInstance processInstance) {
        if (processInstance != null) {
            WorkflowInstance result = new WorkflowInstance(
                    ENGINE_PREFIX + processInstance.getId(),
                    transformProcessDefinition(flowableProcessDefinitionService.getProcessDefinitionById(
                            processInstance.getProcessDefinitionId())),
                    (String) getHistoryProcessVariable(processInstance.getId(), "bpm_workflowDescription"),
                    (NodeRef) getHistoryProcessVariable(processInstance.getId(), "initiator"),
                    (NodeRef) getHistoryProcessVariable(processInstance.getId(), "bpm_package"),
                    null,
                    processInstance.getEndTime() == null,
                    processInstance.getStartTime(),
                    processInstance.getEndTime()
            );
            result.dueDate = (Date) getHistoryProcessVariable(processInstance.getId(), "bpm_workflowDueDate");
            return result;
        } else {
            return null;
        }
    }

    /**
     * Get process variable
     *
     * @param processId   Process id
     * @param variableKey Variable key
     * @return Process variable
     */
    private Object getProcessVariable(String processId, String variableKey) {
        Map<String, Object> variables = runtimeService.getVariables(processId);
        if (variables == null) {
            return null;
        }
        return variables.get(variableKey);
    }

    /**
     * Get history process variable
     *
     * @param variableKey Variable key
     * @return Process variable
     */
    private Object getHistoryProcessVariable(String processId, String variableKey) {
        HistoricProcessInstance historicProcessInstance = flowableHistoryService.getProcessInstanceByIdWithVariables(processId);
        if (historicProcessInstance == null) {
            return null;
        }
        Map<String, Object> variables = historicProcessInstance.getProcessVariables();
        if (variables == null) {
            return null;
        }
        return variables.get(variableKey);
    }

    /**
     * Transform process instances to alfresco workflow instances
     *
     * @param processInstances Process instances
     * @return Workflow instances
     */
    @Override
    public List<WorkflowInstance> transformHistoryProcessInstancesToWorkflowInstances(List<HistoricProcessInstance> processInstances) {
        List<WorkflowInstance> result = new ArrayList<>(processInstances.size());
        for (HistoricProcessInstance processInstance : processInstances) {
            result.add(transformHistoryProcessInstanceToWorkflowInstance(processInstance));
        }
        return result;
    }

    /**
     * Transform task to alfresco workflow task
     *
     * @param task Task
     * @return Workflow task
     */
    @Override
    public WorkflowTask transformTask(Task task) {
        ProcessInstance processInstance = flowableProcessInstanceService.getProcessInstanceById(task.getProcessInstanceId());
        HistoricTaskInstance historicTaskInstance = flowableHistoryService.getTaskInstanceById(task.getId());
        WorkflowTaskDefinition taskDefinition = transformTaskDefinition(task);
        return new WorkflowTask(
                ENGINE_PREFIX + task.getId(),
                taskDefinition,
                taskDefinition.getId(),
                task.getName(),
                task.getDescription(),
                historicTaskInstance.getEndTime() != null ? WorkflowTaskState.COMPLETED : WorkflowTaskState.IN_PROGRESS,
                transformProcessInstanceToWorkflowPath(processInstance),
                flowablePropertyConverter.getTaskProperties(task)
        );
    }

    /**
     * Transform task to alfresco workflow task
     *
     * @param task Task
     * @return Workflow task
     */
    @Override
    public WorkflowTask transformTask(HistoricTaskInstance task) {
        WorkflowTaskDefinition taskDefinition = transformTaskDefinition(task);
        ProcessInstance processInstance = flowableProcessInstanceService.getProcessInstanceById(task.getProcessInstanceId());
        if (processInstance != null) {
            return new WorkflowTask(
                    ENGINE_PREFIX + task.getId(),
                    taskDefinition,
                    taskDefinition.getId(),
                    task.getName(),
                    task.getDescription(),
                    task.getEndTime() != null ? WorkflowTaskState.COMPLETED : WorkflowTaskState.IN_PROGRESS,
                    transformProcessInstanceToWorkflowPath(processInstance),
                    flowablePropertyConverter.getTaskProperties(task)
            );
        } else {
            HistoricProcessInstance historicProcessInstance = flowableHistoryService.getProcessInstanceById(task.getProcessInstanceId());
            return new WorkflowTask(
                    ENGINE_PREFIX + task.getId(),
                    taskDefinition,
                    taskDefinition.getId(),
                    task.getName(),
                    task.getDescription(),
                    task.getEndTime() != null ? WorkflowTaskState.COMPLETED : WorkflowTaskState.IN_PROGRESS,
                    transformHistoryProcessInstanceToWorkflowPath(historicProcessInstance),
                    flowablePropertyConverter.getTaskProperties(task)
            );
        }

    }

    /**
     * Transform tasks to alfresco workflow tasks
     *
     * @param tasks Tasks
     * @return Workflow tasks
     */
    @Override
    public List<WorkflowTask> transformTasks(List<Task> tasks) {
        List<WorkflowTask> result = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            result.add(transformTask(task));
        }
        return result;
    }

    /**
     * Transform tasks to alfresco workflow tasks
     *
     * @param tasks Tasks
     * @return Workflow tasks
     */
    @Override
    public List<WorkflowTask> transformHistoryTasks(List<HistoricTaskInstance> tasks) {
        List<WorkflowTask> result = new ArrayList<>(tasks.size());
        for (HistoricTaskInstance task : tasks) {
            result.add(transformTask(task));
        }
        return result;
    }

    /**
     * Get type definition
     *
     * @param formKey Form key
     * @return Type definition
     */
    private TypeDefinition getTypeDefinition(String formKey) {
        if (formKey != null) {
            QName qName = WorkflowQNameConverter.convertNameToQName(formKey, namespaceService);
            return dictionaryService.getType(qName);
        } else {
            return null;
        }
    }

    public void setFlowableHistoryService(FlowableHistoryService flowableHistoryService) {
        this.flowableHistoryService = flowableHistoryService;
    }

    public void setFlowableProcessInstanceService(FlowableProcessInstanceService flowableProcessInstanceService) {
        this.flowableProcessInstanceService = flowableProcessInstanceService;
    }

    public void setFlowableProcessDefinitionService(FlowableProcessDefinitionService flowableProcessDefinitionService) {
        this.flowableProcessDefinitionService = flowableProcessDefinitionService;
    }

    public void setFormService(FormService formService) {
        this.formService = formService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setFlowablePropertyConverter(FlowablePropertyConverter flowablePropertyConverter) {
        this.flowablePropertyConverter = flowablePropertyConverter;
    }

    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }
}
