package ru.citeck.ecos.behavior.event.trigger;

import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang.mutable.MutableInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.behavior.ChainingJavaBehaviour;
import ru.citeck.ecos.event.EventService;
import ru.citeck.ecos.icase.activity.service.alfresco.CaseActivityPolicies;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.ICaseEventModel;
import ru.citeck.ecos.model.StagesModel;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.AlfActivityUtils;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@DependsOn("idocs.dictionaryBootstrap")
public class CaseActivityEventTrigger implements CaseActivityPolicies.OnCaseActivityStartedPolicy,
                                                 CaseActivityPolicies.OnCaseActivityStoppedPolicy {

    private static final String ACTIVITY_EVENT_TRIGGER_DATA_KEY = "case-activity-event-trigger-data";
    private static final String STAGE_CHILDREN_COMPLETED_TXN_KEY = "case-activity-stage-children-completed";

    private static final int STAGE_COMPLETE_LIMIT = 40;

    private CaseActivityService caseActivityService;
    private AlfActivityUtils alfActivityUtils;
    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private EventService eventService;
    private NodeService nodeService;

    @Autowired
    public CaseActivityEventTrigger(ServiceRegistry serviceRegistry) {
        this.caseActivityService = (CaseActivityService) serviceRegistry.getService(CiteckServices.CASE_ACTIVITY_SERVICE);
        this.alfActivityUtils = (AlfActivityUtils) serviceRegistry.getService(CiteckServices.ALF_ACTIVITY_UTILS);
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.policyComponent = serviceRegistry.getPolicyComponent();
        this.eventService = (EventService) serviceRegistry.getService(CiteckServices.EVENT_SERVICE);
        this.nodeService = serviceRegistry.getNodeService();
    }

    @PostConstruct
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
        RecordRef documentId = alfActivityUtils.getDocumentId(activityRef);
        NodeRef documentNodeRef = RecordsUtils.toNodeRef(documentId);

        TransactionData data = getTransactionData();
        boolean isDataOwner = false;
        if (!data.hasOwner) {
            data.hasOwner = isDataOwner = true;
        }

        eventService.fireEvent(activityRef, documentNodeRef, ICaseEventModel.CONSTR_ACTIVITY_STARTED);

        if (dictionaryService.isSubClass(nodeService.getType(activityRef), StagesModel.TYPE_STAGE)) {
            Integer version = (Integer) nodeService.getProperty(activityRef, ActivityModel.PROP_TYPE_VERSION);
            if (version != null && version >= 1) {
                data.stagesToTryComplete.add(activityRef);
            }
        }

        if (isDataOwner) {
            tryToFireStageChildrenStoppedEvents(data, documentNodeRef);
            data.hasOwner = false;
        }
    }

    @Override
    public void onCaseActivityStopped(NodeRef activityRef) {
        RecordRef documentId = alfActivityUtils.getDocumentId(activityRef);
        NodeRef documentNodeRef = RecordsUtils.toNodeRef(documentId);

        TransactionData data = getTransactionData();
        boolean isDataOwner = false;
        if (!data.hasOwner) {
            data.hasOwner = isDataOwner = true;
        }

        eventService.fireEvent(activityRef, documentNodeRef, ICaseEventModel.CONSTR_ACTIVITY_STOPPED);

        NodeRef parent = nodeService.getPrimaryParent(activityRef).getParentRef();
        if (parent != null && dictionaryService.isSubClass(nodeService.getType(parent), StagesModel.TYPE_STAGE)) {
            data.stagesToTryComplete.add(parent);
        }

        if (isDataOwner) {
            tryToFireStageChildrenStoppedEvents(data, documentNodeRef);
            data.hasOwner = false;
        }
    }

    private void tryToFireStageChildrenStoppedEvents(TransactionData data, NodeRef document) {

        Map<NodeRef, MutableInt> completedStages =
                TransactionalResourceHelper.getMap(STAGE_CHILDREN_COMPLETED_TXN_KEY);

        Queue<NodeRef> stages = new ArrayDeque<>(data.stagesToTryComplete);
        data.stagesToTryComplete.clear();

        while (!stages.isEmpty()) {

            NodeRef stage = stages.poll();

            ActivityRef stageRef = alfActivityUtils.composeActivityRef(stage);
            if (!caseActivityService.hasActiveChildren(stageRef)) {

                MutableInt completedCounter = completedStages.computeIfAbsent(stage, s -> new MutableInt(0));
                completedCounter.increment();

                if (completedCounter.intValue() > STAGE_COMPLETE_LIMIT) {
                    throw new IllegalStateException("Stage " + stage + " completed more than " + STAGE_COMPLETE_LIMIT +
                                                    " times. Seems it is a infinite loop. Document: " + document);
                }
                eventService.fireEvent(stage, document, ICaseEventModel.CONSTR_STAGE_CHILDREN_STOPPED);
            }
            for (NodeRef st : data.stagesToTryComplete) {
                if (!stages.contains(st)) {
                    stages.add(st);
                }
            }
            data.stagesToTryComplete.clear();
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
        Set<NodeRef> stagesToTryComplete = new HashSet<>();
    }
}
