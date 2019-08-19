package ru.citeck.ecos.flowable.converters;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.workflow.*;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.EntryTransformer;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.*;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.service.IdentityLinkType;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.flowable.FlowableWorkflowComponent;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.services.FlowableHistoryService;
import ru.citeck.ecos.flowable.services.FlowableProcessDefinitionService;
import ru.citeck.ecos.flowable.services.FlowableTaskService;
import ru.citeck.ecos.flowable.services.FlowableTaskTypeManager;
import ru.citeck.ecos.flowable.utils.FlowableWorkflowPropertyHandlerRegistry;
import ru.citeck.ecos.model.CiteckWorkflowModel;
import ru.citeck.ecos.utils.WorkflowUtils;

import java.io.Serializable;
import java.util.*;

public class FlowablePropertyConverter {

    private static final String FLOWABLE_ENGINE_NAME = "flowable";
    private static final String FLOWABLE_INITIATOR = "$INITIATOR";
    private static final String TASK_TITLE_KEY_TEMPLATE = "flowable.task.%s.title";
    private static final int DEFAULT_WORKFLOW_TASK_PRIORITY = 2;

    private TaskService taskService;
    private WorkflowObjectFactory factory;
    private WorkflowAuthorityManager authorityManager;
    private TenantService tenantService;
    private MessageService messageService;
    private DictionaryService dictionaryService;
    private NamespacePrefixResolver namespaceService;
    private NodeService nodeService;
    private FlowableTaskService flowableTaskService;
    private FlowableHistoryService flowableHistoryService;
    private FlowableProcessDefinitionService flowableProcessDefinitionService;
    private FlowableWorkflowComponent flowableWorkflowComponent;
    private FlowableTaskTypeManager typeManager;
    private FlowableWorkflowPropertyHandlerRegistry handlerRegistry;
    private PersonService personService;
    private WorkflowUtils workflowUtils;

    /**
     * Init
     */
    public void init() {
        WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(namespaceService);
        factory = new WorkflowObjectFactory(qNameConverter, tenantService, messageService, dictionaryService,
                FLOWABLE_ENGINE_NAME, WorkflowModel.TYPE_START_TASK);
    }

    /**
     * Get task properties
     *
     * @param task Task instance
     * @return Map of properties
     */
    public Map<QName, Serializable> getTaskProperties(Task task) {

        Map<String, Object> variables = taskService.getVariables(task.getId());
        Map<String, Object> localVariables = taskService.getVariablesLocal(task.getId());

        TypeDefinition taskDef = typeManager.getFullTaskDefinition(task);
        Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
        Map<QName, AssociationDefinition> taskAssociations = taskDef.getAssociations();

        Map<QName, Serializable> properties = mapArbitraryProperties(variables, localVariables, taskProperties,
                taskAssociations);

        /** Set task instance properties */
        properties.put(WorkflowModel.PROP_TASK_ID, task.getId());
        properties.put(WorkflowModel.PROP_START_DATE, task.getCreateTime());
        properties.put(WorkflowModel.PROP_COMPLETION_DATE, null);
        properties.put(WorkflowModel.PROP_PRIORITY, workflowUtils.convertPriorityBpmnToWorkflowTask(task.getPriority()));
        properties.put(ContentModel.PROP_CREATED, task.getCreateTime());
        properties.put(ContentModel.PROP_OWNER, getOwner(task));
        if (task.getName() != null) {
            properties.put(WorkflowModel.PROP_DESCRIPTION, task.getName());
        } else {
            if (!properties.containsKey(WorkflowModel.PROP_DESCRIPTION)) {
                properties.put(WorkflowModel.PROP_DESCRIPTION, task.getDescription());
            }
        }
        if (!properties.containsKey(WorkflowModel.PROP_DUE_DATE)) {
            properties.put(WorkflowModel.PROP_DUE_DATE, task.getDueDate());
        }

        String outcomeVarName = factory.mapQNameToName(WorkflowModel.PROP_OUTCOME);
        if (variables.get(outcomeVarName) != null) {
            properties.put(WorkflowModel.PROP_OUTCOME, (Serializable) variables.get(outcomeVarName));
        }
        if (!properties.containsKey(WorkflowModel.PROP_REASSIGNABLE)) {
            properties.put(WorkflowModel.PROP_REASSIGNABLE, false);
        }
        List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
        mapPooledActors(links, properties);
        return filterTaskProperties(properties);
    }

