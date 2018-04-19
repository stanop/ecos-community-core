package ru.citeck.ecos.behavior.role;

import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.role.CaseRoleService;
import ru.citeck.ecos.utils.TransactionUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class UpdateRolesBehaviour implements OnUpdatePropertiesPolicy,
                                             OnCreateAssociationPolicy,
                                             OnDeleteAssociationPolicy,
                                             OnCreateNodePolicy {

    private static final String NODES_TO_UPDATE_TXN_KEY = UpdateRolesBehaviour.class.getName();

    private static final Log LOGGER = LogFactory.getLog(UpdateRolesBehaviour.class);
    private static final int ORDER = 100;

    private PolicyComponent policyComponent;
    private CaseRoleService caseRoleService;
    private NodeService nodeService;

    private boolean mergeRolesUpdating = true;

    public void init() {

        this.policyComponent.bindClassBehaviour(
                OnUpdatePropertiesPolicy.QNAME,
                ICaseRoleModel.ASPECT_HAS_ROLES,
                new OrderedBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT, ORDER)
        );

        this.policyComponent.bindAssociationBehaviour(
                OnCreateAssociationPolicy.QNAME,
                ICaseRoleModel.ASPECT_HAS_ROLES,
                new OrderedBehaviour(this, "onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT, ORDER)
        );
        this.policyComponent.bindAssociationBehaviour(
                OnDeleteAssociationPolicy.QNAME,
                ICaseRoleModel.ASPECT_HAS_ROLES,
                new OrderedBehaviour(this, "onDeleteAssociation", NotificationFrequency.TRANSACTION_COMMIT, ORDER)
        );
        this.policyComponent.bindAssociationBehaviour(
                OnCreateNodePolicy.QNAME,
                ICaseRoleModel.ASPECT_HAS_ROLES,
                new OrderedBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT, ORDER)
        );
    }

    @Override
    public void onCreateAssociation(AssociationRef associationRef) {
        updateRoles(associationRef.getSourceRef());
    }

    @Override
    public void onDeleteAssociation(AssociationRef associationRef) {
        updateRoles(associationRef.getSourceRef());
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> map, Map<QName, Serializable> map1) {
        updateRoles(nodeRef);
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        updateRoles(childAssociationRef.getChildRef());
    }

    private void updateRoles(NodeRef caseRef) {
        if (isValidCase(caseRef)) {
            if (mergeRolesUpdating) {
                TransactionUtils.processBeforeCommit(NODES_TO_UPDATE_TXN_KEY,
                                                     caseRef,
                                                     caseRoleService::updateRoles);
            } else {
                caseRoleService.updateRoles(caseRef);
            }
        }
    }

    private boolean isValidCase(NodeRef caseRef) {
        return caseRef != null && nodeService.exists(caseRef)
               && !nodeService.hasAspect(caseRef, ICaseModel.ASPECT_CASE_TEMPLATE);
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setCaseRoleService(CaseRoleService caseRoleService) {
        this.caseRoleService = caseRoleService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setMergeRolesUpdating(boolean value) {
        this.mergeRolesUpdating = value;
    }
}
