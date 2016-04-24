package ru.citeck.ecos.behavior.activity;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import ru.citeck.ecos.behavior.ChainingJavaBehaviour;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.ActivityModel;

/**
 * @author Pavel Simonov
 */

public class CaseActivityIndexBehaviour implements NodeServicePolicies.OnCreateNodePolicy {

    private CaseActivityService caseActivityService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                ActivityModel.TYPE_ACTIVITY,
                new ChainingJavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        NodeRef nodeRef = childAssociationRef.getChildRef();
        if (nodeService.exists(nodeRef)) {
            caseActivityService.setIndex(nodeRef, Integer.MAX_VALUE);
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
