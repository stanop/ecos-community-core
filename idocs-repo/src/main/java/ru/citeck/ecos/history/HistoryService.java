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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.utils.RepoUtils;

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
     * Properties constants
     */
    private static final String ENABLED_REMOTE_HISTORY_SERVICE = "ecos.citeck.history.service.enabled";

    /**
     * Constants
     */
    private static final String ALFRESCO_NAMESPACE = "http://www.alfresco.org/model/content/1.0";
    private static final String MODIFIER_PROPERTY = "modifier";
    private static final String VERSION_LABEL_PROPERTY = "versionLabel";

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
    private static final String INITIATOR = "initiator";
    private static final String WORKFLOW_INSTANCE_ID = "workflowInstanceId";
    private static final String WORKFLOW_DESCRIPTION = "workflowDescription";
    private static final String TASK_EVENT_INSTANCE_ID = "taskEventInstanceId";
    private static final String DOCUMENT_VERSION = "documentVersion";
    private static final String PROPERTY_NAME = "propertyName";

    /**
     * Date-time format
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    /**
     * Global properties
     */
    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    private static Log logger = LogFactory.getLog(HistoryService.class);

    public static final String SYSTEM_USER = "system";

    public static final String UNKNOWN_USER = "unknown-user";

    private static final String PROPERTY_PREFIX = "event";

    private static final String HISTORY_ROOT = "/" + "history:events";

    private NodeService nodeService;

    private AuthenticationService authenticationService;

    private PersonService personService;

    private SearchService searchService;

    private HistoryRemoteService historyRemoteService;

    private StoreRef storeRef;

    private NodeRef historyRoot;

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
        if (isEnabledRemoteHistoryService()) {
            sendHistoryEventToRemoteService(properties);
            return null;
        } else {
            return persistEventToAlfresco(type, properties);
        }
    }

    private NodeRef persistEventToAlfresco(final QName type, final Map<QName, Serializable> properties) {
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef initiator = getInitiator(properties);
            properties.remove(HistoryModel.ASSOC_INITIATOR);
            if (initiator == null) {
                properties.put(HistoryModel.PROP_NAME, UNKNOWN_USER);
            }
            NodeRef document = getDocument(properties);
            properties.remove(HistoryModel.ASSOC_DOCUMENT);

            //sorting in history for assocs
            Date now = new Date();
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

    private void sendHistoryEventToRemoteService(final Map<QName, Serializable> properties) {
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
        NodeRef userRef = personService.getPerson(username);
        requestParams.put(USERNAME, username);
        requestParams.put(USER_ID, userRef.getId());
        /** Event time */
        Date now = new Date();
        if ("assoc.added".equals(properties.get(HistoryModel.PROP_NAME))) {
            now.setTime(now.getTime() + 1000);
        }
        if ("node.created".equals(properties.get(HistoryModel.PROP_NAME))
                || "node.updated".equals(properties.get(HistoryModel.PROP_NAME))) {
            now.setTime(now.getTime() - 5000);
        }
        requestParams.put(CREATION_TIME, dateFormat.format(now));
        /** Event properties */
        requestParams.put(HISTORY_EVENT_ID, UUID.randomUUID().toString());
        requestParams.put(EVENT_TYPE, properties.get(HistoryModel.PROP_NAME));
        requestParams.put(COMMENTS, properties.get(HistoryModel.PROP_TASK_COMMENT));
        requestParams.put(TASK_ROLE, properties.get(HistoryModel.PROP_TASK_ROLE));
        requestParams.put(TASK_OUTCOME, properties.get(HistoryModel.PROP_TASK_OUTCOME));
        QName taskType = (QName) properties.get(HistoryModel.PROP_TASK_TYPE);
        requestParams.put(TASK_TYPE, taskType != null ? taskType.toString() : "");
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

    private boolean isDocumentForDelete(NodeRef documentRef) {
        if(AlfrescoTransactionSupport.getTransactionReadState() != AlfrescoTransactionSupport.TxnReadState.TXN_READ_WRITE) {
            return false;
        } else {
            Set<NodeRef> nodesPendingDelete = TransactionalResourceHelper.getSet("DbNodeServiceImpl.pendingDeleteNodes");
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
}
