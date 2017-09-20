package ru.citeck.ecos.behavior.event.trigger;

import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import ru.citeck.ecos.behavior.ChainingJavaBehaviour;
import ru.citeck.ecos.event.EventService;
import ru.citeck.ecos.icase.activity.CaseActivityPolicies;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.ICaseEventModel;
import ru.citeck.ecos.model.StagesModel;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CaseActivityEventTrigger implements CaseActivityPolicies.OnCaseActivityStartedPolicy,
                                                 CaseActivityPolicies.OnCaseActivityStoppedPolicy {

    private static final String ACTIVITY_EVENT_TRIGGER_DATA_KEY = "case-activity-event-trigger-data";

    private CaseActivityService caseActivityService;
    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private EventService eventService;
    private NodeService nodeService;

    public void init() {
        policyComponent.bindClassBehaviour(CaseActivityPolicies.OnCaseActivityStartedPolicy.QNAME,
                ActivityModel.TYPE_ACTIVITY,
                new ChainingJavaBehaviour(this, "onCaseActivityStarted", Behaviour.NotificationFrequency.EVERY_EVENT));
        policyComponent.bindClassBehaviour(CaseActivityPolicies.OnCaseActivityStoppedPolicy.QNAME,
                ActivityModel.TYPE_ACTIVITY,
                new ChainingJavaBehaviour(this, "onCaseActivityStopped", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void onCaseActivityStarted(NodeRef activityRef) {
        NodeRef document = caseActivityService.getDocument(activityRef);

        TransactionData data = getTransactionData();
        boolean isDataOwner = false;
        if (!data.hasOwner) {
            data.hasOwner = isDataOwner = true;
        }

        eventService.fireEvent(activityRef, document, ICaseEventModel.CONSTR_ACTIVITY_STARTED);

        if (isDataOwner) {
            tryToFireStageChildrenStoppedEvents(data, document);
            data.hasOwner = false;
        }
    }

    @Override
    public void onCaseActivityStopped(NodeRef activityRef) {
        NodeRef document = caseActivityService.getDocument(activityRef);

        TransactionData data = getTransactionData();
        boolean isDataOwner = false;
        if (!data.hasOwner) {
            data.hasOwner = isDataOwner = true;
        }

        eventService.fireEvent(activityRef, document, ICaseEventModel.CONSTR_ACTIVITY_STOPPED);

        NodeRef parent = nodeService.getPrimaryParent(activityRef).getParentRef();
        if (parent != null && dictionaryService.isSubClass(nodeService.getType(parent), StagesModel.TYPE_STAGE)) {
            data.stagesWithStoppedChildren.add(parent);
        }

        if (isDataOwner) {
            tryToFireStageChildrenStoppedEvents(data, document);
            data.hasOwner = false;
        }
    }

    private void tryToFireStageChildrenStoppedEvents(TransactionData data, NodeRef document) {
        Queue<NodeRef> stages = new ArrayDeque<>(data.stagesWithStoppedChildren);
        data.stagesWithStoppedChildren.clear();

        while (!stages.isEmpty()) {
            NodeRef stage = stages.poll();
            if (!caseActivityService.hasActiveChildren(stage)) {
                eventService.fireEvent(stage, document, ICaseEventModel.CONSTR_STAGE_CHILDREN_STOPPED);
            }
            for (NodeRef st : data.stagesWithStoppedChildren) {
                if (!stages.contains(st)) {
                    stages.add(st);
                }
            }
            data.stagesWithStoppedChildren.clear();
        }
    }

    private TransactionData getTransactionData() {
        TransactionData data = AlfrescoTransactionSupport.getResource(ACTIVITY_EVENT_TRIGGER_DATA_KEY);
        if (data == null) {
            data = new TransactionData();
            AlfrescoTransactionSupport.bindResource(ACTIVITY_EVENT_TRIGGER_DATA_KEY, data);
        }
        return data;
    }

    private static class TransactionData {
        boolean hasOwner = false;
        Set<NodeRef> stagesWithStoppedChildren = new HashSet<>();
    }

    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
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
