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
package ru.citeck.ecos.history;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.config.EcosConfigService;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.utils.RepoUtils;
import ru.citeck.ecos.utils.TransactionUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Provides manipulations with history
 *
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class HistoryService {

    /**
     * Constants
     */
    public static final String KEY_PENDING_DELETE_NODES = "DbNodeServiceImpl.pendingDeleteNodes";
    public static final String SYSTEM_USER = "system";
    public static final String UNKNOWN_USER = "unknown-user";

    private static final String ENABLED_REMOTE_HISTORY_SERVICE = "ecos.citeck.history.service.enabled";
    private static final String ALFRESCO_NAMESPACE = "http://www.alfresco.org/model/content/1.0";
    private static final String MODIFIER_PROPERTY = "modifier";
    private static final String VERSION_LABEL_PROPERTY = "versionLabel";
    private static final String DEFAULT_SLA_JOURNAL_ITEM_ID = "actual-default-sla-duration";

    private static final String HISTORY_EVENT_ID = "historyEventId";
    private static final String DOCUMENT_ID = "documentId";
    private static final String EVENT_TYPE = "eventType";
    private static final String COMMENTS = "comments";
    private static final String VERSION = "version";
    private static final String CREATION_TIME = "creationTime";
    private static final String USERNAME = "username";
    private static final String USER_ID = "userId";
    private static final String TASK_ROLE = "taskRole";
    private static final String TASK_OUTCOME = "taskOutcome";
    private static final String TASK_TYPE = "taskType";
    private static final String FULL_TASK_TYPE = "fullTaskType";
    private static final String INITIATOR = "initiator";
    private static final String WORKFLOW_INSTANCE_ID = "workflowInstanceId";
    private static final String WORKFLOW_DESCRIPTION = "workflowDescription";
    private static final String TASK_EVENT_INSTANCE_ID = "taskEventInstanceId";
    private static final String DOCUMENT_VERSION = "documentVersion";
    private static final String PROPERTY_NAME = "propertyName";
    private static final String EXPECTED_PERFORM_TIME = "expectedPerformTime";

    private static final String TRANSFER_PROCESS_NAME = "transfer-old-history-process";
    private static final int BATCH_SIZE = 1;
    private static final int LOGGING_INTERVAL = 10;

    private static Log logger = LogFactory.getLog(HistoryService.class);
    private static final String PROPERTY_PREFIX = "event";
    private static final String HISTORY_ROOT = "/" + "history:events";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private boolean isHistoryTransferring = false;
    private boolean isHistoryTransferringInterrupted = false;

    /**
     * Global properties
     */
    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    /**
     * Ecos configuration service (system journals - configuration)
     */
    @Autowired
    private EcosConfigService ecosConfigService;

    private NodeService nodeService;

    private AuthenticationService authenticationService;

    private PersonService personService;

    private SearchService searchService;

    private HistoryRemoteService historyRemoteService;

    private StoreRef storeRef;

    private NodeRef historyRoot;

    private TransactionService transactionService;

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setHistoryRemoteService(HistoryRemoteService historyRemoteService) {
        this.historyRemoteService = historyRemoteService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
        storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
    }

    public void setHistoryRoot(NodeRef historyRoot) {
        this.historyRoot = historyRoot;
    }

    public NodeRef persistEvent(final QName type, final Map<QName, Serializable> properties) {
        Date creationDate = new Date();
        TransactionUtils.doAfterCommit(new Runnable() {
            @Override
            public void run() {
                if (isEnabledRemoteHistoryService()) {
                    sendHistoryEventToRemoteService(properties, creationDate);
                } else {
                    persistEventToAlfresco(type, properties, creationDate);
                }
            }
        });
        return null;
    }

    private NodeRef persistEventToAlfresco(final QName type, final Map<QName, Serializable> properties, Date creationDate) {
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef initiator = getInitiator(properties);
            properties.remove(HistoryModel.ASSOC_INITIATOR);
            if (initiator == null) {
                properties.put(HistoryModel.PROP_NAME, UNKNOWN_USER);
            }
            NodeRef document = getDocument(properties);
            properties.remove(HistoryModel.ASSOC_DOCUMENT);

            //sorting in history for assocs
            Date now = creationDate;
            if ("assoc.added".equals(properties.get(HistoryModel.PROP_NAME))) {
                now.setTime(now.getTime() + 1000);
            }
            if ("node.created".equals(properties.get(HistoryModel.PROP_NAME))
                    || "node.updated".equals(properties.get(HistoryModel.PROP_NAME))) {
                now.setTime(now.getTime() - 5000);
            }
            properties.put(HistoryModel.PROP_DATE, now);
            QName assocName = QName.createQName(HistoryModel.HISTORY_NAMESPACE, "event." + properties.get(HistoryModel.PROP_NAME));

            QName assocType;
            NodeRef parentNode;
            if (document == null) {
                parentNode = getHistoryRoot();
                assocType = ContentModel.ASSOC_CONTAINS;
            } else {
                if (isDocumentForDelete(document)) {
                    parentNode = getHistoryRoot();
                } else {
                    parentNode = document;
                }
                assocType = HistoryModel.ASSOC_EVENT_CONTAINED;
            }

            /** Modifier */
            String currentUsername = authenticationService.getCurrentUserName();
            if (currentUsername != null) {
                properties.put(HistoryModel.MODIFIER_PROPERTY, currentUsername);
            }
            NodeRef historyEvent = nodeService.createNode(parentNode, assocType, assocName, type, properties).getChildRef();

            if (initiator != null) {
                if (!RepoUtils.isAssociated(historyEvent, initiator, HistoryModel.ASSOC_INITIATOR, nodeService)) {
                    nodeService.createAssociation(historyEvent, initiator, HistoryModel.ASSOC_INITIATOR);
                } else {
                    logger.warn("Association " + HistoryModel.ASSOC_INITIATOR.toString() + " already exists between " + historyEvent.toString() + " and " + initiator.toString());
                }
                persistAdditionalProperties(historyEvent, initiator);
            }
            if (document != null && !isDocumentForDelete(document)) {
                nodeService.createAssociation(historyEvent, document, HistoryModel.ASSOC_DOCUMENT);
                persistAdditionalProperties(historyEvent, document);
                List<ChildAssociationRef> parents = nodeService.getParentAssocs(document);
                for (ChildAssociationRef parent : parents) {
                    NodeRef parentCase = parent.getParentRef();
                    if (nodeService.hasAspect(parentCase, ICaseModel.ASPECT_CASE) || nodeService.hasAspect(parentCase, ICaseModel.ASPECT_SUBCASE)) {
                        nodeService.createAssociation(historyEvent, parentCase, HistoryModel.ASSOC_CASE);
                    }
                }

                historyRemoteService.updateDocumentHistoryStatus(document, false);
            }
            return historyEvent;
        });
    }

    private void sendHistoryEventToRemoteService(final Map<QName, Serializable> properties, Date creationDate) {
        Map<String, Object> requestParams = new HashMap();
        /** Document */
        NodeRef document = getDocument(properties);
        if (document == null || isDocumentForDelete(document)) {
            return;
        }
        requestParams.put(DOCUMENT_ID, document.getId());
        requestParams.put(VERSION, getDocumentProperty(document, VERSION_LABEL_PROPERTY));
        /** User */
        String username = (String) getDocumentProperty(document, MODIFIER_PROPERTY);
        String currentUsername = authenticationService.getCurrentUserName();
        if (currentUsername != null) {
            username = currentUsername;
        }
        NodeRef userRef = personService.getPerson(username);
        requestParams.put(USERNAME, username);
        requestParams.put(USER_ID, userRef.getId());
        /** Event time */
        Date now = creationDate;
        if ("assoc.added".equals(properties.get(HistoryModel.PROP_NAME))
                    || "task.assign".equals(properties.get(HistoryModel.PROP_NAME))) {
            now.setTime(now.getTime() + 5000);
        }
        if ("node.created".equals(properties.get(HistoryModel.PROP_NAME))
                || "node.updated".equals(properties.get(HistoryModel.PROP_NAME))) {
            now.setTime(now.getTime() - 5000);
        }
        requestParams.put(CREATION_TIME, dateFormat.format(now));
        /** Expected perform time */
        NodeRef taskCaseRef = (NodeRef) properties.get(HistoryModel.PROP_CASE_TASK);
        if (taskCaseRef != null) {
           Integer expectedPerformTime = (Integer) nodeService.getProperty(taskCaseRef, ICaseTaskModel.PROP_EXPECTED_PERFORM_TIME);
           if (expectedPerformTime == null) {
               expectedPerformTime = getDefaultSLA();
           }
            requestParams.put(EXPECTED_PERFORM_TIME, expectedPerformTime != null ? expectedPerformTime.toString() : null);
        }
        /** Event properties */
        requestParams.put(HISTORY_EVENT_ID, UUID.randomUUID().toString());
        requestParams.put(EVENT_TYPE, properties.get(HistoryModel.PROP_NAME));
        requestParams.put(COMMENTS, properties.get(HistoryModel.PROP_TASK_COMMENT));
        requestParams.put(TASK_ROLE, properties.get(HistoryModel.PROP_TASK_ROLE));
        requestParams.put(TASK_OUTCOME, properties.get(HistoryModel.PROP_TASK_OUTCOME));
        QName taskType = (QName) properties.get(HistoryModel.PROP_TASK_TYPE);
        requestParams.put(TASK_TYPE, taskType != null ? taskType.getLocalName() : "");
        requestParams.put(FULL_TASK_TYPE, taskType != null ? taskType.toString() : "");
        /** Workflow properties */
        requestParams.put(INITIATOR, properties.get(HistoryModel.ASSOC_INITIATOR));
        requestParams.put(WORKFLOW_INSTANCE_ID, properties.get(HistoryModel.PROP_WORKFLOW_INSTANCE_ID));
        requestParams.put(WORKFLOW_DESCRIPTION, properties.get(HistoryModel.PROP_WORKFLOW_DESCRIPTION));
        requestParams.put(TASK_EVENT_INSTANCE_ID, properties.get(HistoryModel.PROP_TASK_INSTANCE_ID));
        requestParams.put(DOCUMENT_VERSION, properties.get(HistoryModel.PROP_DOCUMENT_VERSION));
        QName propertyName = (QName) properties.get(HistoryModel.PROP_PROPERTY_NAME);
        requestParams.put(PROPERTY_NAME, propertyName != null ? propertyName.getLocalName() : null);
        historyRemoteService.sendHistoryEventToRemoteService(requestParams);
    }

    private Integer getDefaultSLA() {
        String rawSla = (String) ecosConfigService.getParamValue(DEFAULT_SLA_JOURNAL_ITEM_ID);
        try {
            return Integer.valueOf(rawSla);
        } catch (NumberFormatException exception) {
            logger.error("Can't transform '" + rawSla + "' to the number", exception);
            return null;
        }
    }

    private boolean isDocumentForDelete(NodeRef documentRef) {
        if(AlfrescoTransactionSupport.getTransactionReadState() != AlfrescoTransactionSupport.TxnReadState.TXN_READ_WRITE) {
            return false;
        } else {
            Set<NodeRef> nodesPendingDelete = TransactionalResourceHelper.getSet(KEY_PENDING_DELETE_NODES);
            return nodesPendingDelete.contains(documentRef);
        }
    }

    public void removeEventsByDocument(NodeRef documentRef) {
        if (isEnabledRemoteHistoryService()) {
            historyRemoteService.removeEventsByDocument(documentRef);
        } else {
            List<AssociationRef> associations = nodeService.getSourceAssocs(documentRef, HistoryModel.ASSOC_DOCUMENT);
            for (AssociationRef associationRef : associations) {
                NodeRef eventRef = associationRef.getSourceRef();
                nodeService.deleteNode(eventRef);
            }
        }
    }

    public String sendAndRemoveAllOldEvents(Integer offset, Integer maxItemsCount, Integer stopCount, Integer threads) {
        if (isHistoryTransferring) {
            return "History is transferring";
        }
        isHistoryTransferring = true;
        final Integer threadsCount;
        if (threads != null && threads > 0) {
            threadsCount = threads;
        } else {
            threadsCount = 1;
        }
        logger.info("History transferring started with threads " + threadsCount);
        logger.info("History transferring started from position - " + offset);
        logger.info("History transferring. Max load size - " + maxItemsCount);

        try {
            /** Load first documents */
            AuthenticationUtil.runAsSystem(() -> {
                int documentsTransferred = 0;
                int skipCount = offset;
                ResultSet resultSet = getDocumentsResultSetByOffset(skipCount, maxItemsCount);
                boolean hasMore;

                /** Start processing */
                do {
                    List<NodeRef> documents = resultSet.getNodeRefs();
                    hasMore = resultSet.hasMore();
                    /** Process each document */
                    for (NodeRef documentRef : documents) {
                        if (isHistoryTransferringInterrupted) {
                            logger.info("History transferring - documents have been transferred - " + (documentsTransferred + offset));
                            return null;
                        }
                        RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
                        BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>(
                                TRANSFER_PROCESS_NAME,
                                retryingTransactionHelper,
                                new HistoryTransferProvider(getEventsByDocumentRef(documentRef)),
                                threadsCount, BATCH_SIZE,
                                null, null, LOGGING_INTERVAL
                        );
                        batchProcessor.process(new HistoryTransferWorker(historyRemoteService), true);
                        historyRemoteService.updateDocumentHistoryStatus(documentRef, true);
                        documentsTransferred++;
                    }
                    skipCount += documents.size();
                    resultSet = getDocumentsResultSetByOffset(skipCount, maxItemsCount);
                    logger.info("History transferring - documents have been transferred - " + (documentsTransferred + offset));
                    if (stopCount != null && stopCount > 0) {
                        if (stopCount < documentsTransferred) {
                            break;
                        }
                    }
                } while (hasMore);
                logger.info("History transferring - all documents have been transferred - " + (documentsTransferred + offset));
                return null;
            });
            return "History transferring - documents have been transferred";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            isHistoryTransferring = false;
            isHistoryTransferringInterrupted = false;
        }
    }

    public String interruptHistoryTransferring() {
        isHistoryTransferringInterrupted = true;
        return "History transferring - interrupting";
    }

    private List<NodeRef> getEventsByDocumentRef(NodeRef documentRef) {
        List<AssociationRef> associations = nodeService.getSourceAssocs(documentRef, HistoryModel.ASSOC_DOCUMENT);
        List<NodeRef> result = new ArrayList<>(associations.size());
        for (AssociationRef associationRef : associations) {
            result.add(associationRef.getSourceRef());
        }
        return result;
    }

    private ResultSet getDocumentsResultSetByOffset(Integer offset, Integer maxItemsCount) {
        SearchParameters parameters = new SearchParameters();
        parameters.addStore(storeRef);
        parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
        parameters.setQuery("TYPE:\"idocs:doc\"");
        parameters.addSort("@cm:created", true);
        parameters.setMaxItems(maxItemsCount);
        parameters.setSkipCount(offset);
        return searchService.query(parameters);
    }

    public void sendAndRemoveOldEventsByDocument(NodeRef documentRef) {
        AuthenticationUtil.runAsSystem(() -> {
            /** Check - is remote service enabled */
            if (!isEnabledRemoteHistoryService()) {
                throw new RuntimeException("Remote history service is disabled. Old history event transferring is impossible");
            }
            /** Check - node existing */
            if (!nodeService.exists(documentRef)) {
                return null;
            }
            Boolean useNewHistory = (Boolean) nodeService.getProperty(documentRef, IdocsModel.DOCUMENT_USE_NEW_HISTORY);
            /** Send events to remote service or remove old nodes */
            if (useNewHistory == null || useNewHistory == false) {
                historyRemoteService.sendHistoryEventsByDocumentToRemoteService(documentRef);
            } else {
                List<AssociationRef> associations = nodeService.getSourceAssocs(documentRef, HistoryModel.ASSOC_DOCUMENT);
                for (AssociationRef associationRef : associations) {
                    nodeService.deleteNode(associationRef.getSourceRef());
                }
            }
            return null;
        });
    }

    /**
     * Get document property
     *
     * @param documentNode      Document node
     * @param localPropertyName Local property name
     * @return Property object
     */
    private Object getDocumentProperty(NodeRef documentNode, String localPropertyName) {
        return nodeService.getProperty(documentNode, QName.createQName(ALFRESCO_NAMESPACE, localPropertyName));
    }

    /**
     * Check - is remote history service enabled
     *
     * @return Check result
     */
    private Boolean isEnabledRemoteHistoryService() {
        String propertyValue = properties.getProperty(ENABLED_REMOTE_HISTORY_SERVICE);
        if (propertyValue == null) {
            return false;
        } else {
            return Boolean.valueOf(propertyValue);
        }
    }

    /**
     * Add property that will be persisted within history.
     *
     * @param nodeRef     some node
     * @param sourceProp  property of node, that we wish to persist in history
     * @param historyProp property of history events, that will contain property value
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addHistoricalProperty(NodeRef nodeRef, QName sourceProp, QName historyProp) {
        Object oldValue = nodeService.getProperty(nodeRef, HistoryModel.PROP_ADDITIONAL_PROPERTIES);
        HashMap<QName, QName> propertyMapping = new HashMap<QName, QName>();
        if (oldValue != null && oldValue instanceof Map) {
            propertyMapping.putAll((Map) oldValue);
        }
        propertyMapping.put(sourceProp, historyProp);
        nodeService.setProperty(nodeRef, HistoryModel.PROP_ADDITIONAL_PROPERTIES, propertyMapping);
    }

    public List<NodeRef> getEventsByInitiator(NodeRef initiator) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date monthAgo = calendar.getTime();
        return getEventsByInitiator(initiator, monthAgo);
    }

    public List<NodeRef> getEventsByInitiator(NodeRef initiator, Date limitDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        // TODO refactor with CriteriaSearchService
        SearchParameters parameters = new SearchParameters();
        parameters.addStore(storeRef);
        parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
        parameters.setQuery("PATH:\"" + HISTORY_ROOT + "/*\" AND @" + PROPERTY_PREFIX + "\\:date:[" + dateFormat.format(limitDate) + " TO NOW] " +
                "AND @" + PROPERTY_PREFIX + "\\:initiator_added:\"" + initiator + "\"");
        parameters.addSort("@event:date", true);
        ResultSet query = searchService.query(parameters);
        return query.getNodeRefs();
    }

    public List<NodeRef> getEventsByDocument(NodeRef document) {
        // TODO refactor with CriteriaSearchService
        SearchParameters parameters = new SearchParameters();
        parameters.addStore(storeRef);
        parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
        parameters.setQuery("PATH:\"" + HISTORY_ROOT + "/*\" AND @" + PROPERTY_PREFIX + "\\:document_added:\"" + document + "\"");
        parameters.addSort("@event:date", true);
        ResultSet query = searchService.query(parameters);
        return query.getNodeRefs();
    }

    public List<NodeRef> getAllEventsByDocumentAndEventName(NodeRef document, String eventName) {
        SearchParameters parameters = new SearchParameters();
        parameters.addStore(storeRef);
        parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
        parameters.setQuery("@" + PROPERTY_PREFIX + "\\:document_added:\"" + document + "\" AND @" + PROPERTY_PREFIX + "\\:name:\"" + eventName + "\"");
        parameters.addSort("@event:date", true);
        ResultSet query = searchService.query(parameters);
        return query.getNodeRefs();
    }

    public List<NodeRef> getEventsByWorkflow(String instanceId) {
        // TODO refactor with CriteriaSearchService
        SearchParameters parameters = new SearchParameters();
        parameters.addStore(storeRef);
        parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
        parameters.setQuery("PATH:\"" + HISTORY_ROOT + "/*\" AND @" + PROPERTY_PREFIX + "\\:workflowInstanceId:\"" + instanceId + "\"");
        parameters.addSort("@event:date", true);
        ResultSet query = searchService.query(parameters);
        return query.getNodeRefs();
    }

    public List<NodeRef> getEventsByTask(String instanceId) {
        // TODO refactor with CriteriaSearchService
        SearchParameters parameters = new SearchParameters();
        parameters.addStore(storeRef);
        parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
        parameters.setQuery("PATH:\"" + HISTORY_ROOT + "/*\" AND @" + PROPERTY_PREFIX + "\\:taskInstanceId:\"" + instanceId + "\"");
        parameters.addSort("@event:date", false);
        ResultSet query = searchService.query(parameters);
        return query.getNodeRefs();
    }

    private NodeRef getHistoryRoot() {
        return historyRoot;
    }

    private NodeRef getInitiator(Map<QName, Serializable> properties) {
        Serializable username = properties.get(HistoryModel.ASSOC_INITIATOR);
        NodeRef person = null;
        if (username instanceof NodeRef) {
            person = nodeService.exists((NodeRef) username) ? (NodeRef) username : null;
        } else if (username instanceof String) {
            if (!username.toString().equals(SYSTEM_USER)) {
                person = personService.getPerson(username.toString());
            }
        } else if (username == null) {
            person = personService.getPerson(authenticationService.getCurrentUserName());
        }
        return person;
    }

    private NodeRef getDocument(Map<QName, Serializable> properties) {
        Serializable document = properties.get(HistoryModel.ASSOC_DOCUMENT);
        NodeRef documentNodeRef = null;
        if (document != null) {
            documentNodeRef = new NodeRef(document.toString());
            if (!nodeService.exists(documentNodeRef)) {
                documentNodeRef = null;
            }
        }
        return documentNodeRef;
    }

    private void persistAdditionalProperties(NodeRef historyEvent, NodeRef document) {
        Object mapping = nodeService.getProperty(document, HistoryModel.PROP_ADDITIONAL_PROPERTIES);
        if (mapping == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<QName, QName> propertyMapping = (Map<QName, QName>) mapping;
        Map<QName, Serializable> additionalProperties = new HashMap<QName, Serializable>(propertyMapping.size());
        for (QName documentProp : propertyMapping.keySet()) {
            QName historyProp = propertyMapping.get(documentProp);
            additionalProperties.put(historyProp, nodeService.getProperty(document, documentProp));
        }
        nodeService.addProperties(historyEvent, additionalProperties);
    }

    /**
     * History batch process worker
     */
    private static class HistoryTransferWorker extends BatchProcessor.BatchProcessWorkerAdaptor<NodeRef> {

        private HistoryRemoteService historyService;

        HistoryTransferWorker(HistoryRemoteService historyService) {
            this.historyService = historyService;
        }

        @Override
        public void process(NodeRef eventRef) throws Throwable {
            AuthenticationUtil.runAsSystem(() -> {
                historyService.sendHistoryEventToRemoteService(eventRef);
                return null;
            });
        }
    }

    /**
     * History transfer provider
     */
    private static class HistoryTransferProvider implements BatchProcessWorkProvider<NodeRef> {

        private Collection<NodeRef> events;
        private boolean hasMore = true;

        HistoryTransferProvider(Collection<NodeRef> events) {
            this.events = events;
        }

        @Override
        public int getTotalEstimatedWorkSize() {
            return events.size();
        }

        @Override
        public Collection<NodeRef> getNextWork() {
            if (hasMore) {
                hasMore = false;
                return events;
            } else {
                return Collections.emptyList();
            }
        }
    }
}