    public Task updateTask(Task task, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add,
                           Map<QName, List<NodeRef>> remove) {
        Map<QName, Serializable> newProperties = getNewTaskProperties(task, properties, add, remove);
        if (newProperties != null) {
            setTaskProperties(task, newProperties);
            return flowableTaskService.getTaskById(task.getId());
        }
        return task;
    }

    /**
     * Get task properties
     *
     * @param task           Delegate task
     * @param typeDefinition Type definition
     * @param localOnly      Local only variables
     * @return Map of properties
     */
    public Map<QName, Serializable> getTaskProperties(DelegateTask task, TypeDefinition typeDefinition, boolean localOnly) {
        Map<QName, PropertyDefinition> taskProperties = typeDefinition.getProperties();
        Map<QName, AssociationDefinition> taskAssociations = typeDefinition.getAssociations();

        Map<String, Object> localVariables = task.getVariablesLocal();
        Map<String, Object> variables;

        if (!localOnly) {
            variables = new HashMap<>(localVariables);

            Map<String, Object> executionVariables = task.getVariables();

            for (Map.Entry<String, Object> entry : executionVariables.entrySet()) {
                String key = entry.getKey();
                if (!localVariables.containsKey(key)) {
                    variables.put(key, entry.getValue());
                }
            }
        } else {
            variables = localVariables;
        }

        Map<QName, Serializable> properties = mapArbitraryProperties(variables, localVariables, taskProperties,
                taskAssociations);
        properties.put(WorkflowModel.PROP_TASK_ID, task.getId());
        properties.put(WorkflowModel.PROP_START_DATE, task.getCreateTime());
        properties.put(WorkflowModel.PROP_COMPLETION_DATE, null);
        properties.put(WorkflowModel.PROP_PRIORITY, workflowUtils.convertPriorityBpmnToWorkflowTask(task.getPriority()));
        properties.put(ContentModel.PROP_CREATED, task.getCreateTime());
        properties.put(ContentModel.PROP_OWNER, getOwner(task));
        if (!properties.containsKey(WorkflowModel.PROP_DUE_DATE)) {
            properties.put(WorkflowModel.PROP_DUE_DATE, task.getDueDate());
        }
        Set<IdentityLink> links = task.getCandidates();
        mapPooledActors(links, properties);

        return filterTaskProperties(properties);
    }

    /**
     * Get owner from delegate task
     *
     * @param task Delegate task
     * @return Owner username
     */
    private String getOwner(DelegateTask task) {
        String taskAssignee = task.getAssignee();
        if (FLOWABLE_INITIATOR.equals(taskAssignee)) {
            WorkflowInstance workflowInstance = flowableWorkflowComponent.getWorkflowById(task.getProcessInstanceId());
            return getInitiatorFromWorkflow(workflowInstance);
        } else {
            return taskAssignee;
        }
    }

    /**
     * Get owner from task
     *
     * @param task Task
     * @return Owner username
     */
    private String getOwner(Task task) {
        String taskAssignee = task.getAssignee();
        if (FLOWABLE_INITIATOR.equals(taskAssignee)) {
            WorkflowInstance workflowInstance = flowableWorkflowComponent.getWorkflowById(task.getProcessInstanceId());
            return getInitiatorFromWorkflow(workflowInstance);
        } else {
            return taskAssignee;
        }
    }

    /**
     * Get initiator from workflow
     *
     * @param workflowInstance Workflow instance
     * @return Initiator username
     */
    private String getInitiatorFromWorkflow(WorkflowInstance workflowInstance) {
        if (workflowInstance == null) {
            return null;
        }
        if (workflowInstance.getInitiator() != null) {
            return personService.getPerson(workflowInstance.getInitiator()).getUserName();
        } else {
            return null;
        }
    }

    /**
     * Filter out all internal task-properties.
     *
     * @param properties Map<QName, Serializable>
     * @return Filtered properties.
     */
    private Map<QName, Serializable> filterTaskProperties(
            Map<QName, Serializable> properties) {
        if (properties != null) {
            properties.remove(QName.createQName(null, FlowableConstants.PROP_POOLED_ACTORS_HISTORY));
            properties.remove(QName.createQName(null, FlowableConstants.PROP_TASK_FORM_KEY));
        }
        return properties;
    }

