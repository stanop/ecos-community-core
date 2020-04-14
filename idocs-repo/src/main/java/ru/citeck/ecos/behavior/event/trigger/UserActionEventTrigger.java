package ru.citeck.ecos.behavior.event.trigger;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.behavior.ChainingJavaBehaviour;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import ru.citeck.ecos.icase.activity.dto.EventRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventService;
import ru.citeck.ecos.icase.activity.service.alfresco.CaseActivityPolicies;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.EventModel;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.AlfActivityUtils;
import ru.citeck.ecos.utils.RepoUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
@Component
@DependsOn("idocs.dictionaryBootstrap")
public class UserActionEventTrigger implements OnCreateChildAssociationPolicy,
        CaseActivityPolicies.OnCaseActivityResetPolicy {

    public static final String ADDITIONAL_DATA_VARIABLE = "additionalData";

    private static final int ORDER = 60;

    private CaseActivityEventService caseActivityEventService;
    private AlfActivityUtils alfActivityUtils;
    private PolicyComponent policyComponent;
    private DictionaryService dictionaryService;
    private NodeService nodeService;

    @Autowired
    public UserActionEventTrigger(ServiceRegistry serviceRegistry) {
        this.caseActivityEventService = (CaseActivityEventService) serviceRegistry
                .getService(CiteckServices.CASE_ACTIVITY_EVENT_SERVICE);
        this.alfActivityUtils = (AlfActivityUtils) serviceRegistry.getService(CiteckServices.ALF_ACTIVITY_UTILS);
        this.policyComponent = serviceRegistry.getPolicyComponent();
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    @PostConstruct
    public void init() {
        policyComponent.bindAssociationBehaviour(
                OnCreateChildAssociationPolicy.QNAME,
                EventModel.TYPE_USER_ACTION,
                EventModel.ASSOC_ADDITIONAL_DATA_ITEMS,
                new OrderedBehaviour(this,
                        "onCreateChildAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                        ORDER));

        policyComponent.bindClassBehaviour(CaseActivityPolicies.OnCaseActivityResetPolicy.QNAME,
                ActivityModel.TYPE_ACTIVITY,
                new ChainingJavaBehaviour(this, "onCaseActivityReset", Behaviour.NotificationFrequency.EVERY_EVENT));
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

    @Override
    public void onCaseActivityReset(NodeRef activityRef) {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(activityRef);
        if (childAssocs == null) {
            return;
        }
        for (ChildAssociationRef assoc : childAssocs) {
            QName type = nodeService.getType(assoc.getChildRef());
            if (dictionaryService.isSubClass(type, EventModel.TYPE_USER_ACTION)) {
                List<NodeRef> data = RepoUtils.getChildrenByAssoc(assoc.getChildRef(),
                        EventModel.ASSOC_ADDITIONAL_DATA_ITEMS,
                        nodeService);
                Map<String, Object> vars = ActionConditionUtils.getTransactionVariables();
                Object additionalData = vars.get(UserActionEventTrigger.ADDITIONAL_DATA_VARIABLE);
                for (NodeRef dataItem : data) {
                    if (!dataItem.equals(additionalData)) {
                        RepoUtils.deleteNode(dataItem, nodeService);
                    }
                }
            }
        }
    }
}
