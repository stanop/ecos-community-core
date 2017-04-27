package ru.citeck.ecos.behavior.notification;

import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.notification.ICaseDocumentNotificationSender;

import java.util.List;
import java.util.Map;

public abstract class AbstractICaseDocumentNotificationBehaviour {

    protected DictionaryService dictionaryService;
    protected CaseStatusService caseStatusService;
    protected CaseActivityService caseActivityService;
    protected PolicyComponent policyComponent;
    public NodeService nodeService;

    protected ICaseDocumentNotificationSender sender;
    protected Map<String, List<String>> recipients;

    protected List<String> includeStatuses;
    protected List<String> excludeStatuses;

    protected String notificationType;
    protected String subjectTemplate;

    protected int order;

    private boolean enabled = true;

    public boolean isActive(NodeRef caseRef) {
        if (!isEnabled()) return false;

        String status = caseStatusService.getStatus(caseRef);

        return (includeStatuses == null || includeStatuses.contains(status))
            && (excludeStatuses == null || !excludeStatuses.contains(status));
    }

    public boolean isEnabled() {
        return enabled;
    }

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

    public void setIncludeStatuses(List<String> includeStatuses) {
        this.includeStatuses = includeStatuses;
    }

    public void setExcludeStatuses(List<String> excludeStatuses) {
        this.excludeStatuses = excludeStatuses;
    }

    public void setCaseStatusService(CaseStatusService caseStatusService) {
        this.caseStatusService = caseStatusService;
    }

    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
