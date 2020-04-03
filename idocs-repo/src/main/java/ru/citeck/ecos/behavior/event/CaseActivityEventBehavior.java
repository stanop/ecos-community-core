package ru.citeck.ecos.behavior.event;

import org.alfresco.model.ContentModel;
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
import ru.citeck.ecos.behavior.event.trigger.UserActionEventTrigger;
import ru.citeck.ecos.icase.activity.service.alfresco.EventPolicies;
import ru.citeck.ecos.icase.activity.service.alfresco.CaseActivityPolicies;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.EventModel;
import ru.citeck.ecos.model.ICaseEventModel;
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
public class CaseActivityEventBehavior implements EventPolicies.OnEventPolicy,
                                                  CaseActivityPolicies.OnCaseActivityResetPolicy {

    private CaseActivityService caseActivityService;
    private AlfActivityUtils alfActivityUtils;
    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    @Autowired
    public CaseActivityEventBehavior(ServiceRegistry serviceRegistry) {
        this.caseActivityService = (CaseActivityService) serviceRegistry.getService(CiteckServices.CASE_ACTIVITY_SERVICE);
        this.alfActivityUtils = (AlfActivityUtils) serviceRegistry.getService(CiteckServices.ALF_ACTIVITY_UTILS);
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.policyComponent = serviceRegistry.getPolicyComponent();
        this.nodeService = serviceRegistry.getNodeService();
    }

    @PostConstruct
    public void init() {
        policyComponent.bindClassBehaviour(EventPolicies.OnEventPolicy.QNAME,
                ContentModel.TYPE_CMOBJECT,
                new ChainingJavaBehaviour(this, "onEvent", Behaviour.NotificationFrequency.EVERY_EVENT));

        policyComponent.bindClassBehaviour(CaseActivityPolicies.OnCaseActivityResetPolicy.QNAME,
                ActivityModel.TYPE_ACTIVITY,
                new ChainingJavaBehaviour(this, "onCaseActivityReset", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void onEvent(NodeRef eventRef) {
        if(!nodeService.exists(eventRef)) return;

        ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(eventRef);
        QName assocType = parentAssocRef.getTypeQName();

        NodeRef activityNodeRef = parentAssocRef.getParentRef();
        ActivityRef activityRef = alfActivityUtils.composeActivityRef(activityNodeRef);

        if (assocType.equals(ICaseEventModel.ASSOC_ACTIVITY_START_EVENTS)) {
            caseActivityService.startActivity(activityRef);
        } else if (assocType.equals(ICaseEventModel.ASSOC_ACTIVITY_END_EVENTS)) {
            caseActivityService.stopActivity(activityRef);
        } else if (assocType.equals(ICaseEventModel.ASSOC_ACTIVITY_RESET_EVENTS)) {
            caseActivityService.reset(activityRef);
        } else if (assocType.equals(ICaseEventModel.ASSOC_ACTIVITY_RESTART_EVENTS)) {
            caseActivityService.reset(activityRef);
            caseActivityService.startActivity(activityRef);
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

    @Autowired
    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }

    @Autowired
    public void setAlfActivityUtils(AlfActivityUtils alfActivityUtils) {
        this.alfActivityUtils = alfActivityUtils;
    }

    @Autowired
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Autowired
    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
