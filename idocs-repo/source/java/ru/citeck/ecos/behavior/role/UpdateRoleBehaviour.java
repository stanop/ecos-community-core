package ru.citeck.ecos.behavior.role;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.role.CaseRoleService;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class UpdateRoleBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private static final int ORDER = 150;

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private CaseRoleService caseRoleService;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                ICaseRoleModel.TYPE_ROLE,
                new OrderedBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT, ORDER)
        );
    }

    @Override
    public void onUpdateProperties(NodeRef roleRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (nodeService.exists(roleRef)) {
            NodeRef parentRef = nodeService.getPrimaryParent(roleRef).getParentRef();
            if (nodeService.hasAspect(parentRef, ICaseModel.ASPECT_CASE)) {
                caseRoleService.updateRole(roleRef);
            }
        }
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
}
