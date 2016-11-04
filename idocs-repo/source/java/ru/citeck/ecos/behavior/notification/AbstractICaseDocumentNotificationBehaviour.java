package ru.citeck.ecos.behavior.notification;

import org.alfresco.repo.policy.PolicyComponent;
import ru.citeck.ecos.notification.ICaseDocumentNotificationSender;

import java.util.List;
import java.util.Map;

public abstract class AbstractICaseDocumentNotificationBehaviour {

    protected PolicyComponent policyComponent;
    protected ICaseDocumentNotificationSender sender;
    protected Map<String, List<String>> recipients;
    protected String notificationType;
    protected String subjectTemplate;
    protected int order;

    public void setSender(ICaseDocumentNotificationSender sender) {
        this.sender = sender;
    }

    public void setRecipients(Map<String, List<String>> recipients) {
        this.recipients = recipients;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public void setSubjectTemplate(String subjectTemplate) {
        this.subjectTemplate = subjectTemplate;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }
}
