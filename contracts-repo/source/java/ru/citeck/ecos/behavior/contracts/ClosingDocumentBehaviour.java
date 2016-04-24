package ru.citeck.ecos.behavior.contracts;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ContractsModel;

import java.util.List;

public class ClosingDocumentBehaviour implements NodeServicePolicies.OnCreateAssociationPolicy,
        NodeServicePolicies.OnDeleteAssociationPolicy {

    private NodeService nodeService;
    private PolicyComponent policyComponent;

    private static Log logger = LogFactory.getLog(ClosingDocumentBehaviour.class);

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void init() {
        bind(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, "onCreateAssociation");
        bind(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, "onDeleteAssociation");
    }

    private void bind(QName policy, String method) {
        this.policyComponent.bindAssociationBehaviour(
                policy,
                ContractsModel.TYPE_CONTRACTS_CLOSING_DOCUMENT,
                ContractsModel.ASSOC_CLOSING_DOCUMENT_AGREEMENT,
                new JavaBehaviour(this, method, Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onCreateAssociation(AssociationRef associationRef) {
        NodeRef sourceRef = associationRef.getSourceRef();
        NodeRef targetRef = associationRef.getTargetRef();
        updateAssoc(sourceRef, targetRef, true);
    }

    @Override
    public void onDeleteAssociation(AssociationRef associationRef) {
        NodeRef sourceRef = associationRef.getSourceRef();
        NodeRef targetRef = associationRef.getTargetRef();
        updateAssoc(sourceRef, targetRef, false);
    }

    private void updateAssoc(NodeRef closDoc, NodeRef contracts, boolean isCreate) {
        List<AssociationRef> contractors = nodeService.getTargetAssocs(contracts, ContractsModel.ASSOC_CONTRACTOR);
        if (contractors.size() > 0) {
            if (isCreate) {
                nodeService.createAssociation(closDoc, contractors.get(0).getTargetRef(), ContractsModel.ASSOC_CONTRACTOR);
            } else {
                nodeService.removeAssociation(closDoc,contractors.get(0).getTargetRef(), ContractsModel.ASSOC_CONTRACTOR);
            }
        } else {
            logger.error("Contractor is not exists");
        }
    }

}
