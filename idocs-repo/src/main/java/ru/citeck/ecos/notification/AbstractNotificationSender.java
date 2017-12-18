/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.notification;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.DmsModel;
import ru.citeck.ecos.search.ftsquery.BinOperator;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.search.ftsquery.OperatorExpected;
import ru.citeck.ecos.utils.ReflectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

/**
 * Generic implementation of NotificationSender interface.
 * Concrete implementation should provide:
 * - template nodeRef (getNotificationTemplate method)
 * - template arguments (getNotificationArgs method)
 * <p>
 * Concrete implementations can provide:
 * - e-mail subject line (getNotificationSubject method) (if not, it is taken from 'subject' bean property or template 'cm:title' property)
 * - e-mail recipients (getNotificationRecipients method) (if not, it is taken from 'defaultRecipients' bean property)
 * <p>
 * Concrete implementations are provided with:
 * - getNotificationTemplate(key) method
 * - various protected properties
 * - various service references
 * <p>
 * Configuration should provide:
 * - templateRoot - xpath to folder, containing all notification templates
 * - defaultTemplate - name of default template
 * <p>
 * Configuration can provide:
 * - templates - map of key->templates
 * - subject - subject line
 *
 * @author Sergey Tiunov
 */
public abstract class AbstractNotificationSender<ItemType> implements NotificationSender<ItemType> {

    protected String from = null;
    private static final Log logger = LogFactory.getLog(AbstractNotificationSender.class);

    // subject of notification e-mail
    // take from template, if not set (null)
    protected String subject = null;

    // xpath to root folder of templates
    private String templateRoot;

    // templates of notification e-mail - by some key
    private Map<String, String> templates;

    // default template of notification e-mail
    private String defaultTemplate;

    // collection of default mail recipients
    protected Collection<String> defaultRecipients;

    // notification type
    private String notificationType;

    private boolean asyncNotification = true;

