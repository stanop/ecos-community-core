package ru.citeck.ecos.workflow.perform;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.model.CasePerformModel;
import ru.citeck.ecos.model.CiteckWorkflowModel;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.role.CaseRoleService;

import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CasePerformWorkflowHandler implements Serializable {

    private static final long serialVersionUID = -2309572351324327537L;

    private static final Logger logger = LoggerFactory.getLogger(CasePerformWorkflowHandler.class);

    private CasePerformUtils utils;
    private NodeService nodeService;
    private AuthorityService authorityService;
    private CaseRoleService caseRoleService;

    public void init() {
    }

    public void onWorkflowStart(ExecutionEntity execution) {

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

        ActivitiScriptNode pack = (ActivitiScriptNode) execution.getVariable(utils.toString(WorkflowModel.ASSOC_PACKAGE));
        NodeRef caseTask = null;

        List<AssociationRef> assocs = nodeService.getSourceAssocs(pack.getNodeRef(), ICaseTaskModel.ASSOC_WORKFLOW_PACKAGE);
        if (assocs != null && !assocs.isEmpty()) {
            caseTask = assocs.get(0).getSourceRef();
        }

        if (caseTask != null) {

            Map<NodeRef, List<NodeRef>> performersRolesPool;
            performersRolesPool = CasePerformUtils.getMap(execution, CasePerformUtils.PERFORMER_ROLES_POOL);

            List<AssociationRef> roleAssocs = nodeService.getTargetAssocs(caseTask, CasePerformModel.ASSOC_PERFORMERS_ROLES);
            for (AssociationRef ref : roleAssocs) {
                NodeRef roleRef = ref.getTargetRef();
                Set<NodeRef> assignees = caseRoleService.getAssignees(roleRef);
                for (NodeRef assignee : assignees) {
                    List<NodeRef> roles = performersRolesPool.get(assignee);
                    if (roles == null) {
                        roles = new ArrayList<>();
                        performersRolesPool.put(assignee, roles);
                    }
                    roles.add(roleRef);
                }
            }
        }
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
        execution.setVariableLocal(CasePerformUtils.SKIP_PERFORMING, performers.size() == 0 || !hasMandatoryTasks);
    }

    /* skip way */

    public void onSkipPerformingFlowTake(ExecutionEntity execution) {

    }

    /* skip way */

    /* perform way */

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

        Map<NodeRef, List<NodeRef>> performersRolesPool;
        performersRolesPool = CasePerformUtils.getMap(execution, CasePerformUtils.PERFORMER_ROLES_POOL);
        List<NodeRef> roles = performersRolesPool.get(performer);
        if (roles != null && !roles.isEmpty()) {
            NodeRef role = roles.remove(roles.size() - 1);
            task.setVariableLocal(utils.toString(CasePerformModel.ASSOC_CASE_ROLE), role);
        }
    }

    public void onPerformTaskAssigned(ExecutionEntity execution, TaskEntity task) {

        Boolean syncEnabled = (Boolean) execution.getVariable(utils.toString(CasePerformModel.PROP_SYNC_PERFORMERS));

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

    public void setUtils(CasePerformUtils utils) {
        this.utils = utils;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setCaseRoleService(CaseRoleService caseRoleService) {
        this.caseRoleService = caseRoleService;
    }
}
