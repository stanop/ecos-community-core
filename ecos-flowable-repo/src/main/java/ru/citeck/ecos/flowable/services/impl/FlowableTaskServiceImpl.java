package ru.citeck.ecos.flowable.services.impl;

import lombok.extern.log4j.Log4j;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.flowable.engine.TaskService;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.api.IdentityLinkType;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.services.FlowableHistoryService;
import ru.citeck.ecos.flowable.services.FlowableTaskService;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.utils.RepoUtils;
import ru.citeck.ecos.utils.WorkflowUtils;
import ru.citeck.ecos.workflow.mirror.WorkflowMirrorService;
import ru.citeck.ecos.workflow.tasks.EcosTaskService;
import ru.citeck.ecos.workflow.tasks.EngineTaskService;
import ru.citeck.ecos.workflow.tasks.TaskInfo;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flowable task service
 */
@Log4j
public class FlowableTaskServiceImpl implements FlowableTaskService, EngineTaskService {

    private static final String VAR_PACKAGE = "bpm_package";
    private static final String OUTCOME_FIELD = "outcome";

    private TaskService taskService;

    @Autowired
    private EcosTaskService ecosTaskService;
    @Autowired
    private WorkflowUtils workflowUtils;
    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;
    @Autowired
    private FlowableHistoryService flowableHistoryService;
    @Autowired
    private WorkflowMirrorService workflowMirrorService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private NamespaceService namespaceService;

    @PostConstruct
    public void init() {
        ecosTaskService.register(FlowableConstants.ENGINE_ID, this);
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
     * Get task by task id
     *
     * @param taskId Task id
     * @return Task
     */
    @Override
    public Task getTaskById(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    /**
     * Get tasks by process instance id
     *
     * @param processInstanceId Process instance id
     * @return List of tasks
     */
    @Override
    public List<Task> getTasksByProcessInstanceId(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }

    /**
     * Get tasks by process definition id
     *
     * @param processDefinitionId Process definition id
     * @return List of tasks
     */
    @Override
    public List<Task> getTasksByProcessDefinitionId(String processDefinitionId) {
        return taskService.createTaskQuery().processDefinitionId(processDefinitionId).list();
    }

    public Map<String, Object> getVariables(String taskId) {
        WorkflowTask task = workflowService.getTaskById(FlowableConstants.ENGINE_PREFIX + taskId);

        Map<String, Object> propsFromWorkflowService = new HashMap<>();

        task.getProperties().forEach((qName, serializable) -> {
            String newKey = qName.toPrefixString(namespaceService).replaceAll(":", "_");
            propsFromWorkflowService.put(newKey, serializable);
        });

        Map<String, Object> propsFromFlowable;

        if (taskExists(taskId)) {
            propsFromFlowable = taskService.getVariables(taskId);
        } else {
            propsFromFlowable = flowableHistoryService.getHistoricTaskVariables(taskId);
        }

        propsFromFlowable.putAll(propsFromWorkflowService);

        return propsFromFlowable;
    }

    public Map<String, Object> getVariablesLocal(String taskId) {
        if (taskExists(taskId)) {
            return taskService.getVariablesLocal(taskId);
        } else {
            return flowableHistoryService.getHistoricTaskVariables(taskId);
        }
    }


    public Object getVariable(String taskId, String variableName) {
        Object result = getVariables(taskId).get(variableName);

        if (result == null && VAR_PACKAGE.equals(variableName)) {
            return getPackageFromMirrorTask(taskId);
        }

        return result;
    }

    private NodeRef getPackageFromMirrorTask(String taskId) {
        NodeRef taskMirror = workflowMirrorService.getTaskMirror(FlowableConstants.ENGINE_PREFIX + taskId);
        return RepoUtils.getFirstTargetAssoc(taskMirror, WorkflowModel.ASSOC_PACKAGE, nodeService);
    }

    public String getFormKey(String taskId) {
        String key = getRawFormKey(taskId);
        return key != null ? "alf_" + key : null;
    }

    private String getRawFormKey(String taskId) {
        String key = null;

        if (taskExists(taskId)) {
            key = taskService.createTaskQuery().taskId(taskId).singleResult().getFormKey();
        } else {
            Object keyObj = flowableHistoryService.getHistoricTaskVariables(taskId).get("taskFormKey");
            if (keyObj != null) {
                key = (String) keyObj;
            }
        }

        if (key == null) {
            log.warn(String.format("Could not get formKey for task <%s>, because task does not exists", taskId));
        }

        return key;
    }

    public String getCandidate(String taskId) {
        return getIdentityLinkAuthority(IdentityLinkType.CANDIDATE, taskId);
    }

    public String getAssignee(String taskId) {
        return getIdentityLinkAuthority(IdentityLinkType.ASSIGNEE, taskId);
    }

    private String getIdentityLinkAuthority(String type, String taskId) {
        if (!taskExists(taskId)) {
            return null;
        }

        List<IdentityLink> links = taskService.getIdentityLinksForTask(taskId);

        for (IdentityLink link : links) {
            if (type.equals(link.getType())) {
                return link.getUserId() != null ? link.getUserId() : link.getGroupId();
            }
        }

        return null;
    }

    @Override
    public void endTask(String taskId, String transition, Map<String, Object> variables) {

        String formKey = getFormKey(taskId);
        String formOutcomeField = "form_" + formKey + "_outcome";

        variables.put(formOutcomeField, transition);
        variables.put(OUTCOME_FIELD, transition);

        taskService.complete(taskId, variables, Collections.emptyMap());
    }

    public RecordRef getDocument(String taskId) {

        Object bpmPackage = getVariable(taskId, VAR_PACKAGE);
        NodeRef documentRef = workflowUtils.getTaskDocumentFromPackage(bpmPackage);

        return documentRef != null ? RecordRef.valueOf(documentRef.toString()) : RecordRef.EMPTY;
    }

    private boolean taskExists(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        return task != null;
    }

    @Override
    public TaskInfo getTaskInfo(String taskId) {
        return new FlowableTaskInfo(taskId);
    }

    private class FlowableTaskInfo implements TaskInfo {

        private final String id;

        FlowableTaskInfo(String id) {
            this.id = id;
        }

        @Override
        public String getTitle() {
            WorkflowTask task = workflowService.getTaskById(FlowableConstants.ENGINE_PREFIX + getId());
            return workflowUtils.getTaskTitle(task);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getAssignee() {
            return FlowableTaskServiceImpl.this.getAssignee(getId());
        }

        @Override
        public String getCandidate() {
            return FlowableTaskServiceImpl.this.getCandidate(getId());
        }

        @Override
        public String getFormKey() {
            return FlowableTaskServiceImpl.this.getFormKey(getId());
        }

        @Override
        public Map<String, Object> getAttributes() {
            return FlowableTaskServiceImpl.this.getVariables(getId());
        }

        @Override
        public Map<String, Object> getLocalAttributes() {
            return FlowableTaskServiceImpl.this.getVariablesLocal(getId());
        }

        @Override
        public RecordRef getDocument() {
            return FlowableTaskServiceImpl.this.getDocument(getId());
        }

        @Override
        public Object getAttribute(String name) {
            return FlowableTaskServiceImpl.this.getVariable(getId(), name);
        }
    }
}


