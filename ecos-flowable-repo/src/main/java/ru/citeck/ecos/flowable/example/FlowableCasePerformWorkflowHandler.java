package ru.citeck.ecos.flowable.example;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.apache.commons.lang.StringUtils;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.TaskEntity;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.flowable.utils.FlowableCasePerformUtils;
import ru.citeck.ecos.model.CasePerformModel;
import ru.citeck.ecos.model.CiteckWorkflowModel;
import ru.citeck.ecos.workflow.perform.CasePerformUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Flowable case perform workflow handler
 */
public class FlowableCasePerformWorkflowHandler implements Serializable {

    /**
     * Constants
     */
    private static final int WORKFLOW_VERSION = 1;
    private static final long serialVersionUID = 2032666102315887416L;

    /**
     * Case perform utils
     */
    private FlowableCasePerformUtils utils;

    /**
     * Authority service
     */
    private AuthorityService authorityService;

    public void init() {

    }

    /**
     * Ons workflow start
     * @param execution Execution
     */
    public void onWorkflowStart(ExecutionEntity execution) {
        execution.setVariable(CasePerformUtils.WORKFLOW_VERSION_KEY, WORKFLOW_VERSION);

        /** Check optional performers */
        if (execution.hasVariable(CasePerformUtils.OPTIONAL_PERFORMERS)) {
            Collection<NodeRef> optionalPerformers = utils.getCollection(execution, CasePerformUtils.OPTIONAL_PERFORMERS);
            Collection<NodeRef> expandedPerformers = new ArrayList<>();
            for (NodeRef performer : optionalPerformers) {
                utils.addIfNotContains(expandedPerformers, performer);
                Set<NodeRef> authorities = utils.getContainedAuthorities(performer, AuthorityType.USER, true);
                utils.addAllIfNotContains(expandedPerformers, authorities);
            }
            execution.setVariableLocal(CasePerformUtils.OPTIONAL_PERFORMERS, expandedPerformers);
        }
    }

    /**
     * On before performing flow take
     * @param execution Execution
     */
    public void onBeforePerformingFlowTake(ExecutionEntity execution) {
        String performersKey = utils.toString(CasePerformModel.ASSOC_PERFORMERS);
        Collection<NodeRef> initialPerformers = utils.getCollection(execution, performersKey);
        Collection<NodeRef> excludedPerformers = utils.getCollection(execution, CasePerformUtils.EXCLUDED_PERFORMERS);
        List<NodeRef> performers = new ArrayList<>();

        for (NodeRef performer : initialPerformers) {
            if (!excludedPerformers.contains(performer)) {
                performers.add(performer);
            }
        }

        execution.setVariableLocal(CasePerformUtils.PERFORMERS, performers);
        utils.fillRolesByPerformers(execution);
    }

    public void onSkipPerformingGatewayStarted(ExecutionEntity execution) {

        Collection<NodeRef> optionalPerformers = utils.getCollection(execution, CasePerformUtils.OPTIONAL_PERFORMERS);
        Collection<NodeRef> performers = utils.getCollection(execution, CasePerformUtils.PERFORMERS);
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
            Collection<NodeRef> candidates = utils.getCollection(execution, candidatesKey);
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

        Collection<NodeRef> performers = utils.getCollection(execution, CasePerformUtils.PERFORMERS);
        Collection<Map<String, Serializable>> taskConfigs = utils.getCollection(execution, CasePerformUtils.TASK_CONFIGS);

        if (performers.size() > 0) {

            for (NodeRef performer : performers) {

                String authorityName = utils.getAuthorityName(performer);

                if (StringUtils.isNotBlank(authorityName)) {

                    Map<String, Serializable> config = CasePerformUtils.createMap();
                    config.put(CasePerformUtils.TASK_CONF_CANDIDATE_USERS, new ArrayList<>());
                    ArrayList<String> candidateGroups = new ArrayList<>();
                    config.put(CasePerformUtils.TASK_CONF_CANDIDATE_GROUPS, candidateGroups);

                    if (authorityName.startsWith("GROUP_")) {
                        candidateGroups.add(authorityName);
                    } else {
                        config.put(CasePerformUtils.TASK_CONF_ASSIGNEE, authorityName);
                    }
                    config.put("wfcp_performer", performer);

                    taskConfigs.add(config);
                }
            }

        } else {

            String candidatesKey = utils.toString(CasePerformModel.ASSOC_CANDIDATES);
            Collection<NodeRef> candidates = utils.getCollection(execution, candidatesKey);

            ArrayList<String> candidateGroups = new ArrayList<>();
            ArrayList<String> candidateUsers = new ArrayList<>();

            for (NodeRef candidateRef : candidates) {

                String authorityName = utils.getAuthorityName(candidateRef);

                if (StringUtils.isNotBlank(authorityName)) {
                    if (authorityName.startsWith("GROUP_")) {
                        candidateGroups.add(authorityName);
                    } else {
                        candidateUsers.add(authorityName);
                    }
                }
            }

            Map<String, Serializable> config = CasePerformUtils.createMap();
            config.put(CasePerformUtils.TASK_CONF_CANDIDATE_GROUPS, candidateGroups);
            config.put(CasePerformUtils.TASK_CONF_CANDIDATE_USERS, candidateUsers);
            taskConfigs.add(config);
        }

        Date dueDate = (Date) execution.getVariable("bpm_workflowDueDate");
        String formKey = (String) execution.getVariable("wfcp_formKey");
        for (Map<String, Serializable> config : taskConfigs) {
            config.put(CasePerformUtils.TASK_CONF_DUE_DATE, dueDate);
            config.put(CasePerformUtils.TASK_CONF_FORM_KEY, formKey);
        }
    }

