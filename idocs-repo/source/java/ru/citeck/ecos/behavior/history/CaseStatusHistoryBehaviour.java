package ru.citeck.ecos.behavior.history;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.history.HistoryService;
import ru.citeck.ecos.icase.CaseStatusPolicies;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.model.ICaseModel;

import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CaseStatusHistoryBehaviour implements CaseStatusPolicies.OnCaseStatusChangedPolicy {

    private static final String KEY_STATUS_BEFORE = "statusBefore";
    private static final String KEY_STATUS_AFTER = "statusAfter";
    private static final String KEY_DOCUMENT = "document";

    private static final String ANY_STATUS = "ANY";
    private static final String HISTORY_TYPE = "status.changed";

    private PolicyComponent policyComponent;
    private HistoryService historyService;
    private ScriptService scriptService;
    private NodeService nodeService;

    private String messageScript;

    private List<StatusTransition> transitions;

    private int order = 80;

    public void init() {
        policyComponent.bindClassBehaviour(
                CaseStatusPolicies.OnCaseStatusChangedPolicy.QNAME, ICaseModel.TYPE_CASE_STATUS,
                new OrderedBehaviour(this, "onCaseStatusChanged", NotificationFrequency.TRANSACTION_COMMIT, order)
        );
    }

    @Override
    public void onCaseStatusChanged(NodeRef caseRef, NodeRef caseStatusBefore, NodeRef caseStatusAfter) {

        if (!nodeService.exists(caseRef)) return;

        if (isInterestedTransition(caseRef, caseStatusBefore, caseStatusAfter)) {
            String comment = buildEventComment(caseRef, caseStatusBefore, caseStatusAfter);
            Map<QName, Serializable> eventProperties = new HashMap<>();
            eventProperties.put(HistoryModel.PROP_NAME, HISTORY_TYPE);
            eventProperties.put(HistoryModel.ASSOC_DOCUMENT, caseRef);
            eventProperties.put(HistoryModel.PROP_TASK_COMMENT, comment);
            historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
        }
    }

    private String buildEventComment(NodeRef caseRef, NodeRef caseStatusBefore, NodeRef caseStatusAfter) {

        final Map<String,Object> model = new HashMap<>(3);
        model.put(KEY_DOCUMENT, caseRef);
        model.put(KEY_STATUS_AFTER, caseStatusAfter);
        model.put(KEY_STATUS_BEFORE, caseStatusBefore);

        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<String>() {
            @Override
            public String doWork() throws Exception {
            return String.valueOf(scriptService.executeScriptString(messageScript, model));
            }
        });
    }

    private boolean isInterestedTransition(NodeRef caseRef, NodeRef before, NodeRef after) {

        QName className = nodeService.getType(caseRef);
        String beforeName = before != null ? (String) nodeService.getProperty(before, ContentModel.PROP_NAME) : null;
        String afterName = after != null ? (String) nodeService.getProperty(after, ContentModel.PROP_NAME) : null;

        for (StatusTransition transition : transitions) {
            if (transition.isMatch(className, beforeName, afterName)) {
                return true;
            }
        }
        return false;
    }

    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
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

    public void setTransitions(List<StatusTransition> transitions) {
        this.transitions = transitions;
    }

    public void setMessageScript(String messageScript) {
        this.messageScript = messageScript;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    interface StatusTransition {
        boolean isMatch(QName className, String fromStatus, String toStatus);
    }

    public static class SimpleTransition implements StatusTransition {
        private QName className;
        private String fromStatus;
        private String toStatus;

        public boolean isMatch(QName className, String fromStatus, String toStatus) {
            return (this.className.equals(className))
                   && (this.fromStatus.equals(ANY_STATUS) || this.fromStatus.equals(fromStatus))
                   && (this.toStatus.equals(ANY_STATUS) || this.toStatus.equals(toStatus));
        }

        public void setClassName(QName className) {
            this.className = className;
        }

        public void setFromStatus(String fromStatus) {
            this.fromStatus = fromStatus;
        }

        public void setToStatus(String toStatus) {
            this.toStatus = toStatus;
        }
    }

    public static class MultipleTargetsTransition implements StatusTransition {
        private QName className;
        private String fromStatus;
        private List<String> toStatuses;

        public boolean isMatch(QName className, String fromStatus, String toStatus) {
            return (this.className.equals(className))
                   && (this.fromStatus.equals(ANY_STATUS) || this.fromStatus.equals(fromStatus))
                   && (this.toStatuses.contains(ANY_STATUS) || this.toStatuses.contains(toStatus));
        }

        public void setClassName(QName className) {
            this.className = className;
        }

        public void setFromStatus(String fromStatus) {
            this.fromStatus = fromStatus;
        }

        public void setToStatuses(List<String> toStatuses) {
            this.toStatuses = toStatuses;
        }
    }
}