    /**
     * Get task properties
     *
     * @param task Task instance
     * @return Map of properties
     */
    public Map<QName, Serializable> getTaskProperties(HistoricTaskInstance task) {
        Task currentTask = flowableTaskService.getTaskById(task.getId());
        if (currentTask != null) {
            return getTaskProperties(currentTask);
        } else {
            return getHistoricTaskProperties(task);
        }
    }

    private Map<QName, Serializable> getHistoricTaskProperties(HistoricTaskInstance task) {
        Map<String, Object> historicTaskVariables = flowableHistoryService.getHistoricTaskVariables(task.getId());

        String formKey = (String) historicTaskVariables.get(FlowableConstants.PROP_TASK_FORM_KEY);
        TypeDefinition taskDef = typeManager.getFullTaskDefinition(formKey);

        Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
        Map<QName, AssociationDefinition> taskAssociations = taskDef.getAssociations();

        Map<QName, Serializable> properties = mapArbitraryProperties(historicTaskVariables, historicTaskVariables,
                taskProperties, taskAssociations);

        properties.put(WorkflowModel.PROP_TASK_ID, task.getId());
        properties.put(WorkflowModel.PROP_DESCRIPTION, task.getDescription());
        properties.put(WorkflowModel.PROP_START_DATE, task.getCreateTime());
        properties.put(WorkflowModel.PROP_DUE_DATE, task.getDueDate());
        properties.put(WorkflowModel.PROP_COMPLETION_DATE, task.getEndTime());
        properties.put(WorkflowModel.PROP_PRIORITY, workflowUtils.convertPriorityBpmnToWorkflowTask(task.getPriority()));
        properties.put(ContentModel.PROP_CREATED, task.getCreateTime());
        properties.put(ContentModel.PROP_OWNER, task.getAssignee());

        // Be sure to fetch the outcome
        String outcomeVarName = factory.mapQNameToName(WorkflowModel.PROP_OUTCOME);
        if (historicTaskVariables.get(outcomeVarName) != null) {
            properties.put(WorkflowModel.PROP_OUTCOME, (Serializable) historicTaskVariables.get(outcomeVarName));
        }

        // History of pooled actors is stored in task variable
        List<NodeRef> pooledActors = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<String> pooledActorRefIds = (List<String>) historicTaskVariables.get(
                FlowableConstants.PROP_POOLED_ACTORS_HISTORY);

        if (pooledActorRefIds != null) {
            for (String nodeId : pooledActorRefIds) {
                pooledActors.add(new NodeRef(nodeId));
            }
        }
        properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, (Serializable) pooledActors);

