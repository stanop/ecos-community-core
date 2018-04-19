package ru.citeck.ecos.behavior;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.*;
import ru.citeck.ecos.model.ContractsModel;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.model.ContractsWorkflowModel;

import java.util.List;

/**
 * @author Anton Ivanov
 */
public class SelectSignerBehaviour implements NodeServicePolicies.OnCreateAssociationPolicy {

    private static final String TASK_OUTCOME = "Done";

    private PolicyComponent policyComponent;
    private WorkflowService workflowService;
    private NodeService nodeService;

    public void init() {
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                ContractsModel.CONTRACTS_TYPE, IdocsModel.ASSOC_SIGNER,
                new JavaBehaviour(this, "onCreateAssociation", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                ContractsModel.CONTRACTS_SUPPLEMENTARY_TYPE, IdocsModel.ASSOC_SIGNER,
                new JavaBehaviour(this, "onCreateAssociation", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onCreateAssociation(AssociationRef associationRef) {
        final NodeRef contractRef = associationRef.getSourceRef();
        if (nodeService.exists(contractRef)) {
            List<AssociationRef> signers = nodeService.getTargetAssocs(contractRef, IdocsModel.ASSOC_SIGNER);
            if (signers != null && signers.size() > 0) {
                AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
                    @Override
                    public Void doWork() throws Exception {
                        completeSelectSignerTasks(contractRef);
                        return null;
                    }
                });
            }
        }
    }

    private void completeSelectSignerTasks(NodeRef contractRef) {
        List<WorkflowInstance> workflows = workflowService.getWorkflowsForContent(contractRef, true);

        for (WorkflowInstance wf : workflows) {

            WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();
            tasksQuery.setActive(true);
            tasksQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
            tasksQuery.setProcessId(wf.getId());
            tasksQuery.setTaskName(ContractsWorkflowModel.TYPE_SELECT_SIGNER_TASK);

            List<WorkflowTask> tasks = workflowService.queryTasks(tasksQuery, false);
            for (WorkflowTask task : tasks) {
                workflowService.endTask(task.getId(), TASK_OUTCOME);
            }
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
