package ru.citeck.ecos.behavior.icase;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.NodeActionsService;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.model.EventModel;
import ru.citeck.ecos.utils.TransactionUtils;

/**
 * @author Pavel Simonov
 */
public class ClearNodeActionsCache extends AbstractBehaviour implements NodeServicePolicies.OnCreateAssociationPolicy,
                                                                        NodeServicePolicies.OnDeleteAssociationPolicy {

    private static final String TXN_KEY = ClearNodeActionsCache.class.getName();

    private NodeActionsService nodeActionsService;

    @Override
    protected void beforeInit() {
        setAssocName(EventModel.ASSOC_EVENT_SOURCE);
        setClassName(EventModel.TYPE_USER_ACTION);
    }

    @PolicyMethod(policy = NodeServicePolicies.OnCreateAssociationPolicy.class,
                  frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
    public void onCreateAssociation(AssociationRef nodeAssocRef) {
        clearCache(nodeAssocRef.getTargetRef());
    }

    @PolicyMethod(policy = NodeServicePolicies.OnDeleteAssociationPolicy.class,
                  frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
    public void onDeleteAssociation(AssociationRef nodeAssocRef) {
        clearCache(nodeAssocRef.getTargetRef());
    }

    private void clearCache(NodeRef nodeRef) {
        TransactionUtils.processBeforeCommit(TXN_KEY, nodeRef, r -> nodeActionsService.clearCache(r));
    }

    @Autowired
    public void setNodeActionsService(NodeActionsService nodeActionsService) {
        this.nodeActionsService = nodeActionsService;
    }
}
