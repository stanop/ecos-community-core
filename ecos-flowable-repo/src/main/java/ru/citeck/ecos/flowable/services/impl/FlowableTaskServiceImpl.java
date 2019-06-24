package ru.citeck.ecos.flowable.services.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.TaskService;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.api.IdentityLinkType;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.services.FlowableTaskService;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.utils.WorkflowUtils;
import ru.citeck.ecos.workflow.tasks.EcosTaskService;
import ru.citeck.ecos.workflow.tasks.EngineTaskService;
import ru.citeck.ecos.workflow.tasks.TaskInfo;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Flowable task service
 */
public class FlowableTaskServiceImpl implements FlowableTaskService, EngineTaskService {

    private static final Log logger = LogFactory.getLog(FlowableTaskService.class);

    private static final String OUTCOME_FIELD = "outcome";

    private TaskService taskService;

    @Autowired
    private EcosTaskService ecosTaskService;
    @Autowired
    private WorkflowUtils workflowUtils;
    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;

    @PostConstruct
    public void init() {
        ecosTaskService.register(FlowableConstants.ENGINE_ID, this);
    }

    /**
     * Set task service
     * @param taskService Task service
     */
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Get task by task id
     * @param taskId Task id
     * @return Task
     */
    @Override
    public Task getTaskById(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    /**
     * Get tasks by process instance id
     * @param processInstanceId Process instance id
     * @return List of tasks
     */
    @Override
    public List<Task> getTasksByProcessInstanceId(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }

    /**
     * Get tasks by process definition id
     * @param processDefinitionId Process definition id
     * @return List of tasks
     */
    @Override
    public List<Task> getTasksByProcessDefinitionId(String processDefinitionId) {
        return taskService.createTaskQuery().processDefinitionId(processDefinitionId).list();
    }

    public Map<String, Object> getVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    public Map<String, Object> getVariablesLocal(String taskId) {
        return taskService.getVariablesLocal(taskId);
    }


    public Object getVariable(String taskId, String variableName) {
        return taskService.getVariable(taskId, variableName);
    }

    public String getFormKey(String taskId) {

        Task task = getTaskById(taskId);

        if (task != null) {
            return task.getFormKey();
        } else {
            logger.warn("Task with id " + taskId + " not found!");
        }

        return null;
    }

    public String getAssignee(String taskId) {

        List<IdentityLink> links = taskService.getIdentityLinksForTask(taskId);

        for (IdentityLink link : links) {
            if (IdentityLinkType.ASSIGNEE.equals(link.getType())) {
                return link.getUserId();
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

        Object bpmPackage = getVariable(taskId, "bpm_package");
        NodeRef documentRef = workflowUtils.getTaskDocumentFromPackage(bpmPackage);

        return documentRef != null ? RecordRef.valueOf(documentRef.toString()) : RecordRef.EMPTY;
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
            WorkflowTask task = workflowService.getTaskById("flowable$" + getId());
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