        return filterTaskProperties(properties);
    }

    /**
     * Map pooled actors
     *
     * @param links      Identity links
     * @param properties Properties map
     */
    private void mapPooledActors(Collection<IdentityLink> links, Map<QName, Serializable> properties) {
        List<NodeRef> pooledActorRefs = getPooledActorsReference(links);
        if (pooledActorRefs != null) {
            properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, (Serializable) pooledActorRefs);
        }
    }

    /**
     * Get pooled actors node references
     *
     * @param links Identity links
     * @return List of node references
     */
    public List<NodeRef> getPooledActorsReference(Collection<IdentityLink> links) {
        List<NodeRef> pooledActorRefs = new ArrayList<>();
        if (links != null) {
            for (IdentityLink link : links) {
                if (IdentityLinkType.CANDIDATE.equals(link.getType())) {
                    String id = link.getGroupId();
                    if (id == null) {
                        id = link.getUserId();
                    }
                    NodeRef pooledNodeRef = authorityManager.mapNameToAuthority(id);
                    if (pooledNodeRef != null) {
                        pooledActorRefs.add(pooledNodeRef);
                    }
                }
            }
        }
        return pooledActorRefs;
    }

    /**
     * Map arbitrary properties
     *
     * @param variables Variable
     * @return Map of properties
     */
    private Map<QName, Serializable> mapArbitraryProperties(Map<String, Object> variables,
                                                            final Map<String, Object> localVariables,
                                                            final Map<QName, PropertyDefinition> taskProperties,
                                                            final Map<QName, AssociationDefinition> taskAssociations) {
        EntryTransformer<String, Object, QName, Serializable> transformer = entry -> {
            String key = entry.getKey();
            QName qname = factory.mapNameToQName(key);
            if (taskProperties.containsKey(qname) ||
                    taskAssociations.containsKey(qname) ||
                    localVariables.containsKey(key)) {
                Serializable value = (Serializable) entry.getValue();
                return new Pair<>(qname, value);
            }
            return null;
        };
        return CollectionUtils.transform(variables, transformer);
    }

    /**
     * Get new task properties
     *
     * @param task       Task instance
     * @param properties Properties
     * @param add        Added properties
     * @param remove     Removed properties
     * @return Map of properties
     */
    public Map<QName, Serializable> getNewTaskProperties(Task task, Map<QName, Serializable> properties,
                                                         Map<QName, List<NodeRef>> add,
                                                         Map<QName, List<NodeRef>> remove) {
        Map<QName, Serializable> newProperties = properties;
        if (add != null || remove != null) {
            if (newProperties == null) {
                newProperties = new HashMap<>(10);
            }

            Map<QName, Serializable> existingProperties = getTaskProperties(task);
            /** Added properties */
            if (add != null) {
                for (Map.Entry<QName, List<NodeRef>> toAdd : add.entrySet()) {

                    Serializable existingAdd = newProperties.get(toAdd.getKey());
                    if (existingAdd == null) {
                        existingAdd = existingProperties.get(toAdd.getKey());
                        newProperties.put(toAdd.getKey(), existingAdd);
                    }

                    if (existingAdd == null) {
                        newProperties.put(toAdd.getKey(), (Serializable) toAdd.getValue());
                    } else {
                        if (existingAdd instanceof List<?>) {
                            List<NodeRef> existingList = (List<NodeRef>) existingAdd;
                            for (NodeRef nodeRef : toAdd.getValue()) {
                                if (!(existingList.contains(nodeRef))) {
                                    existingList.add(nodeRef);
                                }
                            }
                        } else {
                            if (toAdd.getValue().size() > 0) {
                                newProperties.put(toAdd.getKey(), toAdd.getValue().get(0));
                            }
                        }
                    }
                }
            }
            /** Removed properties */
            if (remove != null) {
                for (Map.Entry<QName, List<NodeRef>> toRemove : remove.entrySet()) {
                    Serializable existingRemove = newProperties.get(toRemove.getKey());
                    boolean isAlreadyNewProperty = existingRemove != null;

                    if (existingRemove == null) {
                        existingRemove = existingProperties.get(toRemove.getKey());
                    }

                    if (existingRemove != null) {
                        if (existingRemove instanceof List<?>) {
                            existingRemove = new ArrayList<>((List<NodeRef>) existingRemove);
                            for (NodeRef nodeRef : toRemove.getValue()) {
                                ((List<NodeRef>) existingRemove).remove(nodeRef);
                            }
                            newProperties.put(toRemove.getKey(), existingRemove);
                        } else {
                            if (!isAlreadyNewProperty) {
                                newProperties.put(toRemove.getKey(), null);
                            }
                        }
                    }
                }
            }
        }
        return newProperties;
    }

    /**
     * Set default task properties
     *
     * @param task Delegate task
     */
    public void setDefaultTaskProperties(DelegateTask task) {
        TypeDefinition typeDefinition = typeManager.getFullTaskDefinition(task);
        Map<QName, Serializable> existingValues = getTaskProperties(task, typeDefinition, true);
        Map<QName, Serializable> defaultValues = new HashMap<>();

        Map<QName, PropertyDefinition> propertyDefs = typeDefinition.getProperties();

        for (Map.Entry<QName, PropertyDefinition> entry : propertyDefs.entrySet()) {
            QName key = entry.getKey();
            String defaultValue = entry.getValue().getDefaultValue();
            if (defaultValue != null && existingValues.get(key) == null) {
                defaultValues.put(key, defaultValue);
            }
        }

        Serializable existingValue = existingValues.get(WorkflowModel.PROP_PRIORITY);
        if (existingValue != null) {
            Integer priority = (Integer) existingValue;
            defaultValues.put(WorkflowModel.PROP_PRIORITY, workflowUtils.convertPriorityBpmnToWorkflowTask(priority));
        } else {
            defaultValues.put(WorkflowModel.PROP_PRIORITY, DEFAULT_WORKFLOW_TASK_PRIORITY);
        }

        String description = (String) existingValues.get(WorkflowModel.PROP_DESCRIPTION);
        if (description == null || description.length() == 0) {
            ProcessDefinition processDefinition = flowableProcessDefinitionService.getProcessDefinitionById(
                    task.getProcessDefinitionId());
            if (processDefinition != null && processDefinition.getKey() != null) {
                String processDefinitionKey = processDefinition.getKey();
                description = factory.getTaskDescription(typeDefinition, factory.buildGlobalId(processDefinitionKey),
                        null, task.getTaskDefinitionKey());
                if (description != null && description.length() > 0) {
                    defaultValues.put(WorkflowModel.PROP_DESCRIPTION, description);
                } else {
                    String descriptionKey = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
                    description = (String) task.getVariable(descriptionKey);
                    if (description != null && description.length() > 0) {
                        defaultValues.put(WorkflowModel.PROP_DESCRIPTION, description);
                    } else {
                        defaultValues.put(WorkflowModel.PROP_DESCRIPTION, task.getName());
                    }
                }
            }

        }

        String taskKey = task.getTaskDefinitionKey();
        String taskTitle = null;
        if (StringUtils.isNotBlank(taskKey)) {
            String taskTitleFormat = String.format(TASK_TITLE_KEY_TEMPLATE, taskKey);
            if (StringUtils.isNotBlank(I18NUtil.getMessage(taskTitleFormat))) {
                taskTitle = taskTitleFormat;
            }
        }
        if (StringUtils.isBlank(taskTitle)) {
            taskTitle = task.getName();
        }

        if (StringUtils.isNotBlank(taskTitle)) {
            defaultValues.putIfAbsent(CiteckWorkflowModel.PROP_TASK_TITLE, taskTitle);
        }

        if (defaultValues.size() > 0) {
            setTaskProperties(task, defaultValues);
        }
    }

    /**
     * Set task properties
     *
     * @param task       Delegate task
     * @param properties Properties
     */
    public void setTaskProperties(DelegateTask task, Map<QName, Serializable> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        TypeDefinition type = typeManager.getFullTaskDefinition(task);
        Map<String, Object> variablesToSet = handlerRegistry.handleVariablesToSet(properties, type, task,
                DelegateTask.class);
        if (variablesToSet.size() > 0) {
            task.setVariablesLocal(variablesToSet);
        }
    }

    public void setTaskProperties(Task task, Map<QName, Serializable> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        TypeDefinition type = typeManager.getFullTaskDefinition(task);
        Map<String, Object> variablesToSet = handlerRegistry.handleVariablesToSet(properties, type, task, Task.class);

        taskService.saveTask(task);
        taskService.setVariablesLocal(task.getId(), variablesToSet);
        setTaskOwner(task, properties);
    }

    /**
     * Get start task properties
     *
     * @param historicProcessInstance History process instance
     * @param taskDef                 Task definition type
     * @param completed               Is completed
     * @return Map of properties
     */
    public Map<QName, Serializable> getStartTaskProperties(HistoricProcessInstance historicProcessInstance,
                                                           TypeDefinition taskDef, boolean completed) {
        Map<QName, PropertyDefinition> taskProperties = taskDef != null ? taskDef.getProperties() : new HashMap<>();
        Map<QName, AssociationDefinition> taskAssociations = taskDef != null ? taskDef.getAssociations() : new HashMap<>();

        Map<String, Object> variables = getStartVariables(historicProcessInstance);
        Map<QName, Serializable> properties = mapArbitraryProperties(variables, variables, taskProperties,
                taskAssociations);

        properties.put(WorkflowModel.PROP_TASK_ID, ActivitiConstants.START_TASK_PREFIX + historicProcessInstance.getId());
        properties.put(WorkflowModel.PROP_START_DATE, historicProcessInstance.getStartTime());

        String wfDueDateKey = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DUE_DATE);
        String dueDateKey = factory.mapQNameToName(WorkflowModel.PROP_DUE_DATE);
        Serializable dueDate = (Serializable) variables.get(wfDueDateKey);
        if (dueDate == null) {
            dueDate = (Serializable) variables.get(dueDateKey);
        }

        properties.put(WorkflowModel.PROP_DUE_DATE, dueDate);
        properties.put(WorkflowModel.PROP_COMPLETION_DATE, historicProcessInstance.getStartTime());
        String priorityKey = factory.mapQNameToName(WorkflowModel.PROP_PRIORITY);
        Serializable priority = (Serializable) variables.get(priorityKey);
        if (priority == null) {
            String wfPriorityKey = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_PRIORITY);
            priority = (Serializable) variables.get(wfPriorityKey);
        }
        properties.put(WorkflowModel.PROP_PRIORITY, priority);
        properties.put(ContentModel.PROP_CREATED, historicProcessInstance.getStartTime());

        NodeRef ownerNode = (NodeRef) variables.get(WorkflowConstants.PROP_INITIATOR);
        if (ownerNode != null && nodeService.exists(ownerNode)) {
            properties.put(ContentModel.PROP_OWNER, personService.getPerson(ownerNode).getUserName());
        }

        if (completed) {
            properties.put(WorkflowModel.PROP_STATUS, WorkflowConstants.TASK_STATUS_COMPLETED);
            properties.put(WorkflowModel.PROP_OUTCOME, ActivitiConstants.DEFAULT_TRANSITION_NAME);
        }

        return filterTaskProperties(properties);
    }

    /**
     * Get start variables
     *
     * @param historicProcessInstance Historic process instance
     * @return Map of variables
     */
    public Map<String, Object> getStartVariables(HistoricProcessInstance historicProcessInstance) {
        if (historicProcessInstance.getStartActivityId() == null) {
            return Collections.emptyMap();
        }
        HistoricActivityInstance startEvent = flowableHistoryService.getHistoryActivityInstance(
                historicProcessInstance.getId(), historicProcessInstance.getStartActivityId());
        return flowableHistoryService.getVariablesByActivityId(startEvent.getId());
    }

    /**
     * Set task owner
     *
     * @param task       Task Task instance
     * @param properties Map<QName, Serializable> Map of parameters
     */
    public void setTaskOwner(Task task, Map<QName, Serializable> properties) {
        QName ownerKey = ContentModel.PROP_OWNER;
        if (properties.containsKey(ownerKey)) {
            Serializable owner = properties.get(ownerKey);
            if (owner != null && !(owner instanceof String)) {
                throw new WorkflowException("Task property " + ownerKey + " has to be not null and be instance of " +
                        "java.lang.String");
            }
            String assignee = (String) owner;
            String currentAssignee = task.getAssignee();
            if (!ObjectUtils.equals(currentAssignee, assignee)) {
                taskService.setAssignee(task.getId(), assignee);
            }
        }
    }

    /**
     * Set task service
     *
     * @param taskService Task service
     */
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Set authority manager
     *
     * @param authorityManager Authority manager
     */
    public void setAuthorityManager(WorkflowAuthorityManager authorityManager) {
        this.authorityManager = authorityManager;
    }

    /**
     * Set namespace prefix resolver
     *
     * @param namespaceService Namespace prefix resolver
     */
    public void setNamespaceService(NamespacePrefixResolver namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * Dictionary service
     *
     * @param dictionaryService Dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set tenant service
     *
     * @param tenantService Tenant service
     */
    public void setTenantService(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * Set message service
     *
     * @param messageService Message service
     */
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Set node service
     *
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set flowable task service
     *
     * @param flowableTaskService Flowable task service
     */
    public void setFlowableTaskService(FlowableTaskService flowableTaskService) {
        this.flowableTaskService = flowableTaskService;
    }

    /**
     * Set flowable process definition service
     *
     * @param flowableProcessDefinitionService Flowable process definition service
     */
    public void setFlowableProcessDefinitionService(FlowableProcessDefinitionService flowableProcessDefinitionService) {
        this.flowableProcessDefinitionService = flowableProcessDefinitionService;
    }

    /**
     * Set flowable history service
     *
     * @param flowableHistoryService Flowable history service
     */
    public void setFlowableHistoryService(FlowableHistoryService flowableHistoryService) {
        this.flowableHistoryService = flowableHistoryService;
    }

    /**
     * Set flowable workflow component
     *
     * @param flowableWorkflowComponent Flowable workflow component
     */
    public void setFlowableWorkflowComponent(FlowableWorkflowComponent flowableWorkflowComponent) {
        this.flowableWorkflowComponent = flowableWorkflowComponent;
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
     * Set handler registry
     *
     * @param handlerRegistry Handler registry
     */
    public void setHandlerRegistry(FlowableWorkflowPropertyHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    /**
     * Set type manager
     *
     * @param typeManager Type manager
     */
    public void setTypeManager(FlowableTaskTypeManager typeManager) {
        this.typeManager = typeManager;
    }

    /**
     * Get workflow object factory
     *
     * @return Workflow object factory
     */
    public WorkflowObjectFactory getFactory() {
        return factory;
    }

    @Autowired
    public void setWorkflowUtils(WorkflowUtils workflowUtils) {
        this.workflowUtils = workflowUtils;
    }
}
