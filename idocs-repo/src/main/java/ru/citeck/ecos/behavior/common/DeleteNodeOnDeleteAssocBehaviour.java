package ru.citeck.ecos.behavior.common;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.utils.NodeUtils;
import ru.citeck.ecos.utils.RepoUtils;

public class DeleteNodeOnDeleteAssocBehaviour extends AbstractBehaviour
        implements NodeServicePolicies.OnDeleteAssociationPolicy {

    private NodeUtils nodeUtils;

    @Override
    protected void beforeInit() {
        if (this.getAssocName() == null) {
            throw new AlfrescoRuntimeException("Not set assoc name!");
        }
    }

    @PolicyMethod(policy = NodeServicePolicies.OnDeleteAssociationPolicy.class,
            frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT, checkNodeRefs = false, runAsSystem = true)
    public void onDeleteAssociation(AssociationRef nodeAssocRef) {

        NodeRef node = nodeAssocRef.getSourceRef();
        if (node != null && nodeService.exists(node)) {

            if (!nodeUtils.getAssocTarget(node, this.getAssocName()).isPresent()) {
                RepoUtils.deleteNode(node, nodeService);
            }
        }
    }

    @Autowired
    public void setNodeUtils(NodeUtils nodeUtils) {
        this.nodeUtils = nodeUtils;
    }

}
