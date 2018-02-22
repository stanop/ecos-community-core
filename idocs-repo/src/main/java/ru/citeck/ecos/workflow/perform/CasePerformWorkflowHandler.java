package ru.citeck.ecos.workflow.perform;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.model.CasePerformModel;
import ru.citeck.ecos.model.CiteckWorkflowModel;
import ru.citeck.ecos.workflow.variable.type.NodeRefsList;
import ru.citeck.ecos.workflow.variable.type.TaskConfig;
import ru.citeck.ecos.workflow.variable.type.TaskConfigs;

import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CasePerformWorkflowHandler implements Serializable {

    private static final long serialVersionUID = -2309572351324327537L;
    private static final int WORKFLOW_VERSION = 1;

    private static final Logger logger = LoggerFactory.getLogger(CasePerformWorkflowHandler.class);

    private CasePerformUtils utils;
    private AuthorityService authorityService;

    public void init() {
    }

    public void onWorkflowStart(ExecutionEntity execution) {

        execution.setVariable(CasePerformUtils.WORKFLOW_VERSION_KEY, WORKFLOW_VERSION);

        if (execution.hasVariable(CasePerformUtils.OPTIONAL_PERFORMERS)) {
            Collection<NodeRef> optionalPerformers = utils.getNodeRefsList(execution, CasePerformUtils.OPTIONAL_PERFORMERS);
            Collection<NodeRef> expandedPerformers = new NodeRefsList();

            for (NodeRef performer : optionalPerformers) {
                utils.addIfNotContains(expandedPerformers, performer);
                Set<NodeRef> authorities = utils.getContainedAuthorities(performer, AuthorityType.USER, true);
                utils.addAllIfNotContains(expandedPerformers, authorities);
            }

            execution.setVariableLocal(CasePerformUtils.OPTIONAL_PERFORMERS, expandedPerformers);
        }
    }

    public void onBeforePerformingFlowTake(ExecutionEntity execution) {

        String performersKey = utils.toString(CasePerformModel.ASSOC_PERFORMERS);
        Collection<NodeRef> initialPerformers = utils.getNodeRefsList(execution, performersKey);
        Collection<NodeRef> excludedPerformers = utils.getNodeRefsList(execution, CasePerformUtils.EXCLUDED_PERFORMERS);
        List<NodeRef> performers = new NodeRefsList();

        for (NodeRef performer : initialPerformers) {
            if (!excludedPerformers.contains(performer)) {
                performers.add(performer);
            }
        }

        execution.setVariableLocal(CasePerformUtils.PERFORMERS, performers);
        utils.fillRolesByPerformers(execution);
    }

    public void onSkipPerformingGatewayStarted(ExecutionEntity execution) {

        Collection<NodeRef> optionalPerformers = utils.getNodeRefsList(execution, CasePerformUtils.OPTIONAL_PERFORMERS);
        Collection<NodeRef> performers = utils.getNodeRefsList(execution, CasePerformUtils.PERFORMERS);
        boolean hasMandatoryTasks = false;

        for (NodeRef performer : performers) {
            if (!optionalPerformers.contains(performer)) {
                hasMandatoryTasks = true;
                break;
            }
        }

        boolean skipPerforming;
        if (performers.size() == 0) {
            String candidatesKey = utils.toString(CasePerformModel.ASSOC_CANDIDATES);
            Collection<NodeRef> candidates = utils.getNodeRefsList(execution, candidatesKey);
            skipPerforming = candidates.size() == 0;
        } else {
            skipPerforming = !hasMandatoryTasks;
        }

        execution.setVariableLocal(CasePerformUtils.SKIP_PERFORMING, skipPerforming);
    }

    /* skip way */

    public void onSkipPerformingFlowTake(ExecutionEntity execution) {

    }

    /* skip way */

    /* perform way */

    public void onPerformingFlowTake(ExecutionEntity execution) {

        Collection<NodeRef> performers = utils.getNodeRefsList(execution, CasePerformUtils.PERFORMERS);
        TaskConfigs taskConfigs = new TaskConfigs();

        if (performers.size() > 0) {

            for (NodeRef performer : performers) {

                String authorityName = utils.getAuthorityName(performer);

                if (StringUtils.isNotBlank(authorityName)) {
                    TaskConfig config = new TaskConfig();
                    config.setPerformer(authorityName);
                    taskConfigs.add(config);
                }
            }

        } else {

            TaskConfig config = new TaskConfig();

            String candidatesKey = utils.toString(CasePerformModel.ASSOC_CANDIDATES);
            Collection<NodeRef> candidates = utils.getNodeRefsList(execution, candidatesKey);

            for (NodeRef candidateRef : candidates) {
                String authorityName = utils.getAuthorityName(candidateRef);
                config.addCandidate(authorityName);
            }

            taskConfigs.add(config);
        }

        Date dueDate = (Date) execution.getVariable("bpm_workflowDueDate");
        String formKey = (String) execution.getVariable("wfcp_formKey");
        for (TaskConfig config : taskConfigs) {
            config.setDueDate(dueDate);
            config.setFormKey(formKey);
        }

        execution.setVariableLocal(CasePerformUtils.TASK_CONFIGS, taskConfigs);
    }

    public void onBeforePerformTaskCreated(ExecutionEntity execution) {

        Object taskConfigObj = execution.getVariableLocal("taskConfig");
        TaskConfig taskConfig;

        if (taskConfigObj instanceof NodeRef) {

            taskConfig = new TaskConfig();

            String authorityName = utils.getAuthorityName((NodeRef) taskConfigObj);
            if (StringUtils.isNotBlank(authorityName)) {
                taskConfig.setPerformer(authorityName);
            }

            taskConfig.setDueDate((Date) execution.getVariable("bpm_workflowDueDate"));
            taskConfig.setFormKey((String) execution.getVariable("wfcp_formKey"));

            execution.setVariableLocal("taskConfig", taskConfig);

        } else if (taskConfigObj instanceof Map) {

            Map data = (Map) taskConfigObj;

            taskConfig = new TaskConfig();
            taskConfig.setFormKey((String) data.get(CasePerformUtils.TASK_CONF_FORM_KEY));
            taskConfig.setDueDate((Date) data.get(CasePerformUtils.TASK_CONF_DUE_DATE));
            taskConfig.setAssignee((String) data.get(CasePerformUtils.TASK_CONF_ASSIGNEE));
            taskConfig.setCandidateGroups((List) data.get(CasePerformUtils.TASK_CONF_CANDIDATE_GROUPS));
            taskConfig.setCandidateUsers((List) data.get(CasePerformUtils.TASK_CONF_CANDIDATE_USERS));
            taskConfig.setCategory((String) data.get(CasePerformUtils.TASK_CONF_CATEGORY));
            taskConfig.setPriority((Integer) data.get(CasePerformUtils.TASK_CONF_PRIORITY));
        } else {
            taskConfig = (TaskConfig) taskConfigObj;
        }

        NodeRef performer = utils.authorityToNodeRef(taskConfig.getPerformer());
        execution.setVariableLocal("wfcp_performer", performer);
    }

    public void onPerformTaskCreated(ExecutionEntity execution, TaskEntity task) {

        utils.shareVariables(execution, task);

        Collection<NodeRef> optionalPerformers = utils.getNodeRefsList(execution, CasePerformUtils.OPTIONAL_PERFORMERS);
        NodeRef performer = (NodeRef) task.getVariable(utils.toString(CasePerformModel.ASSOC_PERFORMER));

        boolean isOptional = optionalPerformers.contains(performer);

        task.setVariableLocal(utils.toString(CiteckWorkflowModel.PROP_IS_OPTIONAL_TASK), isOptional);
        if (!isOptional) {
            Collection<String> mandatoryTasks = utils.getStringsList(execution, CasePerformUtils.MANDATORY_TASKS);
            utils.addIfNotContains(mandatoryTasks, task.getId());
            execution.setVariable(CasePerformUtils.MANDATORY_TASKS, mandatoryTasks);
        }

        task.setVariableLocal(utils.toString(CasePerformModel.ASSOC_CASE_ROLE), utils.getCaseRole(performer, execution));
    }

    public void onPerformTaskAssigned(ExecutionEntity execution, TaskEntity task) {

        Boolean syncEnabled = (Boolean) execution.getVariable(utils.toString(CasePerformModel.PROP_SYNC_WORKFLOW_TO_ROLES));

        if (syncEnabled != null && syncEnabled) {
            String performerKey = utils.toString(CasePerformModel.ASSOC_PERFORMER);
            NodeRef performerRef = (NodeRef) task.getVariable(performerKey);
            String assignee = task.getAssignee();

            if (assignee != null) {
                NodeRef assigneeRef = authorityService.getAuthorityNodeRef(assignee);
                if (!assigneeRef.equals(performerRef)) {
                    utils.setPerformer(task, assigneeRef);
                }
            } else if (performerRef == null || !utils.hasCandidate(task, performerRef)) {

                utils.setPerformer(task, utils.getFirstGroupCandidate(task));
            }
        }
    }

    public void onPerformTaskCompleted(ExecutionEntity execution, TaskEntity task) {

        utils.shareVariables(task, execution);
        utils.saveTaskResult(execution, task);

        if (utils.isCommentMandatory(execution, task)) {
            String comment = (String) task.getVariableLocal("bpm_comment");
            if (StringUtils.isBlank(comment)) {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage("wfcf_confirmworkflow.message_comment_is_empty"));
            }
        }

        Collection<String> mandatoryTasks = utils.getStringsList(execution, CasePerformUtils.MANDATORY_TASKS);
        mandatoryTasks.remove(task.getId());
        execution.setVariable(CasePerformUtils.ABORT_PERFORMING, mandatoryTasks.size() == 0
                                                                || utils.isAbortOutcomeReceived(execution, task));
        execution.setVariable(CasePerformUtils.MANDATORY_TASKS, mandatoryTasks);
    }

    public void onAfterPerformingFlowTake(ExecutionEntity execution) {

    }

    /* perform way */

    public void onWorkflowEnd(ExecutionEntity execution) {
        utils.getNodeRefsList(execution, CasePerformUtils.OPTIONAL_PERFORMERS).clear();
        utils.getNodeRefsList(execution, CasePerformUtils.EXCLUDED_PERFORMERS).clear();
    }

    public void setUtils(CasePerformUtils utils) {
        this.utils = utils;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

}
