package ru.citeck.ecos.utils;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.model.CiteckWorkflowModel;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Workflow service utils
 */
@Component
public class WorkflowUtils {

    private static final String ID_SEPERATOR_REGEX = "\\$";
    private static final Locale[] locales = {new Locale("ru"), new Locale("en")};

    private WorkflowService workflowService;
    private AuthorityUtils authorityUtils;

    private WorkflowAdminService workflowAdminService;

    /**
     * Get workflow definition by global name
     *
     * @param workflowName Workflow name
     * @return Workflow definition
     */
    public WorkflowDefinition getWorkflowDefinition(String workflowName) {
        if (workflowName == null) {
            return null;
        }
        String parts[] = workflowName.split(ID_SEPERATOR_REGEX);
        if (parts.length != 2) {
            return null;
        }
        if (!workflowAdminService.isEngineEnabled(parts[0])) {
            return null;
        } else {
            return workflowService.getDefinitionByName(workflowName);
        }
    }

    public List<WorkflowTask> getDocumentUserTasks(NodeRef nodeRef, boolean active) {
        return getDocumentUserTasks(nodeRef, active, null);
    }

    /**
     * Get current user document tasks
     */
    public List<WorkflowTask> getDocumentUserTasks(NodeRef nodeRef, boolean active, String engine) {

        List<WorkflowTask> tasks = new LinkedList<>();

        List<WorkflowInstance> workflows = workflowService.getWorkflowsForContent(nodeRef, active);

        if (StringUtils.isNotBlank(engine)) {
            String enginePrefix = engine + "$";
            workflows = workflows.stream()
                                 .filter(workflow -> workflow.getId()
                                 .startsWith(enginePrefix))
                                 .collect(Collectors.toList());
        }

        if (workflows.size() > 0) {

            String userName = AuthenticationUtil.getFullyAuthenticatedUser();
            Set<NodeRef> authorities = authorityUtils.getUserAuthoritiesRefs();

            for (WorkflowInstance workflow : workflows) {
                for (WorkflowTask task : getWorkflowTasks(workflow, active)) {
                    if (isTaskActor(task, userName, authorities)) {
                        tasks.add(task);
                    }
                }
            }
        }

        return tasks;
    }

    private boolean isTaskActor(WorkflowTask task, String userName, Set<NodeRef> authorities) {

        boolean matches;

        Map<QName, Serializable> properties = task.getProperties();
        String actor = (String) properties.get(ContentModel.PROP_OWNER);
        List<?> pooledActors = (List<?>) properties.get(WorkflowModel.ASSOC_POOLED_ACTORS);

        if (actor != null) {
            matches = actor.equals(userName);
        } else {
            matches = pooledActors != null && CollectionUtils.intersection(pooledActors, authorities).size() > 0;
        }
        return matches;
    }

    private List<WorkflowTask> getWorkflowTasks(WorkflowInstance workflow, boolean active) {
        WorkflowTaskQuery query = new WorkflowTaskQuery();
        if (active) {
            query.setActive(true);
            query.setTaskState(WorkflowTaskState.IN_PROGRESS);
        }
        query.setOrderBy(new WorkflowTaskQuery.OrderBy[]{WorkflowTaskQuery.OrderBy.TaskDue_Asc});
        query.setProcessId(workflow.getId());
        return workflowService.queryTasks(query, true);
    }

    public String getTaskTitle(WorkflowTask task) {

        String taskTitle = (String) task.getProperties().get(CiteckWorkflowModel.PROP_TASK_TITLE);
        if (StringUtils.isNotBlank(taskTitle)) {
            String taskTitleMessage = I18NUtil.getMessage(taskTitle);
            if (StringUtils.isNotBlank(taskTitleMessage)) {
                taskTitle = taskTitleMessage;
            }
        } else {
            taskTitle = task.getTitle();
        }
        return taskTitle;
    }

    public MLText getTaskMLTitle(WorkflowTask task) {

        MLText result = new MLText();

        String taskTitle = (String) task.getProperties().get(CiteckWorkflowModel.PROP_TASK_TITLE);

        if (StringUtils.isNotBlank(taskTitle)) {

            for (Locale locale : locales) {
                String taskTitleMessage = I18NUtil.getMessage(taskTitle, locale);
                if (StringUtils.isNotBlank(taskTitleMessage)) {
                    result.put(locale, taskTitleMessage);
                }
            }

            if (result.isEmpty()) {
                result.put(Locale.ENGLISH, taskTitle);
            }
        } else {
            result.put(Locale.ENGLISH, task.getTitle());
        }

        return result;
    }

    /**
     * Set workflow service
     *
     * @param workflowService Workflow service
     */
    @Autowired
    @Qualifier("WorkflowService")
    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * Set workflow admin service
     *
     * @param workflowAdminService Workflow admin service
     */
    public void setWorkflowAdminService(WorkflowAdminService workflowAdminService) {
        this.workflowAdminService = workflowAdminService;
    }

    @Autowired
    public void setAuthorityUtils(AuthorityUtils authorityUtils) {
        this.authorityUtils = authorityUtils;
    }
}
