package ru.citeck.ecos.behavior.event.trigger;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeUtils;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import ru.citeck.ecos.icase.activity.dto.EventRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventService;
import ru.citeck.ecos.model.EcosProcessModel;
import ru.citeck.ecos.model.EventModel;
import ru.citeck.ecos.utils.TransactionUtils;

import javax.annotation.PostConstruct;

@Component
@DependsOn("idocs.dictionaryBootstrap")
public class EProcUserActionEventTrigger implements NodeServicePolicies.OnCreateChildAssociationPolicy {

    public static final String ADDITIONAL_DATA_VARIABLE = "additionalData";

    private static final int ORDER = 60;

    private CaseActivityEventService caseActivityEventService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    @Autowired
    public EProcUserActionEventTrigger(CaseActivityEventService caseActivityEventService,
                                       PolicyComponent policyComponent,
                                       NodeService nodeService) {
        this.caseActivityEventService = caseActivityEventService;
        this.policyComponent = policyComponent;
        this.nodeService = nodeService;
    }

    @PostConstruct
    public void init() {
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                EventModel.TYPE_USER_ACTION,
                EcosProcessModel.ASSOC_ADDITIONAL_EVENT_DATA_ITEMS,
                new OrderedBehaviour(this, "onCreateChildAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT, ORDER));
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean primary) {
        NodeRef caseRef = childAssociationRef.getParentRef();
        NodeRef additionalDataRef = childAssociationRef.getChildRef();
        if (nodeService.exists(caseRef) && nodeService.exists(additionalDataRef)) {
            ActionConditionUtils.getTransactionVariables().put(ADDITIONAL_DATA_VARIABLE, additionalDataRef);
            String rawEventRef = (String) nodeService.getProperty(additionalDataRef, EcosProcessModel.PROP_EVENT_REF);
            if (StringUtils.isBlank(rawEventRef)) {
                throw new RuntimeException("EventRef is not defined in props of user-action for caseRef=" + caseRef);
            }
            EventRef eventRef = EventRef.of(rawEventRef);
            caseActivityEventService.fireConcreteEvent(eventRef);
        }

        TransactionUtils.doAfterBehaviours(() -> removeAdditionalData(additionalDataRef));
    }

    private void removeAdditionalData(NodeRef additionalDataRef) {
        if (NodeUtils.exists(additionalDataRef, nodeService)) {
            nodeService.deleteNode(additionalDataRef);
        }
    }


}
