package ru.citeck.ecos.behavior.event.trigger;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventService;
import ru.citeck.ecos.model.EcosProcessModel;
import ru.citeck.ecos.model.EventModel;

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
        NodeRef eventNodeRef = childAssociationRef.getParentRef();
        NodeRef additionalDataRef = childAssociationRef.getChildRef();
        if (nodeService.exists(eventNodeRef) && nodeService.exists(additionalDataRef)) {
            ActionConditionUtils.getTransactionVariables().put(ADDITIONAL_DATA_VARIABLE, additionalDataRef);
        }
    }


}
