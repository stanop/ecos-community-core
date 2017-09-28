package ru.citeck.ecos.behavior.event.trigger;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.event.EventService;
import ru.citeck.ecos.model.ICaseEventModel;
import ru.citeck.ecos.model.ICaseModel;

import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CaseEventTrigger implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private static final Log logger = LogFactory.getLog(CaseEventTrigger.class);

    private int order = 60;

    private PolicyComponent policyComponent;
    private EventService eventService;

    public void init() {
        OrderedBehaviour behaviour;
        QName type = ICaseModel.ASPECT_CASE;
        NotificationFrequency frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT;

        behaviour = new OrderedBehaviour(this, "onUpdateProperties", frequency, order);
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, type, behaviour);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        eventService.fireEvent(nodeRef, ICaseEventModel.CONSTR_CASE_PROPERTIES_CHANGED);
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
}
