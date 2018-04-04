package ru.citeck.ecos.behavior;

import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.behavior.DiadocAttachmentsNotificationBehaviour.EventData;
import ru.citeck.ecos.role.CaseRoleService;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public class DiadocAttachmentsNotificationBehaviour extends DiadocAttachmentsStatusBehaviour<EventData> {

    protected static final String ARG_DOCUMENT = "document";
    protected static final String ARG_EVENT_DATA = "eventData";
    protected static final String ARG_ATTACHMENT = "attachment";
    protected static final String ARG_STATUS_TRANSITION = "statusTransition";

    private String template;

    private String recipients;
    private boolean enabled = true;

    private CaseRoleService caseRoleService;
    private DictionaryService dictionaryService;
    private TransactionService transactionService;
    private NotificationService notificationService;

    @Override
    protected void processTransition(NodeRef parentRef, NodeRef attachmentRef, Transition transition, EventData data) {
        if (enabled) {
            final NotificationContext notificationContext = new NotificationContext();
            Map<String, Serializable> args = prepareArgs(parentRef, attachmentRef, transition, data);

            for (String to : getRecipients(parentRef)) {
                notificationContext.addTo(to);
            }
            notificationContext.setSubject(evaluateScript(data.getSubjectScript(), args));
            notificationContext.setBodyTemplate(template);
            notificationContext.setAsyncNotification(false);
            notificationContext.setTemplateArgs(args);

            AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
                @Override
                public void afterCommit() {
                    RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
                    helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
                        @Override
                        public Void execute() throws Throwable {
                            notificationService.sendNotification(EMailNotificationProvider.NAME, notificationContext);
                            return null;
                        }
                    }, true, true);
                }
            });
        }
    }

    private String evaluateScript(String script, Map<String, Serializable> args) {
        if (StringUtils.isNotBlank(script)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> model = (Map) args;
            return String.valueOf(scriptService.executeScriptString(script, model));
        } else {
            return "";
        }
    }

    protected Map<String, Serializable> prepareArgs(NodeRef parentRef, NodeRef attachmentRef,
                                                  Transition transition, EventData data) {
        Map<String, Serializable> args = new HashMap<>();
        args.put(ARG_DOCUMENT, parentRef);
        args.put(ARG_ATTACHMENT, attachmentRef);
        args.put(ARG_STATUS_TRANSITION, transition);
        args.put(ARG_EVENT_DATA, data);
        return args;
    }

    private List<String> getRecipients(NodeRef caseRef) {
        List<String> result = new ArrayList<>();
        String[] roles = recipients.split(",");
        for (String role : roles) {
            Set<NodeRef> assignees = caseRoleService.getAssignees(caseRef, role);
            for (NodeRef assignee : assignees) {
                result.add(RepoUtils.getAuthorityName(assignee, nodeService, dictionaryService));
            }
        }
        return result;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setCaseRoleService(CaseRoleService caseRoleService) {
        this.caseRoleService = caseRoleService;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public static class EventData implements Serializable {

        private String subjectScript;
        private String emailType;

        public String getEmailType() {
            return emailType;
        }

        public void setEmailType(String emailType) {
            this.emailType = emailType;
        }

        public String getSubjectScript() {
            return subjectScript;
        }

        public void setSubjectScript(String subjectScript) {
            this.subjectScript = subjectScript;
        }
    }
}
