package ru.citeck.ecos.icase.activity.service.alfresco;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.action.ConditionDAO;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.EventRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventDelegate;
import ru.citeck.ecos.model.EventModel;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.AlfActivityUtils;
import ru.citeck.ecos.utils.DictionaryUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Component
@DependsOn("idocs.dictionaryBootstrap")
public class AlfrescoCaseActivityEventDelegate implements CaseActivityEventDelegate {

    public static final String TRANSACTION_EVENT_VARIABLE = "event";

    private ClassPolicyDelegate<EventPolicies.OnEventPolicy> onEventDelegate;
    private ClassPolicyDelegate<EventPolicies.BeforeEventPolicy> beforeEventDelegate;

    private AlfActivityUtils alfActivityUtils;

    private PolicyComponent policyComponent;
    private NodeService nodeService;

    private ActionService actionService;
    private ConditionDAO conditionDAO;

    @Autowired
    public AlfrescoCaseActivityEventDelegate(ServiceRegistry serviceRegistry) {
        this.alfActivityUtils = (AlfActivityUtils) serviceRegistry.getService(CiteckServices.ALF_ACTIVITY_UTILS);
        this.policyComponent = serviceRegistry.getPolicyComponent();
        this.nodeService = serviceRegistry.getNodeService();
        this.actionService = serviceRegistry.getActionService();
        this.conditionDAO = (ConditionDAO) serviceRegistry.getService(CiteckServices.CONDITION_DAO);
    }

    @PostConstruct
    public void init() {
        onEventDelegate = policyComponent.registerClassPolicy(EventPolicies.OnEventPolicy.class);
        beforeEventDelegate = policyComponent.registerClassPolicy(EventPolicies.BeforeEventPolicy.class);
    }

    @Override
    public void fireEvent(ActivityRef activityRef, String eventType) {
        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        NodeRef caseNodeRef = RecordsUtils.toNodeRef(activityRef.getProcessId());
        fireEventImpl(activityNodeRef, caseNodeRef, eventType);
    }

    private void fireEventImpl(NodeRef eventSourceRef, NodeRef caseRef, String eventType) {
        if (!nodeService.exists(eventSourceRef)) {
            return;
        }

        if (log.isDebugEnabled()) {
            String sourceTitle = (String) nodeService.getProperty(eventSourceRef, ContentModel.PROP_TITLE);
            if (sourceTitle == null) {
                sourceTitle = (String) nodeService.getProperty(eventSourceRef, ContentModel.PROP_NAME);
            }
            log.debug(String.format("Try to fire event. Event type: '%s', Event source: '%s'", eventType, sourceTitle));
        }

        EventPolicies.BeforeEventPolicy beforePolicy = getBeforePolicy(eventSourceRef);
        EventPolicies.OnEventPolicy onPolicy = getOnPolicy(eventSourceRef);
        List<NodeRef> events = getEvents(eventSourceRef, eventType);

        for (NodeRef event : events) {
            fireConcreteEventImpl(event, caseRef, beforePolicy, onPolicy);
        }
    }

    @Override
    public void fireConcreteEvent(EventRef eventRef) {
        NodeRef eventNodeRef = alfActivityUtils.getEventNodeRef(eventRef);
        NodeRef caseRef = RecordsUtils.toNodeRef(eventRef.getProcessId());
        EventPolicies.BeforeEventPolicy beforePolicy = getBeforePolicy(caseRef);
        EventPolicies.OnEventPolicy onPolicy = getOnPolicy(caseRef);
        fireConcreteEventImpl(eventNodeRef, caseRef, beforePolicy, onPolicy);
    }

    private void fireConcreteEventImpl(NodeRef eventRef, NodeRef conditionContextRef,
                                       EventPolicies.BeforeEventPolicy beforePolicy,
                                       EventPolicies.OnEventPolicy onPolicy) {

        ActionConditionUtils.getTransactionVariables().put(TRANSACTION_EVENT_VARIABLE, eventRef);
        if (checkConditionsImpl(eventRef, conditionContextRef)) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Event '%s' was fired.", eventRef));
            }
            beforePolicy.beforeEvent(eventRef);
            onPolicy.onEvent(eventRef);
        }
    }

    @Override
    public boolean checkConditions(EventRef eventRef) {
        NodeRef eventNodeRef = alfActivityUtils.getEventNodeRef(eventRef);
        NodeRef caseRef = RecordsUtils.toNodeRef(eventRef.getProcessId());
        return checkConditionsImpl(eventNodeRef, caseRef);
    }

    private boolean checkConditionsImpl(NodeRef eventRef, NodeRef conditionContextRef) {
        List<ActionCondition> eventConditions = conditionDAO.readConditions(eventRef, EventModel.ASSOC_CONDITIONS);
        ActionConditionUtils.getTransactionVariables().put(TRANSACTION_EVENT_VARIABLE, eventRef);
        for (ActionCondition condition : eventConditions) {
            if (!actionService.evaluateActionCondition(condition, conditionContextRef)) {
                return false;
            }
        }
        return true;
    }

    private List<NodeRef> getEvents(NodeRef nodeRef, String eventType) {
        List<NodeRef> events = new ArrayList<>();
        List<AssociationRef> eventsAssocs = nodeService.getSourceAssocs(nodeRef, EventModel.ASSOC_EVENT_SOURCE);
        for (AssociationRef assoc : eventsAssocs) {
            NodeRef eventRef = assoc.getSourceRef();
            if (eventType.equals(nodeService.getProperty(eventRef, EventModel.PROP_TYPE))) {
                events.add(eventRef);
            }
        }
        return events;
    }

    private EventPolicies.OnEventPolicy getOnPolicy(NodeRef nodeRef) {
        List<QName> classes = DictionaryUtils.getNodeClassNames(nodeRef, nodeService);
        return onEventDelegate.get(new HashSet<>(classes));
    }

    private EventPolicies.BeforeEventPolicy getBeforePolicy(NodeRef nodeRef) {
        List<QName> classes = DictionaryUtils.getNodeClassNames(nodeRef, nodeService);
        return beforeEventDelegate.get(new HashSet<>(classes));
    }

}
