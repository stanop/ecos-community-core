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
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.EntryTransformer;
import org.apache.commons.lang.ObjectUtils;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.service.IdentityLinkType;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.Task;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.services.FlowableHistoryService;
import ru.citeck.ecos.flowable.services.FlowableProcessDefinitionService;
import ru.citeck.ecos.flowable.services.FlowableTaskService;
import ru.citeck.ecos.flowable.services.FlowableTaskTypeManager;
import ru.citeck.ecos.flowable.utils.FlowableWorkflowPropertyHandlerRegistry;

import java.io.Serializable;
import java.util.*;

/**
 * Flowable property converter
 */
public class FlowablePropertyConverter {

    /**
     * Constants
     */
    private static final String FLOWABLE_ENGINE_NAME = "flowable";

    /**
     * Task service
     */
    private TaskService taskService;

    /**
     * Workflow object factory
     */
    private WorkflowObjectFactory factory;

    /**
     * Workflow authority manager
     */
    private WorkflowAuthorityManager authorityManager;

    /**
     * Tenant service
     */
    private TenantService tenantService;

    /**
     * Message service
     */
    private MessageService messageService;

    /**
     * Dictionary service
     */
    private DictionaryService dictionaryService;

    /**
     * Namespace prefix resolver
     */
    private NamespacePrefixResolver namespaceService;

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Flowable task service
     */
    private FlowableTaskService flowableTaskService;

    /**
     * Flowable history service
     */
    private FlowableHistoryService flowableHistoryService;

    /**
     * Flowable process definition service
     */
    private FlowableProcessDefinitionService flowableProcessDefinitionService;

    /**
     * Type manager
     */
    private FlowableTaskTypeManager typeManager;

    /**
     * Handler registry
     */
    private FlowableWorkflowPropertyHandlerRegistry handlerRegistry;

