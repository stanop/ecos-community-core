package ru.citeck.ecos.behavior.icase;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.CasePerformModel;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.role.CaseRolePolicies;
import ru.citeck.ecos.workflow.activiti.cmd.AddParallelExecutionInstanceCmd;
import ru.citeck.ecos.workflow.perform.CasePerformUtils;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public class UpdateCasePerformTaskAssignees implements CaseRolePolicies.OnRoleAssigneesChangedPolicy {

    private String ACTIVITI_PREFIX = ActivitiConstants.ENGINE_ID + "$";

    private NodeService nodeService;
    private TaskService taskService;
    private RuntimeService runtimeService;
    private PolicyComponent policyComponent;
    private WorkflowService workflowService;
    private AuthorityService authorityService;
    private CaseActivityService caseActivityService;

    public void init() {
        policyComponent.bindClassBehaviour(
                CaseRolePolicies.OnRoleAssigneesChangedPolicy.QNAME,
                ICaseRoleModel.TYPE_ROLE,
                new JavaBehaviour(this, "onRoleAssigneesChanged", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onRoleAssigneesChanged(NodeRef roleRef, Set<NodeRef> added, Set<NodeRef> removed) {
        Set<String> activeWorkflows = getActiveWorkflows(roleRef);

        for (String workflowId : activeWorkflows) {

            WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
            taskQuery.setProcessId(workflowId);
            taskQuery.setActive(true);
            taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

            List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery, false);

            updateOrCreateTasks(workflowId.replace(ACTIVITI_PREFIX, ""), tasks, added, removed);
        }
    }

    private Set<String> getActiveWorkflows(NodeRef roleRef) {

        List<AssociationRef> caseTaskRefs = nodeService.getSourceAssocs(roleRef, CasePerformModel.ASSOC_PERFORMERS_ROLES);
        Set<String> activeWorkflows = new HashSet<>();

        for (AssociationRef ref : caseTaskRefs) {

            String workflowId = getActiveWorkflowID(ref.getSourceRef());

            if (workflowId != null) {
                activeWorkflows.add(workflowId);
            }
        }

        return activeWorkflows;
    }

    private void updateOrCreateTasks(String workflowId, List<WorkflowTask> tasks,
                                     Set<NodeRef> added, Set<NodeRef> removed) {

        List<NodeRef> addedUsers = filterByType(added, ContentModel.TYPE_PERSON);
        List<NodeRef> removedUsers = filterByType(removed, ContentModel.TYPE_PERSON);

        List<WorkflowTask> freeTasks = filterByOwners(tasks, removedUsers);

        Set<NodeRef> usersWithoutTask = new HashSet<>();

        for (NodeRef assignee : addedUsers) {

            if (filterByOwners(tasks, Collections.singleton(assignee)).size() > 0) {
                continue;
            }

            if (!freeTasks.isEmpty()) {
                int lastIdx = freeTasks.size() - 1;
                WorkflowTask task = freeTasks.get(lastIdx);
                if (changeOwner(workflowId, task, assignee)) {
                    freeTasks.remove(lastIdx);
                }
            } else {
                usersWithoutTask.add(assignee);
            }
        }

        addParallelExecutions(workflowId, usersWithoutTask);
    }

    private boolean changeOwner(String workflowId, WorkflowTask task, NodeRef newOwner) {

        String userName = (String) nodeService.getProperty(newOwner, ContentModel.PROP_USERNAME);

        if (userName != null) {

            String id = task.getId().replace(ACTIVITI_PREFIX, "");
            Map<String, Collection<NodeRef>> performersByTask = getVariable(workflowId, CasePerformUtils.TASKS_PERFORMERS);

            if (performersByTask != null) {

                Collection<NodeRef> performers = performersByTask.get(id);

                if (performers != null) {

                    String ownerName = (String) task.getProperties().get(ContentModel.PROP_OWNER);
                    NodeRef ownerRef = authorityService.getAuthorityNodeRef(ownerName);

                    if (performers.remove(ownerRef)) {

                        performers.add(newOwner);
                        runtimeService.setVariable(workflowId, CasePerformUtils.TASKS_PERFORMERS, performersByTask);

                        taskService.setAssignee(id, userName);
                        taskService.setOwner(id, userName);
                        return true;
                    }
                }
            }

        }
        return false;
    }

    private <T> T getVariable(String workflowId, String varName) {
        return (T) runtimeService.getVariable(workflowId, varName);
    }

    private List<NodeRef> filterByType(Collection<NodeRef> elements, QName type) {
        List<NodeRef> result = new ArrayList<>();
        for (NodeRef element : elements) {
            if (type.equals(nodeService.getType(element))) {
                result.add(element);
            }
        }
        return result;
    }

    private List<WorkflowTask> filterByOwners(List<WorkflowTask> tasks, Collection<NodeRef> owners) {
        List<WorkflowTask> result = new ArrayList<>();
        for (WorkflowTask task : tasks) {
            String taskOwner = (String) task.getProperties().get(ContentModel.PROP_OWNER);
            NodeRef taskOwnerRef = authorityService.getAuthorityNodeRef(taskOwner);
            if (owners.contains(taskOwnerRef)) {
                result.add(task);
            }
        }
        return result;
    }

    private void addParallelExecutions(String workflowId, Set<NodeRef> assignees) {
        if (workflowId == null || assignees == null || assignees.isEmpty()) {
            return;
        }
        List<NodeRef> assigneesList = new ArrayList<>(assignees);
        AddParallelExecutionInstanceCmd cmd = new AddParallelExecutionInstanceCmd(workflowId,
                                                                                  CasePerformUtils.SUB_PROCESS_NAME,
                                                                                  assigneesList);
        CommandExecutor commandExecutor = getCommandExecutor();
        if (commandExecutor != null) {
            commandExecutor.execute(cmd);
        }
    }

    private CommandExecutor getCommandExecutor() {
        if (runtimeService instanceof ServiceImpl) {
            return ((ServiceImpl) runtimeService).getCommandExecutor();
        }
        return null;
    }

    private String getActiveWorkflowID(NodeRef taskRef) {
        QName type = nodeService.getType(taskRef);
        if (CasePerformModel.TYPE_PERFORM_CASE_TASK.equals(type) && caseActivityService.isActive(taskRef)) {
            return (String) nodeService.getProperty(taskRef, ICaseTaskModel.PROP_WORKFLOW_INSTANCE_ID);
        }
        return null;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }
}
