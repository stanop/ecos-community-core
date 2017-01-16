package ru.citeck.ecos.behavior.icase;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.CasePerformModel;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.role.CaseRolePolicies;
import ru.citeck.ecos.service.AlfrescoServices;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.workflow.activiti.cmd.UpdateCasePerformAssigneesCmd;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public class UpdateCasePerformAssignees implements CaseRolePolicies.OnRoleAssigneesChangedPolicy,
                                                   CaseRolePolicies.OnCaseRolesAssigneesChangedPolicy {

    private static final String UPDATE_BY_ROLE_KEY = UpdateCasePerformAssignees.class + ".update-by-role";

    private ServiceRegistry serviceRegistry;

    private NodeService nodeService;
    private RuntimeService runtimeService;
    private PolicyComponent policyComponent;
    private CaseActivityService caseActivityService;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                CaseRolePolicies.OnCaseRolesAssigneesChangedPolicy.QNAME,
                ICaseRoleModel.ASPECT_HAS_ROLES,
                new JavaBehaviour(this, "onCaseRolesAssigneesChanged", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindClassBehaviour(
                CaseRolePolicies.OnRoleAssigneesChangedPolicy.QNAME,
                ICaseRoleModel.TYPE_ROLE,
                new JavaBehaviour(this, "onRoleAssigneesChanged", Behaviour.NotificationFrequency.EVERY_EVENT)
        );
    }

    @Override
    public void onRoleAssigneesChanged(final NodeRef roleRef, final Set<NodeRef> added, final Set<NodeRef> removed) {
        Map<NodeRef, RoleState> byRole = TransactionalResourceHelper.getMap(UPDATE_BY_ROLE_KEY);
        byRole.put(roleRef, RoleState.merge(byRole.get(roleRef), new RoleState(added, removed)));
    }

    @Override
    public void onCaseRolesAssigneesChanged(NodeRef caseRef) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {
                updateTasks();
                return null;
            }
        });
    }

    private void updateTasks() {

        Map<NodeRef, RoleState> byRole = TransactionalResourceHelper.getMap(UPDATE_BY_ROLE_KEY);

        for (Map.Entry<NodeRef, RoleState> entry : byRole.entrySet()) {
            NodeRef roleRef = entry.getKey();
            if (!nodeService.exists(roleRef)) {
                continue;
            }
            Set<NodeRef> added = entry.getValue().added;
            Set<NodeRef> removed = entry.getValue().removed;
            updateTask(roleRef, added, removed);
        }

        byRole.clear();
    }

    private void updateTask(NodeRef roleRef, Set<NodeRef> added, Set<NodeRef> removed) {

        Set<String> activeWorkflows = getActiveWorkflows(roleRef);
        CommandExecutor commandExecutor = getCommandExecutor();

        if (commandExecutor != null) {
            for (String workflowId : activeWorkflows) {
                commandExecutor.execute(new UpdateCasePerformAssigneesCmd(workflowId, added, removed, serviceRegistry));
            }
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

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.runtimeService = (RuntimeService) serviceRegistry.getService(AlfrescoServices.ACTIVITI_RUNTIME_SERVICE);
        this.policyComponent = (PolicyComponent) serviceRegistry.getService(AlfrescoServices.POLICY_COMPONENT);
        this.caseActivityService = (CaseActivityService) serviceRegistry.getService(CiteckServices.CASE_ACTIVITY_SERVICE);
    }

    private static class RoleState {
        public Set<NodeRef> added = new HashSet<>();
        public Set<NodeRef> removed = new HashSet<>();

        RoleState(Set<NodeRef> added, Set<NodeRef> removed) {
            this.added = added;
            this.removed = removed;
        }

        static RoleState merge(RoleState state0, RoleState state1) {
            Set<NodeRef> added = new HashSet<>();
            Set<NodeRef> removed = new HashSet<>();
            if (state0 != null) {
                added.addAll(state0.added);
                removed.addAll(state0.removed);
            }
            if (state1 != null) {
                added.removeAll(state1.removed);
                removed.removeAll(state1.added);
                added.addAll(state1.added);
                removed.addAll(state1.removed);
            }
            return new RoleState(added, removed);
        }
    }
}
