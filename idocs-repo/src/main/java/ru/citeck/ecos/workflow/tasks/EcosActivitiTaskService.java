package ru.citeck.ecos.workflow.tasks;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.utils.WorkflowUtils;

import java.util.List;
import java.util.Map;

@Component
public class EcosActivitiTaskService implements EngineTaskService {

    private static final Log logger = LogFactory.getLog(EcosActivitiTaskService.class);

    private static final String DEFAULT_OUTCOME_FIELD = "bpm_outcome";
    private static final String OUTCOME_FIELD = "outcome";

    @Autowired
    private TaskService taskService;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private WorkflowUtils workflowUtils;
    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;

    @Autowired
    public EcosActivitiTaskService(EcosTaskService ecosTaskService) {
        ecosTaskService.register(ActivitiConstants.ENGINE_ID, this);
    }

    private Map<String, Object> getVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    private Map<String, Object> getVariablesLocal(String taskId) {
        return taskService.getVariablesLocal(taskId);
    }

    private Object getVariable(String taskId, String variableName) {
        return taskService.getVariable(taskId, variableName);
    }

    private String getFormKey(String taskId) {
        String key = getRawFormKey(taskId);
        return key != null ? "alf_" + key : null;
    }

    private String getRawFormKey(String taskId) {

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
            return task.getFormKey();
        } else {
            logger.warn("Task with id " + taskId + " not found!");
        }
        return null;
    }

    @Override
    public void endTask(String taskId, String transition, Map<String, Object> variables) {

        String outcomeProp = getOutcomeProperty(taskId);
        if (StringUtils.isBlank(outcomeProp)) {
            outcomeProp = DEFAULT_OUTCOME_FIELD;
        }

        Object comment = variables.get("comment");
        if (comment != null) {
            variables.put("bpm_comment", comment);
        }

        variables.put(outcomeProp, transition);
        variables.put(OUTCOME_FIELD, transition);

        taskService.complete(taskId, variables, true);
    }

    private String getAssignee(String taskId) {

        List<IdentityLink> links = taskService.getIdentityLinksForTask(taskId);

        for (IdentityLink link : links) {
            if (IdentityLinkType.ASSIGNEE.equals(link.getType())) {
                return link.getUserId();
            }
        }

        return null;
    }

    private RecordRef getDocument(String taskId) {

        Object bpmPackage = getVariable(taskId, "bpm_package");
        if (bpmPackage instanceof ActivitiScriptNode) {
            bpmPackage = ((ActivitiScriptNode) bpmPackage).getNodeRef();
        }
        NodeRef documentRef = workflowUtils.getTaskDocumentFromPackage(bpmPackage);

        return documentRef != null ? RecordRef.valueOf(documentRef.toString()) : RecordRef.EMPTY;
    }

    @Override
    public TaskInfo getTaskInfo(String taskId) {
        return new ActivitiTaskInfo(taskId);
    }

    private String getOutcomeProperty(String taskId) {

        String rawFormKey = getRawFormKey(taskId);

        if (rawFormKey == null) {
            return null;
        }
        QName formKeyQName = QName.resolveToQName(namespaceService, rawFormKey);
        if (formKeyQName == null) {
            return null;
        }

        PropertyDefinition prop = dictionaryService.getProperty(formKeyQName, WorkflowModel.PROP_OUTCOME_PROPERTY_NAME);
        String value = prop != null ? prop.getDefaultValue() : null;

        if (value == null) {
            return null;
        }

        QName propQName = QName.resolveToQName(namespaceService, value);
        if (propQName == null) {
            return null;
        }

        return propQName.toPrefixString(namespaceService).replaceAll(":", "_");
    }

    private class ActivitiTaskInfo implements TaskInfo {

        private final String id;

        ActivitiTaskInfo(String id) {
            this.id = id;
        }

        @Override
        public String getTitle() {
            WorkflowTask task = workflowService.getTaskById("activiti$" + getId());
            return workflowUtils.getTaskTitle(task);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getAssignee() {
            return EcosActivitiTaskService.this.getAssignee(getId());
        }

        @Override
        public String getFormKey() {
            return EcosActivitiTaskService.this.getFormKey(getId());
        }

        @Override
        public Map<String, Object> getAttributes() {
            return EcosActivitiTaskService.this.getVariables(getId());
        }

        @Override
        public Map<String, Object> getLocalAttributes() {
            return EcosActivitiTaskService.this.getVariablesLocal(getId());
        }

        @Override
        public RecordRef getDocument() {
            return EcosActivitiTaskService.this.getDocument(getId());
        }

        @Override
        public Object getAttribute(String name) {
            return EcosActivitiTaskService.this.getVariable(getId(), name);
        }
    }
}
