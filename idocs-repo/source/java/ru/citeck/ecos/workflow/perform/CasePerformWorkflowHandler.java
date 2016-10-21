package ru.citeck.ecos.workflow.perform;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.model.CasePerformModel;
import ru.citeck.ecos.model.CiteckWorkflowModel;

import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CasePerformWorkflowHandler implements Serializable {

    private static final long serialVersionUID = -2309572351324327537L;

    private CasePerformUtils utils;

    public void init() {
    }

    public void onWorkflowStart(ExecutionEntity execution) {

        if (execution.hasVariable(CasePerformUtils.OPTIONAL_PERFORMERS)) {
            Collection<NodeRef> optionalPerformers = utils.getCollection(execution, CasePerformUtils.OPTIONAL_PERFORMERS);
            Set<NodeRef> expandedPerformers = new HashSet<>();

            for (NodeRef performer : optionalPerformers) {
                expandedPerformers.add(performer);
                expandedPerformers.addAll(utils.getContainedAuthorities(performer, AuthorityType.USER, true));
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
        utils.saveTaskPerformers(execution, task);

        Collection<NodeRef> optionalPerformers = utils.getCollection(execution, CasePerformUtils.OPTIONAL_PERFORMERS);
        Set<NodeRef> performers = utils.getTaskPerformers(task);
        boolean isOptional = optionalPerformers.containsAll(performers);

        task.setVariableLocal(utils.toString(CiteckWorkflowModel.PROP_IS_OPTIONAL_TASK), isOptional);
        if (!isOptional) {
            Collection<String> mandatoryTasks = utils.getCollection(execution, CasePerformUtils.MANDATORY_TASKS);
            if (!mandatoryTasks.contains(task.getId())) {
                mandatoryTasks.add(task.getId());
            }
        }
    }

    public void onPerformTaskAssigned(ExecutionEntity execution, TaskEntity task) {

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
}
