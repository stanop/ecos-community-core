package ru.citeck.ecos.behavior;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.history.HistoryService;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.SecurityModel;
import ru.citeck.ecos.model.ContractsModel;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Platunov
 * Behaviour creates events for urkk:contracts node and its icase:documents associated documents
 * if it was rejected by counterparty (after stage 'Signing by counterparty (EDO)')
 * OR if parental package was not accepted as valid by Diadoc (possibly if non-valid certificate was used for signing)
 */

public class ContractsCreateEventsFromDiadocBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private final static Logger logger = Logger.getLogger(ContractsCreateEventsFromDiadocBehaviour.class);

    private static final Serializable NODE_UPDATED = "node.updated";
    private static final String REJECTED_WITH_COMMENT = "idocs.rejected-by-counterparty-with-comment";
    private static final String REJECTED_WITHOUT_COMMENT = "idocs.rejected-by-counterparty-without-comment";
    private static final String ATMNT_REJECTED_WITH_COMMENT = "idocs-atmnt.rejected-by-counterparty-with-comment";
    private static final String ATMNT_REJECTED_WITHOUT_COMMENT = "idocs-atmnt.rejected-by-counterparty-without-comment";
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private HistoryService historyService;

    private int order;

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ContractsModel.CONTRACTS_TYPE,
                new OrderedBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        String packageStatusCopyBefore = (String) before.get(SecurityModel.PROP_PACKAGE_STATUS_COPY);
        String packageStatusCopyAfter = (String) after.get(SecurityModel.PROP_PACKAGE_STATUS_COPY);

        // events created only if sam:packageStatusCopy property was changed and only if it changed to REJECTED
        if (packageStatusCopyBefore == null && packageStatusCopyAfter == null) {
            return;
        } else if (packageStatusCopyAfter != null && !packageStatusCopyAfter.equals(packageStatusCopyBefore)) {
            if (packageStatusCopyAfter.equals(SecurityModel.PKG_ATMNT_STATUS_REJECTED)) {
                logger.info("Document " + nodeRef + " or some of its attachments was(were) rejected");
                // then create events for same document (contract) if was rejected
                String packageAtmntStatusAfter = (String) after.get(SecurityModel.PROP_PACKAGE_ATMNT_STATUS);
                if (packageAtmntStatusAfter != null && packageAtmntStatusAfter.equals(SecurityModel.PKG_ATMNT_STATUS_REJECTED)) {
                    logger.info("Document " + nodeRef + " was rejected");
                    Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>(7);
                    eventProperties.put(HistoryModel.PROP_NAME, NODE_UPDATED);
                    eventProperties.put(HistoryModel.ASSOC_DOCUMENT, nodeRef);
                    eventProperties.put(HistoryModel.PROP_DOCUMENT_VERSION, after.get(ContentModel.PROP_VERSION_LABEL));
                    String title = (String) after.get(ContentModel.PROP_NAME);
                    String comment = (String) after.get(SecurityModel.PROP_COMMENT);
                    if (comment != null && !comment.equals("")) {
                        String message = I18NUtil.getMessage(REJECTED_WITH_COMMENT);
                        eventProperties.put(HistoryModel.PROP_TASK_COMMENT, MessageFormat.format(message, title, comment));
                    } else {
                        String message = I18NUtil.getMessage(REJECTED_WITHOUT_COMMENT);
                        eventProperties.put(HistoryModel.PROP_TASK_COMMENT, MessageFormat.format(message, title));
                    }
                    historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
                }

                // and creating events for icase:document associated documents
                // if were rejected
                List<ChildAssociationRef> docsAssocRefs = nodeService.getChildAssocs(nodeRef, ICaseModel.ASSOC_DOCUMENTS, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef docAssocRef : docsAssocRefs) {
                    NodeRef docRef = docAssocRef.getChildRef();
                    if (docRef != null) {
                        String docStatus = (String) nodeService.getProperty(docRef, SecurityModel.PROP_PACKAGE_ATMNT_STATUS);
                        if (docStatus != null && docStatus.equals(SecurityModel.PKG_ATMNT_STATUS_REJECTED)) {
                            logger.info("Contract document " + docRef + " was rejected");
                            Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>(7);
                            eventProperties.put(HistoryModel.PROP_NAME, NODE_UPDATED);
                            eventProperties.put(HistoryModel.ASSOC_DOCUMENT, nodeRef);
                            eventProperties.put(HistoryModel.PROP_DOCUMENT_VERSION, after.get(ContentModel.PROP_VERSION_LABEL));
                            String title = (String) nodeService.getProperty(docRef, ContentModel.PROP_NAME);
                            String comment = (String) nodeService.getProperty(docRef, SecurityModel.PROP_COMMENT);
                            if (comment != null && !comment.equals("")) {
                                String message = I18NUtil.getMessage(ATMNT_REJECTED_WITH_COMMENT);
                                eventProperties.put(HistoryModel.PROP_TASK_COMMENT, MessageFormat.format(message, title, comment));
                            } else {
                                String message = I18NUtil.getMessage(ATMNT_REJECTED_WITHOUT_COMMENT);
                                eventProperties.put(HistoryModel.PROP_TASK_COMMENT, MessageFormat.format(message, title));
                            }
                            historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
                        }
                    }
                }
            }

            // also create event for 'DELIVERY_FAILED' package-status (reason also comes from Diadoc
            // and written at sam:packageErrorCode property)
            if (packageStatusCopyAfter.equals(SecurityModel.PKG_ATMNT_STATUS_DELIVERY_FAILED)) {
                List<AssociationRef> packageAssocRefs = nodeService.getSourceAssocs(nodeRef, SecurityModel.ASSOC_PACKAGE_ATTACHMENTS);
                if (packageAssocRefs != null && packageAssocRefs.size() > 0) {
                    NodeRef packageRef = packageAssocRefs.get(0).getSourceRef();
                    String comment = (String) nodeService.getProperty(packageRef, SecurityModel.PROP_PACKAGE_ERROR_CODE);
                    if (comment != null && !comment.equals("")) {
                        Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>(7);
                        eventProperties.put(HistoryModel.PROP_NAME, NODE_UPDATED);
                        eventProperties.put(HistoryModel.ASSOC_DOCUMENT, nodeRef);
                        eventProperties.put(HistoryModel.PROP_DOCUMENT_VERSION, after.get(ContentModel.PROP_VERSION_LABEL));
                        eventProperties.put(HistoryModel.PROP_TASK_COMMENT, comment);
                        historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
                    }
                }
            }
        }
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
