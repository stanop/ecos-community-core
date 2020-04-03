package ru.citeck.ecos.behavior.event.trigger;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import ru.citeck.ecos.icase.activity.dto.EventRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventService;
import ru.citeck.ecos.model.EventModel;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.AlfActivityUtils;

import javax.annotation.PostConstruct;

/**
 * @author Pavel Simonov
 */
@Component
@DependsOn("idocs.dictionaryBootstrap")
public class UserActionEventTrigger implements OnCreateChildAssociationPolicy {

    public static final String ADDITIONAL_DATA_VARIABLE = "additionalData";

    private static final int ORDER = 60;

    private CaseActivityEventService caseActivityEventService;
    private AlfActivityUtils alfActivityUtils;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    @Autowired
    public UserActionEventTrigger(ServiceRegistry serviceRegistry) {
        this.caseActivityEventService = (CaseActivityEventService) serviceRegistry
            .getService(CiteckServices.CASE_ACTIVITY_EVENT_SERVICE);
        this.alfActivityUtils = (AlfActivityUtils) serviceRegistry.getService(CiteckServices.ALF_ACTIVITY_UTILS);
        this.policyComponent = serviceRegistry.getPolicyComponent();
        this.nodeService = serviceRegistry.getNodeService();
    }

    @PostConstruct
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
        NodeRef eventNodeRef = childAssociationRef.getParentRef();
        NodeRef additionalDataRef = childAssociationRef.getChildRef();
        if (nodeService.exists(eventNodeRef) && nodeService.exists(additionalDataRef)) {
            ActionConditionUtils.getTransactionVariables().put(ADDITIONAL_DATA_VARIABLE, additionalDataRef);
            EventRef eventRef = alfActivityUtils.composeEventRef(eventNodeRef);
            caseActivityEventService.fireConcreteEvent(eventRef);
        }
    }
}
