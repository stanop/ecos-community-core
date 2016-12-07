package ru.citeck.ecos.notification;


import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.notification.utils.RecipientsUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author Roman Makarskiy
 */
public class ICaseDocumentNotificationSender extends DocumentNotificationSender {

    private NodeService nodeService;
    private NamespaceService namespaceService;
    private TemplateService templateService;

    private String nodeVariable;
    private String templateEngine;
    private String subjectTemplate;
    private String notificationType;
    private NodeRef targetRef;
    private Map<String, List<String>> recipients;

    private static final String ARG_TARGET_REF = "targetRef";
    private static final String ARG_NOTIFICATION_TYPE = "notificationType";
    private static final String ASSOC_RECIPIENTS_KEY = "assocRecipients";
    private static final String RECIPIENTS_FROM_ROLE_KEY = "recipientsFromRole";

    private static Log logger = LogFactory.getLog(ICaseDocumentNotificationSender.class);

    @Override
    protected Collection<String> getNotificationRecipients(NodeRef item) {
        List<QName> assocRecipients = convertStringToQNameList(recipients.get(ASSOC_RECIPIENTS_KEY));
        List<String> assocRecipientsFromICaseRole = recipients.get(RECIPIENTS_FROM_ROLE_KEY);
        Set<String> finalRecipients = new HashSet<>();
        finalRecipients.addAll(super.getNotificationRecipients(item));

        if (assocRecipients != null) {
            Set<String> assocRecipient = RecipientsUtils.getRecipientFromNodeAssoc(assocRecipients, item, nodeService);
            if (!assocRecipient.isEmpty()) {
                finalRecipients.addAll(assocRecipient);
            }
        }

        if (assocRecipientsFromICaseRole != null) {
            Set<String> roleRecipients = RecipientsUtils.getRecipientsFromRole(assocRecipientsFromICaseRole, item,
                    nodeService);
            if (!roleRecipients.isEmpty()) {
                finalRecipients.addAll(roleRecipients);
            }
        }

        return finalRecipients;
    }

    @Override
    protected String getNotificationSubject(NodeRef item) {
        String subject = "";
        if (subjectTemplate != null) {
            HashMap<String, Object> model = new HashMap<>(1);
            model.put(nodeVariable, item);
            subject = templateService.processTemplateString(templateEngine, subjectTemplate, model);
        }
        return subject;
    }

    public void sendNotification(NodeRef sourceRef, NodeRef targetRef, Map<String, List<String>> recipients,
                                 String notificationType, String subjectTemplate, boolean afterCommit) {
        this.targetRef = targetRef;
        this.recipients = recipients;
        this.notificationType = notificationType;
        this.subjectTemplate = subjectTemplate;
        super.sendNotification(sourceRef, afterCommit);

        if (logger.isDebugEnabled()) {
            logger.debug("Send notification - "
                    + "\nsource nodeRef: " + sourceRef
                    + "\ntarget nodeRef: " + targetRef
                    + "\nrecipients: " + recipients
                    + "\nnotification type: " + notificationType
                    + "\nsubject template: " + subjectTemplate
            );
        }
    }

    public void sendNotification(NodeRef sourceRef, NodeRef targetRef, Map<String, List<String>> recipients,
                                 String notificationType, String subjectTemplate) {
        this.sendNotification(sourceRef, targetRef, recipients, notificationType, subjectTemplate, false);
    }

    @Override
    protected Map<String, Serializable> getNotificationArgs(NodeRef item) {
        Map<String, Serializable> args = super.getNotificationArgs(item);
        if (targetRef != null) {
            args.put(ARG_TARGET_REF, targetRef);
        }
        args.put(ARG_NOTIFICATION_TYPE, notificationType);
        return args;
    }

    private List<QName> convertStringToQNameList(List<String> stringList) {
        List<QName> qNameList = new ArrayList<>();
        if (stringList != null) {
            for (String item : stringList) {
                qNameList.add(QName.resolveToQName(namespaceService, item));
            }
        }
        return qNameList;
    }

    public void setNodeVariable(String nodeVariable) {
        this.nodeVariable = nodeVariable;
    }

    public void setTemplateEngine(String templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setRecipients(Map<String, List<String>> recipients) {
        this.recipients = recipients;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
