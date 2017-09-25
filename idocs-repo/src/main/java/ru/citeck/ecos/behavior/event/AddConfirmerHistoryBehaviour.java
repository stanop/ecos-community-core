package ru.citeck.ecos.behavior.event;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.behavior.ChainingJavaBehaviour;
import ru.citeck.ecos.behavior.event.trigger.UserActionEventTrigger;
import ru.citeck.ecos.event.EventPolicies;
import ru.citeck.ecos.history.HistoryService;
import ru.citeck.ecos.model.EventModel;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class AddConfirmerHistoryBehaviour implements EventPolicies.BeforeEventPolicy {

    private static final String HISTORY_EVENT_MESSAGE = "Добавлен согласующий - %s";
    private static final String HISTORY_EVENT_COMMENT = " с комментарием: \"%s\"";
    private static final String USER_EVENT_HISTORY_TYPE = "user.action";

    private PolicyComponent policyComponent;
    private HistoryService historyService;
    private NodeService nodeService;

    public void init() {
        policyComponent.bindClassBehaviour(EventPolicies.BeforeEventPolicy.QNAME,
                ContentModel.TYPE_CMOBJECT,
                new ChainingJavaBehaviour(this, "beforeEvent", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void beforeEvent(NodeRef eventRef) {
        if (nodeService.getType(eventRef).equals(EventModel.TYPE_USER_ACTION)) {
            NodeRef additionalData = (NodeRef) ActionConditionUtils.getTransactionVariables().get(UserActionEventTrigger.ADDITIONAL_DATA_VARIABLE);
            if (additionalData != null) {
                QName dataType = nodeService.getType(additionalData);
                NodeRef eventSource = RepoUtils.getFirstTargetAssoc(eventRef, EventModel.ASSOC_EVENT_SOURCE, nodeService);
                if (eventSource == null || !dataType.equals(EventModel.TYPE_ADDITIONAL_CONFIRMER)) {
                    return;
                }
                Map<QName, Serializable> eventProperties = new HashMap<>();
                eventProperties.put(HistoryModel.PROP_NAME, USER_EVENT_HISTORY_TYPE);
                eventProperties.put(HistoryModel.ASSOC_DOCUMENT, eventSource);
                eventProperties.put(HistoryModel.PROP_TASK_COMMENT, buildEventComment(additionalData));
                NodeRef historyEvent = historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
                if (historyEvent != null) {
                    transferAttributes(additionalData, historyEvent);
                }
            }
        }
    }

    private void transferAttributes(NodeRef additionalData, NodeRef historyEvent) {
        String comment = (String) nodeService.getProperty(additionalData, EventModel.PROP_COMMENT);
        nodeService.setProperty(historyEvent, EventModel.PROP_COMMENT, comment != null ? comment : "");
        NodeRef confirmerRef = RepoUtils.getFirstTargetAssoc(additionalData, EventModel.ASSOC_CONFIRMER, nodeService);
        nodeService.createAssociation(historyEvent, confirmerRef, EventModel.ASSOC_CONFIRMER);
    }

    private String buildEventComment(NodeRef additionalDataRef) {
        NodeRef confirmerRef = RepoUtils.getFirstTargetAssoc(additionalDataRef, EventModel.ASSOC_CONFIRMER, nodeService);
        QName confirmerType = nodeService.getType(confirmerRef);
        String confirmer;
        if (confirmerType.equals(ContentModel.TYPE_PERSON)) {
            String firstName = (String) nodeService.getProperty(confirmerRef, ContentModel.PROP_FIRSTNAME);
            String lastName = (String) nodeService.getProperty(confirmerRef, ContentModel.PROP_LASTNAME);
            confirmer = String.format("%s %s", firstName, lastName);
        } else {
            confirmer = (String) nodeService.getProperty(confirmerRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
        }
        String result = String.format(HISTORY_EVENT_MESSAGE, confirmer);
        String comment = (String) nodeService.getProperty(additionalDataRef, EventModel.PROP_COMMENT);
        if (StringUtils.isNotBlank(comment)) {
            result = result + String.format(HISTORY_EVENT_COMMENT, comment);
        }
        return result;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
