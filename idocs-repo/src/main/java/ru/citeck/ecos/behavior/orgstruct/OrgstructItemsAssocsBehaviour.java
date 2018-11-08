package ru.citeck.ecos.behavior.orgstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import ru.citeck.ecos.behavior.JavaBehaviour;
import ru.citeck.ecos.model.OrgStructModel;
import ru.citeck.ecos.utils.RepoUtils;

public class OrgstructItemsAssocsBehaviour implements NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy {

    private PolicyComponent policyComponent;
    private NodeService nodeService;

    public void init() {
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                ContentModel.TYPE_AUTHORITY_CONTAINER, OrgStructModel.ASSOC_ROLE_TYPE,
                new JavaBehaviour(this, "onCreateAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                ContentModel.TYPE_AUTHORITY_CONTAINER, OrgStructModel.ASSOC_BRANCH_TYPE,
                new JavaBehaviour(this, "onCreateAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
                ContentModel.TYPE_AUTHORITY_CONTAINER, OrgStructModel.ASSOC_ROLE_TYPE,
                new JavaBehaviour(this, "onDeleteAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
                ContentModel.TYPE_AUTHORITY_CONTAINER, OrgStructModel.ASSOC_BRANCH_TYPE,
                new JavaBehaviour(this, "onDeleteAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onCreateAssociation(AssociationRef associationRef) {
        QName assocQName = associationRef.getTypeQName();
        final NodeRef sourceRef = associationRef.getSourceRef();
        final NodeRef targetRef = associationRef.getTargetRef();
        if (assocQName.equals(OrgStructModel.ASSOC_ROLE_TYPE)) {
            String roleName = RepoUtils.getProperty(targetRef, ContentModel.PROP_NAME, String.class, nodeService);
            if (nodeService.exists(sourceRef) && StringUtils.isNoneBlank(roleName)) {
                nodeService.setProperty(sourceRef, OrgStructModel.PROP_ROLE_TYPE, roleName);
            }
        } else if (assocQName.equals(OrgStructModel.ASSOC_BRANCH_TYPE)) {
            String branchName = RepoUtils.getProperty(targetRef, ContentModel.PROP_NAME, String.class, nodeService);
            if (nodeService.exists(sourceRef) && StringUtils.isNoneBlank(branchName)) {
                nodeService.setProperty(sourceRef, OrgStructModel.PROP_BRANCH_TYPE, branchName);
            }
        }
    }

    @Override
    public void onDeleteAssociation(AssociationRef associationRef) {
        QName assocQName = associationRef.getTypeQName();
        final NodeRef sourceRef = associationRef.getSourceRef();
        if (nodeService.exists(sourceRef) && assocQName.equals(OrgStructModel.ASSOC_ROLE_TYPE)) {
            nodeService.setProperty(sourceRef, OrgStructModel.PROP_ROLE_TYPE, "");
        } else if (nodeService.exists(sourceRef) && assocQName.equals(OrgStructModel.ASSOC_BRANCH_TYPE)) {
            nodeService.setProperty(sourceRef, OrgStructModel.PROP_BRANCH_TYPE, "");
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