    public void onBeforePerformTaskCreated(ExecutionEntity execution) {

        Object taskConfig = execution.getVariableLocal("taskConfig");

        if (taskConfig instanceof NodeRef) {

            Map<String, Object> config = CasePerformUtils.createMap();
            config.put(CasePerformUtils.TASK_CONF_CANDIDATE_USERS, new ArrayList<>());
            ArrayList<String> candidateGroups = new ArrayList<>();
            config.put(CasePerformUtils.TASK_CONF_CANDIDATE_GROUPS, candidateGroups);

            String authorityName = utils.getAuthorityName((NodeRef) taskConfig);
            if (StringUtils.isNotBlank(authorityName)) {
                if (authorityName.startsWith("GROUP_")) {
                    candidateGroups.add(authorityName);
                } else {
                    config.put(CasePerformUtils.TASK_CONF_ASSIGNEE, authorityName);
                }
            }

            config.put(CasePerformUtils.TASK_CONF_DUE_DATE, execution.getVariable("bpm_workflowDueDate"));
            config.put(CasePerformUtils.TASK_CONF_FORM_KEY, execution.getVariable("wfcp_formKey"));
            config.put("wfcp_performer", taskConfig);

            execution.setVariableLocal("taskConfig", config);
        }

        Map<String, Object> config = FlowableCasePerformUtils.getMap(execution, "taskConfig");
        execution.setVariableLocal("wfcp_performer", config.get("wfcp_performer"));
    }

    public void onPerformTaskCreated(ExecutionEntity execution, TaskEntity task) {

        utils.shareVariables(execution, task);

        Collection<NodeRef> optionalPerformers = utils.getCollection(execution, CasePerformUtils.OPTIONAL_PERFORMERS);
        NodeRef performer = (NodeRef) task.getVariable(utils.toString(CasePerformModel.ASSOC_PERFORMER));

        boolean isOptional = optionalPerformers.contains(performer);

        task.setVariableLocal(utils.toString(CiteckWorkflowModel.PROP_IS_OPTIONAL_TASK), isOptional);
        if (!isOptional) {
            Collection<String> mandatoryTasks = utils.getCollection(execution, CasePerformUtils.MANDATORY_TASKS);
            utils.addIfNotContains(mandatoryTasks, task.getId());
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

        Collection<String> mandatoryTasks = utils.getCollection(execution, CasePerformUtils.MANDATORY_TASKS);
        mandatoryTasks.remove(task.getId());
        execution.setVariable(CasePerformUtils.ABORT_PERFORMING, mandatoryTasks.size() == 0
                || utils.isAbortOutcomeReceived(execution, task));
    }

    public void onAfterPerformingFlowTake(ExecutionEntity execution) {

    }

    /* perform way */

    public void onWorkflowEnd(ExecutionEntity execution) {
        utils.getCollection(execution, CasePerformUtils.OPTIONAL_PERFORMERS).clear();
        utils.getCollection(execution, CasePerformUtils.EXCLUDED_PERFORMERS).clear();
    }

    public void setUtils(FlowableCasePerformUtils utils) {
        this.utils = utils;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }
}