    /**
     * Person service
     */
    private PersonService personService;

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
     * @param task Task instance
     * @return Map of properties
     */
    public Map<QName, Serializable> getTaskProperties(Task task) {

        Map<String, Object> variables = taskService.getVariables(task.getId());
        Map<String, Object> localVariables = taskService.getVariablesLocal(task.getId());

        TypeDefinition taskDef = typeManager.getFullTaskDefinition(task);
        Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
        Map<QName, AssociationDefinition> taskAssociations = taskDef.getAssociations();

        Map<QName, Serializable> properties = mapArbitraryProperties(variables, localVariables, taskProperties, taskAssociations);

        /** Set task instance properties */
        properties.put(WorkflowModel.PROP_TASK_ID, task.getId());
        properties.put(WorkflowModel.PROP_START_DATE, task.getCreateTime());
        properties.put(WorkflowModel.PROP_COMPLETION_DATE, null);
        properties.put(WorkflowModel.PROP_PRIORITY, task.getPriority());
        properties.put(ContentModel.PROP_CREATED, task.getCreateTime());
        properties.put(ContentModel.PROP_OWNER, task.getAssignee());
        if (!properties.containsKey(WorkflowModel.PROP_DESCRIPTION)) {
            properties.put(WorkflowModel.PROP_DESCRIPTION, task.getDescription());
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
        Map<String, Object> variables = null;

        if (!localOnly) {
            variables = new HashMap<String, Object>();
            variables.putAll(localVariables);

            Map<String, Object> executionVariables = task.getVariables();

            for (Map.Entry<String, Object> entry : executionVariables.entrySet()) {
                String key = entry.getKey();
                if (localVariables.containsKey(key) == false) {
                    variables.put(key, entry.getValue());
                }
            }
        } else {
            variables = localVariables;
        }

        Map<QName, Serializable> properties = mapArbitraryProperties(variables, localVariables, taskProperties, taskAssociations);
        properties.put(WorkflowModel.PROP_TASK_ID, task.getId());
        properties.put(WorkflowModel.PROP_START_DATE, task.getCreateTime());
        properties.put(WorkflowModel.PROP_COMPLETION_DATE, null);
        properties.put(WorkflowModel.PROP_PRIORITY, task.getPriority());
        properties.put(ContentModel.PROP_CREATED, task.getCreateTime());
        properties.put(ContentModel.PROP_OWNER, task.getAssignee());
        if (!properties.containsKey(WorkflowModel.PROP_DUE_DATE)) {
            properties.put(WorkflowModel.PROP_DUE_DATE, task.getDueDate());
        }
        Set<IdentityLink> links = ((TaskEntity) task).getCandidates();
        mapPooledActors(links, properties);

        return filterTaskProperties(properties);
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
            return Collections.EMPTY_MAP;
        }
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
        List<NodeRef> pooledActorRefs = new ArrayList<NodeRef>();
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
        EntryTransformer<String, Object, QName, Serializable> transformer = new EntryTransformer<String, Object, QName, Serializable>() {
            @Override
            public Pair<QName, Serializable> apply(Map.Entry<String, Object> entry) {
                String key = entry.getKey();
                QName qname = factory.mapNameToQName(key);
                if (taskProperties.containsKey(qname) ||
                        taskAssociations.containsKey(qname) ||
                        localVariables.containsKey(key)) {
                    Serializable value = (Serializable) entry.getValue();
                    return new Pair<QName, Serializable>(qname, value);
                }
                return null;
            }
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
                newProperties = new HashMap<QName, Serializable>(10);
            }

            Map<QName, Serializable> existingProperties = getTaskProperties(task);
            /** Added properties */
            if (add != null) {
                for (Map.Entry<QName, List<NodeRef>> toAdd : add.entrySet()) {

                    Serializable existingAdd = newProperties.get(toAdd.getKey());
                    if (existingAdd == null) {
                        existingAdd = existingProperties.get(toAdd.getKey());
                        newProperties.put(toAdd.getKey(), (Serializable) existingAdd);
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
                                newProperties.put(toAdd.getKey(), (Serializable) toAdd.getValue().get(0));
                            }
                        }
                    }
                }
            }
            /** Removed properties */
            if (remove != null) {
                for (Map.Entry<QName, List<NodeRef>> toRemove : remove.entrySet()) {
                    Serializable existingRemove = (Serializable) newProperties.get(toRemove.getKey());
                    boolean isAlreadyNewProperty = existingRemove != null;

                    if (existingRemove == null) {
                        existingRemove = (Serializable) existingProperties.get(toRemove.getKey());
                    }

                    if (existingRemove != null) {
                        if (existingRemove instanceof List<?>) {
                            existingRemove = new ArrayList<NodeRef>((List<NodeRef>) existingRemove);
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
        Map<QName, Serializable> defaultValues = new HashMap<QName, Serializable>();

        Map<QName, PropertyDefinition> propertyDefs = typeDefinition.getProperties();

        for (Map.Entry<QName, PropertyDefinition> entry : propertyDefs.entrySet()) {
            QName key = entry.getKey();
            String defaultValue = entry.getValue().getDefaultValue();
            if (defaultValue != null && existingValues.get(key) == null) {
                defaultValues.put(key, defaultValue);
            }
        }

        PropertyDefinition priorDef = propertyDefs.get(WorkflowModel.PROP_PRIORITY);
        Serializable existingValue = existingValues.get(WorkflowModel.PROP_PRIORITY);
        try {
            if (priorDef != null) {
                for (ConstraintDefinition constraintDef : priorDef.getConstraints()) {
                    constraintDef.getConstraint().evaluate(existingValue);
                }
            }
        } catch (ConstraintException ce) {
            if (priorDef != null) {
                Integer defaultVal = Integer.valueOf(priorDef.getDefaultValue());
                defaultValues.put(WorkflowModel.PROP_PRIORITY, defaultVal);
            }
        }

        String description = (String) existingValues.get(WorkflowModel.PROP_DESCRIPTION);
        if (description == null || description.length() == 0) {
            ProcessDefinition processDefinition = flowableProcessDefinitionService.getProcessDefinitionById(task.getProcessDefinitionId());
            if (processDefinition != null && processDefinition.getKey() != null) {
                String processDefinitionKey = processDefinition.getKey();
                description = factory.getTaskDescription(typeDefinition, factory.buildGlobalId(processDefinitionKey), null, task.getTaskDefinitionKey());
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
        Map<String, Object> variablesToSet = handlerRegistry.handleVariablesToSet(properties, type, task, DelegateTask.class);
        if (variablesToSet.size() > 0) {
            task.setVariablesLocal(variablesToSet);
        }
    }

    /**
     * Get start task properties
     *
     * @param historicProcessInstance History process instance
     * @param taskDef                 Task definition type
     * @param completed               Is completed
     * @return Map of properties
     */
    public Map<QName, Serializable> getStartTaskProperties(HistoricProcessInstance historicProcessInstance, TypeDefinition taskDef, boolean completed) {
        Map<QName, PropertyDefinition> taskProperties = taskDef != null ? taskDef.getProperties() : new HashMap<>();
        Map<QName, AssociationDefinition> taskAssociations = taskDef != null ? taskDef.getAssociations() : new HashMap<>();

        Map<String, Object> variables = getStartVariables(historicProcessInstance);
        Map<QName, Serializable> properties = mapArbitraryProperties(variables, variables, taskProperties, taskAssociations);

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
     * @param task Task Task instance
     * @param properties Map<QName, Serializable> Map of parameters
     */
    public void setTaskOwner(Task task, Map<QName, Serializable> properties) {
        QName ownerKey = ContentModel.PROP_OWNER;
        if (properties.containsKey(ownerKey)) {
            Serializable owner = properties.get(ownerKey);
            if (owner != null && !(owner instanceof String)) {
                throw new WorkflowException("Task property " + ownerKey + " has to be not null and be instance of java.lang.String");
            }
            String assignee = (String) owner;
            String currentAssignee = task.getAssignee();
            if (ObjectUtils.equals(currentAssignee, assignee) == false) {
                taskService.setAssignee(task.getId(), assignee);
            }
        }
    }

    /**
     * Set task service
     * @param taskService Task service
     */
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Set authority manager
     * @param authorityManager Authority manager
     */
    public void setAuthorityManager(WorkflowAuthorityManager authorityManager) {
        this.authorityManager = authorityManager;
    }

    /**
     * Set namespace prefix resolver
     * @param namespaceService Namespace prefix resolver
     */
    public void setNamespaceService(NamespacePrefixResolver namespaceService) {
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
     * Set tenant service
     * @param tenantService Tenant service
     */
    public void setTenantService(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * Set message service
     * @param messageService Message service
     */
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Set node service
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set flowable task service
     * @param flowableTaskService Flowable task service
     */
    public void setFlowableTaskService(FlowableTaskService flowableTaskService) {
        this.flowableTaskService = flowableTaskService;
    }

    /**
     * Set flowable process definition service
     * @param flowableProcessDefinitionService Flowable process definition service
     */
    public void setFlowableProcessDefinitionService(FlowableProcessDefinitionService flowableProcessDefinitionService) {
        this.flowableProcessDefinitionService = flowableProcessDefinitionService;
    }

    /**
     * Set flowable history service
     * @param flowableHistoryService Flowable history service
     */
    public void setFlowableHistoryService(FlowableHistoryService flowableHistoryService) {
        this.flowableHistoryService = flowableHistoryService;
    }

    /**
     * Set person service
     * @param personService Person service
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Set handler registry
     * @param handlerRegistry Handler registry
     */
    public void setHandlerRegistry(FlowableWorkflowPropertyHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    /**
     * Set type manager
     * @param typeManager Type manager
     */
    public void setTypeManager(FlowableTaskTypeManager typeManager) {
        this.typeManager = typeManager;
    }

    /**
     * Get workflow object factory
     * @return Workflow object factory
     */
    public WorkflowObjectFactory getFactory() {
        return factory;
    }
}