    // dependencies:
    protected ServiceRegistry services;
    protected NodeService nodeService;
    protected TransactionService transactionService;
    protected SearchService searchService;
    protected NamespaceService namespaceService;
    protected DictionaryService dictionaryService;
    protected WorkflowQNameConverter qNameConverter;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.services = serviceRegistry;
        this.nodeService = services.getNodeService();
        this.searchService = services.getSearchService();
        this.namespaceService = services.getNamespaceService();
        this.dictionaryService = services.getDictionaryService();
        this.qNameConverter = new WorkflowQNameConverter(namespaceService);
    }

    /**
     * Set notification e-mail subject.
     * Subject line is taken from template's cm:title property, if not set explicitly.
     *
     * @param subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Set e-mail template root folder in xpath.
     *
     * @param templateRoot
     */
    public void setTemplateRoot(String templateRoot) {
        if (templateRoot.endsWith("/")) {
            this.templateRoot = templateRoot;
        } else {
            this.templateRoot = templateRoot + "/";
        }
    }

    /**
     * Set map of templates: workflow-definition-name -> template-file-name.
     *
     * @param templates
     */
    public void setTemplates(Map<String, String> templates) {
        this.templates = templates;
    }

    /**
     * Set default e-mail notification template.
     *
     * @param defaultTemplate
     */
    public void setDefaultTemplate(String defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    /**
     * Set default recipients of e-mail
     *
     * @param defaultRecipients
     */
    public void setDefaultRecipients(Collection<String> defaultRecipients) {
        this.defaultRecipients = defaultRecipients;
    }

    public void setAsyncNotification(boolean asyncNotification) {
        this.asyncNotification = asyncNotification;
        logger.debug("setAsyncNotification_asyncNotification: " + asyncNotification + " instance = " + toString());
    }

    @Override
    public void sendNotification(ItemType item) {
        sendNotification(item, false);
    }

    public void sendNotification(final ItemType item, final boolean afterCommit) {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {
                sendNotification(
                        getNotificationProviderName(item),
                        getNotificationFrom(item),
                        getNotificationSubject(item),
                        getNotificationTemplate(item),
                        getNotificationArgs(item),
                        getNotificationRecipients(item),
                        afterCommit
                );
                return null;
            }
        });
    }

    protected boolean getAsyncNotification() {
        logger.debug("getAsyncNotification_asyncNotification: " + asyncNotification + " instance = " + toString());
        return asyncNotification;
    }

    /**
     * Get notification subject line for specified item.
     *
     * @param item
     * @return subject line
     */
    protected String getNotificationSubject(ItemType item) {
        return subject;
    }

    protected String getNotificationFrom(ItemType item) {
        return from;
    }

    protected String getNotificationProviderName(ItemType item) {
        return EMailNotificationProvider.NAME; //default email notification provider
    }

    /**
     * Get notification template nodeRef for specified item.
     * Concrete implementations can use getNotificationTemplate(key) method for this purposes.
     *
     * @param item
     * @return
     */
    protected abstract NodeRef getNotificationTemplate(ItemType item);

    /**
     * Get notification template arguments for specified item.
     * Set of item names is item-type specific.
     *
     * @param item
     * @return
     */
    protected abstract Map<String, Serializable> getNotificationArgs(ItemType item);

    /**
     * Get collection of notification recipients.
     * Recipient can be user name or group full name (e.g. GROUP_ALFRESCO_ADMINISTRATORS).
     *
     * @param item
     * @return
     */
    protected Collection<String> getNotificationRecipients(ItemType item) {
        return defaultRecipients;
    }

    /**
     * Get notification template by key.
     * Utility method to get notification template.
     * Key is dependent on item type, so concrete implementations may calculate key and use this implemenation.
     *
     * @param key
     * @return
     */
    protected NodeRef getNotificationTemplate(String key) {
        String template = null;

        // try to look template by key
        if (this.templates != null) {
            template = this.templates.get(key);
        }

        // if not found - get default template
        if (template == null) {
            template = defaultTemplate;
        }

        // now get this template in repository:
        return getTemplateNodeRef(template);
    }

    protected NodeRef getNotificationTemplate(String wfkey, String tkey) {
        return getNotificationTemplate(wfkey, tkey, false);
    }

    protected NodeRef getNotificationTemplate(String wfkey, String tkey, boolean findNotSearchable) {

        Map<QName, Serializable> fields = new HashMap<>();

        fields.put(DmsModel.PROP_WORKFLOW_NAME, wfkey);
        fields.put(DmsModel.PROP_TASK_NAME, tkey);

        return getWFNotificationTemplate(fields, findNotSearchable).orElseGet(() ->
            getTemplateNodeRef(this.defaultTemplate)
        );
    }

    protected NodeRef getNotificationTemplate(String wfkey, String tkey, QName docType) {
        return getNotificationTemplate(wfkey, tkey, docType, false);
    }

    protected NodeRef getNotificationTemplate(String wfkey, String tkey, QName docType, boolean findNotSearchable) {

        Map<QName, Serializable> fields = new HashMap<>();

        fields.put(DmsModel.PROP_WORKFLOW_NAME, wfkey);
        fields.put(DmsModel.PROP_TASK_NAME, tkey);
        fields.put(DmsModel.PROP_DOC_TYPE, docType);

        return getWFNotificationTemplate(fields, findNotSearchable).orElseGet(() ->
            getNotificationTemplate(wfkey, tkey, findNotSearchable)
        );
    }

    protected Optional<NodeRef> getWFNotificationTemplate(Map<QName, Serializable> props,
                                                          boolean findNotSearchable) {

        Map<QName, Serializable> tempFields;

        Serializable wfkey = props.get(DmsModel.PROP_TASK_NAME);
        Serializable tkey = props.get(DmsModel.PROP_WORKFLOW_NAME);

        Optional<NodeRef> templateRef = searchTemplate(props, findNotSearchable);

        if (!templateRef.isPresent() && wfkey != null) {
            tempFields = new HashMap<>(props);
            tempFields.put(DmsModel.PROP_WORKFLOW_NAME, null);
            templateRef = searchTemplate(tempFields, findNotSearchable);
        }

        if (!templateRef.isPresent() && tkey != null) {
            tempFields = new HashMap<>(props);
            tempFields.put(DmsModel.PROP_TASK_NAME, null);
            templateRef = searchTemplate(tempFields, findNotSearchable);
        }

        if (!templateRef.isPresent() && wfkey != null && tkey != null) {
            tempFields = new HashMap<>(props);
            tempFields.put(DmsModel.PROP_TASK_NAME, null);
            tempFields.put(DmsModel.PROP_WORKFLOW_NAME, null);
            templateRef = searchTemplate(tempFields, findNotSearchable);
        }

        return templateRef;
    }

    private Optional<NodeRef> searchTemplate(Map<QName, Serializable> props, boolean findNotSearchable) {

        Predicate<NodeRef> notSearchableFilter = ref -> {
            Serializable notSearchable = nodeService.getProperty(ref, DmsModel.PROP_NOT_SEARCHABLE);
            return findNotSearchable || !Boolean.TRUE.equals(notSearchable);
        };

        for (QName key : props.keySet()) {
            props.putIfAbsent(key, "");
        }

        OperatorExpected query = FTSQuery.create()
                                         .type(DmsModel.TYPE_NOTIFICATION_TEMPLATE).and()
                                         .exact(DmsModel.PROP_NOTIFICATION_TYPE, this.notificationType).and()
                                         .values(props, BinOperator.AND, true)
                                         .transactionalIfPossible();

        if (logger.isDebugEnabled()) {
            logger.debug("Template searching. Query: " + query);
        }
        return query.queryOne(searchService, notSearchableFilter);
    }

    protected NodeRef getTemplateNodeRef(String templatePath) {
        String xpath = this.templateRoot + templatePath;
        List<NodeRef> results = this.searchService.query(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                SearchService.LANGUAGE_XPATH,
                xpath).getNodeRefs();
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    private NodeRef findNode(String query) {
        ResultSet nodes = this.searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "fts-alfresco", query);
        if (nodes.length() > 0) {
            NodeRef templateNode = nodes.getNodeRef(0);
            logger.debug("templateNode " + templateNode);
            if ((templateNode != null) && (this.nodeService.exists(templateNode)))
                return templateNode;
        }
        return null;
    }

    /**
     * Set notification type.
     *
     * @param notificationType
     */
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    // send notification
    // subject can be null, then subject is taken from template's cm:title property
    protected void sendNotification(final String notificationProviderName, String from, String subject,
                                    NodeRef template, Map<String, Serializable> args, Collection<String> recipients,
                                    boolean afterCommit) {

        if (template != null) {
            // create notification context
            final NotificationContext notificationContext = new NotificationContext();
            // set necessary variables
            if (subject == null) {
                subject = (String) nodeService.getProperty(template, ContentModel.PROP_TITLE);
            }
            notificationContext.setSubject(subject);
            setBodyTemplate(notificationContext, template);
            notificationContext.setTemplateArgs(args);
            for (String to : recipients) {
                notificationContext.addTo(to);
            }
            notificationContext.setAsyncNotification(asyncNotification);
            logger.debug("sendNotification_asyncNotification: " + asyncNotification + " instance = " + toString());
            if (null != from) {
                notificationContext.setFrom(from);
            }

            // send
            if (afterCommit) {
                AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
                    @Override
                    public void afterCommit() {
                        RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
                        helper.doInTransaction(() -> {
                            sendNotificationContext(notificationProviderName, notificationContext);
                            return null;
                        }, false, true);
                    }
                });
            } else {
                sendNotificationContext(notificationProviderName, notificationContext);
            }
        }
    }

    private void sendNotificationContext(final String notificationProviderName, final NotificationContext notificationContext) {
        AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                services.getNotificationService().sendNotification(
                        notificationProviderName,
                        notificationContext
                );
                return null;
            }
        });
    }

    protected void setBodyTemplate(NotificationContext notificationContext,
                                   NodeRef template) {
        // notificationContext.setBodyTemplate(template);
        // NOTE: for compatibility with Alfresco Community 4.2.c
        ReflectionUtils.callSetterIfDeclared(notificationContext, "setBodyTemplate", template);
        ReflectionUtils.callSetterIfDeclared(notificationContext, "setBodyTemplate", template.toString());
    }

    public Set<String> getRecipients(ItemType task, NodeRef template, NodeRef document) {
        Set<String> authorities = new HashSet<String>();
        Boolean sendToAssigneeProp = isSendToAssignee(template);
        if (sendToAssigneeProp != null && Boolean.TRUE.equals(sendToAssigneeProp)) {
            sendToAssignee(task, authorities);
        }
        Boolean sendToInitiatorProp = isSendToInitiator(template);
        if (sendToInitiatorProp != null && Boolean.TRUE.equals(sendToInitiatorProp)) {
            sendToInitiator(task, authorities);
        }
        Boolean sendToOwnerProp = (Boolean) nodeService.getProperty(template,
                qNameConverter.mapNameToQName("dms_sendToOwner"));
        if (sendToOwnerProp != null && Boolean.TRUE.equals(sendToOwnerProp)
                && document != null && nodeService.exists(document)) {
            sendToOwner(authorities, document);
        }
        ArrayList<String> taskSubscribers = (ArrayList<String>) nodeService.getProperty(template,
                qNameConverter.mapNameToQName("dms_taskSubscribers"));
        if (taskSubscribers != null && taskSubscribers.size() > 0) {
            sendToSubscribers(task, authorities, taskSubscribers);
        }
        String additionRecipientsStr = (String) nodeService.getProperty(template,
                qNameConverter.mapNameToQName("dms_additionRecipients"));
        if (additionRecipientsStr != null && !"".equals(additionRecipientsStr)) {
            String[] additionRecipientsArr = additionRecipientsStr.split(",");
            ArrayList<String> additionRecipients = new ArrayList<String>(Arrays.asList(additionRecipientsArr));

            if (additionRecipients != null && additionRecipients.size() > 0) {
                authorities.addAll(additionRecipients);

            }
        }
        return authorities;
    }

    protected Boolean isSendToInitiator(NodeRef template) {
        return (Boolean) nodeService.getProperty(template,
                qNameConverter.mapNameToQName("dms_sendToInitiator"));
    }

    protected Boolean isSendToAssignee(NodeRef template) {
        return (Boolean) nodeService.getProperty(template,
                qNameConverter.mapNameToQName("dms_sendToAssignee"));
    }

    protected abstract void sendToAssignee(ItemType task, Set<String> authorities);

    protected abstract void sendToInitiator(ItemType task, Set<String> authorities);

    protected abstract void sendToSubscribers(ItemType task, Set<String> authorities, List<String> taskSubscribers);

    protected abstract void sendToOwner(Set<String> authorities, NodeRef node);

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
}
