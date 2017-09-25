package ru.citeck.ecos.event;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.action.ConditionDAO;
import ru.citeck.ecos.model.EventModel;
import ru.citeck.ecos.utils.DictionaryUtils;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public class EventServiceImpl implements EventService {

    public static final Log log = LogFactory.getLog(EventService.class);

    public static final String TRANSACTION_EVENT_VARIABLE = "event";

    private ClassPolicyDelegate<EventPolicies.OnEventPolicy> onEventDelegate;
    private ClassPolicyDelegate<EventPolicies.BeforeEventPolicy> beforeEventDelegate;

    protected PolicyComponent policyComponent;
    protected NodeService nodeService;

    protected ActionService actionService;
    protected ConditionDAO conditionDAO;

    public void init() throws Exception {
        onEventDelegate = policyComponent.registerClassPolicy(EventPolicies.OnEventPolicy.class);
        beforeEventDelegate = policyComponent.registerClassPolicy(EventPolicies.BeforeEventPolicy.class);
    }

    @Override
    public void fireEvent(NodeRef nodeRef, String eventType) {
        fireEvent(nodeRef, nodeRef, eventType);
    }

    @Override
    public void fireEvent(NodeRef nodeRef, NodeRef conditionContextRef, String eventType) {
        if (!nodeService.exists(nodeRef)) return;

        if (log.isDebugEnabled()) {
            String sourceTitle = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
            if(sourceTitle == null) sourceTitle = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            log.debug(String.format("Try to fire event. Event type: '%s', Event source: '%s'", eventType, sourceTitle));
        }

        EventPolicies.BeforeEventPolicy beforePolicy = getBeforePolicy(nodeRef);
        EventPolicies.OnEventPolicy onPolicy = getOnPolicy(nodeRef);
        List<NodeRef> events = getEvents(nodeRef, eventType);

        for (NodeRef event : events) {
            fireConcreteEvent(event, conditionContextRef, beforePolicy, onPolicy);
        }
    }

    @Override
    public void fireConcreteEvent(NodeRef eventRef) {
        List<AssociationRef> eventSource = nodeService.getTargetAssocs(eventRef, EventModel.ASSOC_EVENT_SOURCE);
        if (eventSource != null && eventSource.size() > 0) {
            fireConcreteEvent(eventRef, eventSource.get(0).getTargetRef());
        }
    }

    @Override
    public void fireConcreteEvent(NodeRef eventRef, NodeRef conditionContextRef) {
        fireConcreteEvent(eventRef, conditionContextRef, getBeforePolicy(conditionContextRef),
                                                         getOnPolicy(conditionContextRef));
    }

    private void fireConcreteEvent(NodeRef eventRef, NodeRef conditionContextRef, EventPolicies.BeforeEventPolicy beforePolicy,
                                                                                  EventPolicies.OnEventPolicy onPolicy) {
        ActionConditionUtils.getTransactionVariables().put(TRANSACTION_EVENT_VARIABLE, eventRef);
        if (checkConditions(eventRef, conditionContextRef)) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Event '%s' was fired.", eventRef));
            }
            beforePolicy.beforeEvent(eventRef);
            onPolicy.onEvent(eventRef);
        }
    }

    @Override
    public boolean checkConditions(NodeRef eventRef) {
        return checkConditions(eventRef, eventRef);
    }

    @Override
    public boolean checkConditions(NodeRef eventRef, NodeRef conditionContextRef) {
        List<ActionCondition> eventConditions = conditionDAO.readConditions(eventRef, EventModel.ASSOC_CONDITIONS);
        ActionConditionUtils.getTransactionVariables().put(TRANSACTION_EVENT_VARIABLE, eventRef);
        for(ActionCondition condition : eventConditions) {
            if (!actionService.evaluateActionCondition(condition, conditionContextRef)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<NodeRef> getEvents(NodeRef nodeRef, String eventType) {
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

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setConditionDAO(ConditionDAO conditionDAO) {
        this.conditionDAO = conditionDAO;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }
}