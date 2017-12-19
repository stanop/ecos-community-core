package ru.citeck.ecos.flowable.services.impl;

import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.FormService;
import org.flowable.engine.form.StartFormData;
import org.flowable.engine.form.TaskFormData;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricTaskInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.converters.FlowablePropertyConverter;
import ru.citeck.ecos.flowable.services.FlowableHistoryService;
import ru.citeck.ecos.flowable.services.FlowableProcessDefinitionService;
import ru.citeck.ecos.flowable.services.FlowableProcessInstanceService;
import ru.citeck.ecos.flowable.services.FlowableTransformService;

import java.util.ArrayList;
import java.util.List;

/**
 * Flowable transform service
 */
public class FlowableTransformServiceImpl implements FlowableTransformService {

    /**
     * Logger
     */
    private static Log logger = LogFactory.getLog(FlowableTransformServiceImpl.class);

    /**
     * Constants
     */
    private static final String ENGINE_PREFIX = "flowable$";
    private static final String DEFAULT_TASK_VIEW_TYPE = "wfcf:defaultTask";

    /**
     * Namespace service
     */
    private NamespaceService namespaceService;

    /**
     * Dictionary service
     */
    private DictionaryService dictionaryService;

    /**
     * Person service
     */
    private PersonService personService;

    /**
     * Authentication service
     */
    private AuthenticationService authenticationService;

    /**
     * Flowable process definition service
     */
    private FlowableProcessDefinitionService flowableProcessDefinitionService;

    /**
     * Form service
     */
    private FormService formService;

    /**
     * Flowable process instance service
     */
    private FlowableProcessInstanceService flowableProcessInstanceService;

    /**
     * Flowable history service
     */
    private FlowableHistoryService flowableHistoryService;

    /**
     * Flowable property converter
     */
    private FlowablePropertyConverter flowablePropertyConverter;

    /**
     * Set flowable history service
     * @param flowableHistoryService Flowable history service
     */
    public void setFlowableHistoryService(FlowableHistoryService flowableHistoryService) {
        this.flowableHistoryService = flowableHistoryService;
    }

    /**
     * Set flowable process instance service
     * @param flowableProcessInstanceService Flowable process instance service
     */
    public void setFlowableProcessInstanceService(FlowableProcessInstanceService flowableProcessInstanceService) {
        this.flowableProcessInstanceService = flowableProcessInstanceService;
    }

    /**
     * Set flowable process definition service
     * @param flowableProcessDefinitionService Flowable process definition service
     */
    public void setFlowableProcessDefinitionService(FlowableProcessDefinitionService flowableProcessDefinitionService) {
        this.flowableProcessDefinitionService = flowableProcessDefinitionService;
    }

    /**
     * Set form service
     * @param formService Form service
     */
    public void setFormService(FormService formService) {
        this.formService = formService;
    }

    /**
     * Set namespace service
     * @param namespaceService Namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * Dictionary service
     * @param dictionaryService Dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set authentication service
     * @param authenticationService Authentication service
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Set person service
     * @param personService Person service
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Set flowable property converter
     * @param flowablePropertyConverter Flowable property converter
     */
    public void setFlowablePropertyConverter(FlowablePropertyConverter flowablePropertyConverter) {
        this.flowablePropertyConverter = flowablePropertyConverter;
    }

    /**
     * Transform process definition to alfresco workflow definition
     * @param processDefinition Process definition
     * @return Workflow definition
     */
    @Override
    public WorkflowDefinition transformProcessDefinition(ProcessDefinition processDefinition) {
        /** Workflow definition */
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
     * @param processDefinition Process definition
     * @return Workflow task definition
     */
    @Override
    public WorkflowTaskDefinition transformStartTaskDefinition(ProcessDefinition processDefinition) {
        StartFormData startFormData = formService.getStartFormData(processDefinition.getId());
        /** Task node */
        WorkflowNode taskNode = new WorkflowNode(
                startFormData.getProcessDefinition().getKey(),
                startFormData.getProcessDefinition().getName(),
                startFormData.getProcessDefinition().getKey(),
                "",
                true
        );
        /** Start task definition */
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
     * @param task Task
     * @return Workflow task definition
     */
    @Override
    public WorkflowTaskDefinition transformTaskDefinition(Task task) {
        /** Task node */
        WorkflowNode taskNode = new WorkflowNode(
                task.getTaskDefinitionKey(),
                task.getName(),
                task.getTaskDefinitionKey(),
                "",
                true
        );
        /** Task definition */
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
     * @param task Task
     * @return Workflow task definition
     */
    @Override
    public WorkflowTaskDefinition transformTaskDefinition(HistoricTaskInstance task) {
        /** Task node */
        WorkflowNode taskNode = new WorkflowNode(
                task.getTaskDefinitionKey(),
                task.getName(),
                task.getTaskDefinitionKey(),
                "",
                true
        );
        /** Task definition */
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
     * @param processInstance Process instance
     * @return Workflow path
     */
    @Override
    public WorkflowPath transformHistoryProcessInstanceToWorkflowPath(HistoricProcessInstance processInstance) {
        if (processInstance != null) {
            /** Workflow path */
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
     * @param processInstance Process instance
     * @return Workflow instance
     */
    @Override
    public WorkflowInstance transformProcessInstanceToWorkflowInstance(ProcessInstance processInstance) {
        if (processInstance != null) {
            HistoricProcessInstance historicProcessInstance = flowableHistoryService.getProcessInstanceById(processInstance.getId());
            return new WorkflowInstance(
                    ENGINE_PREFIX + processInstance.getId(),
                    transformProcessDefinition(flowableProcessDefinitionService.getProcessDefinitionById(processInstance.getProcessDefinitionId())),
                    processInstance.getDescription(),
                    personService.getPerson(authenticationService.getCurrentUserName()),
                    null,
                    null,
                    !processInstance.isEnded(),
                    processInstance.getStartTime(),
                    historicProcessInstance!= null ? historicProcessInstance.getEndTime() : null
            );
        } else {
            return null;
        }
    }

    /**
     * Transform process instances to alfresco workflow instances
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
     * @param processInstance Process instance
     * @return Workflow instance
     */
    @Override
    public WorkflowInstance transformHistoryProcessInstanceToWorkflowInstance(HistoricProcessInstance processInstance) {
        if (processInstance != null) {
            return new WorkflowInstance(
                    ENGINE_PREFIX + processInstance.getId(),
                    transformProcessDefinition(flowableProcessDefinitionService.getProcessDefinitionById(processInstance.getProcessDefinitionId())),
                    processInstance.getDescription(),
                    personService.getPerson(authenticationService.getCurrentUserName()),
                    null,
                    null,
                    processInstance.getEndTime() == null,
                    processInstance.getStartTime(),
                    processInstance.getEndTime()
            );
        } else {
            return null;
        }
    }

    /**
     * Transform process instances to alfresco workflow instances
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
}
