package ru.citeck.ecos.behavior.event.trigger;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.policy.Behaviour;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.event.EventService;
import ru.citeck.ecos.model.EventModel;

/**
 * @author Pavel Simonov
 */
public class UserActionEventTrigger implements OnCreateChildAssociationPolicy {

    public static final String ADDITIONAL_DATA_VARIABLE = "additionalData";

    private static final int ORDER = 60;

    private PolicyComponent policyComponent;
    private EventService eventService;
    private NodeService nodeService;

    public void init() {
        OrderedBehaviour behaviour;
        QName clazz = EventModel.TYPE_USER_ACTION;
        QName assoc = EventModel.ASSOC_ADDITIONAL_DATA_ITEMS;
        Behaviour.NotificationFrequency frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT;
        behaviour = new OrderedBehaviour(this, "onCreateChildAssociation", frequency, ORDER);
        policyComponent.bindAssociationBehaviour(OnCreateChildAssociationPolicy.QNAME, clazz, assoc, behaviour);
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean primary) {
        NodeRef eventRef = childAssociationRef.getParentRef();
        NodeRef additionalDataRef = childAssociationRef.getChildRef();
        if (nodeService.exists(eventRef) && nodeService.exists(additionalDataRef)) {
            ActionConditionUtils.getTransactionVariables().put(ADDITIONAL_DATA_VARIABLE, additionalDataRef);
            eventService.fireConcreteEvent(eventRef);
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
