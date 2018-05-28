package ru.citeck.ecos.behavior;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.ContractsModel;

/**
 * @author Andrey Platunov on 31/08/2017.
 */

public class ContractsSetAspectForAttachments implements NodeServicePolicies.OnCreateChildAssociationPolicy{

    PolicyComponent policyComponent;
    NodeService nodeService;

    public void init() {
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                ContractsModel.CONTRACTS_TYPE, ICaseModel.ASSOC_DOCUMENTS,
                new JavaBehaviour(this,
                        "onCreateChildAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean b) {
        NodeRef document = childAssociationRef.getChildRef();
        if (document == null || !this.nodeService.exists(document)) return;
        nodeService.addAspect(document, ContractsModel.ASPECT_IS_CONTRACT_ATTACHMENT, null);
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
