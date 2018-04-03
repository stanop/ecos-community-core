package ru.citeck.ecos.behavior;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
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
 * @author Andrey Platunov on 31/08/2017.
 */

public class ContractsCreateEventsForAttachments implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private final static Logger logger = Logger.getLogger(ContractsCreateEventsForAttachments.class);

    private static final Serializable NODE_UPDATED = "node.updated";
    private static final String ATMNT_REJECTED_WITH_COMMENT = "idocs-atmnt.rejected-by-counterparty-with-comment";
    private static final String ATMNT_REJECTED_WITHOUT_COMMENT = "idocs-atmnt.rejected-by-counterparty-without-comment";

    PolicyComponent policyComponent;
    NodeService nodeService;
    HistoryService historyService;

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                ContractsModel.ASPECT_IS_CONTRACT_ATTACHMENT,
                new JavaBehaviour(this,
                        "onUpdateProperties",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

        String packageAtmntStatusBefore = (String) before.get(SecurityModel.PROP_PACKAGE_ATMNT_STATUS);
        String packageAtmntStatusAfter = (String) after.get(SecurityModel.PROP_PACKAGE_ATMNT_STATUS);

        if (packageAtmntStatusAfter != null && !packageAtmntStatusAfter.equals(packageAtmntStatusBefore)) {
            if (packageAtmntStatusAfter.equals(SecurityModel.PKG_ATMNT_STATUS_REJECTED)) {
                List<ChildAssociationRef> parentContractAssocRefs = nodeService.getParentAssocs(nodeRef,
                        ICaseModel.ASSOC_DOCUMENTS,
                        RegexQNamePattern.MATCH_ALL);
                if (parentContractAssocRefs != null && parentContractAssocRefs.size() > 0) {
                    for (ChildAssociationRef parentContractsAssocRef : parentContractAssocRefs) {
                        NodeRef contractRef = parentContractsAssocRef.getParentRef();
                        if (contractRef == null || !nodeService.exists(contractRef)) continue;
                        if (nodeService.getType(contractRef).equals(ContractsModel.CONTRACTS_TYPE)) {
                            logger.info("Contract document " + nodeRef + " was rejected");
                            Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>(7);
                            eventProperties.put(HistoryModel.PROP_NAME, NODE_UPDATED);
                            eventProperties.put(HistoryModel.ASSOC_DOCUMENT, contractRef);
                            String title = (String) after.get(ContentModel.PROP_NAME);
                            String comment = (String) after.get(SecurityModel.PROP_COMMENT);
                            if (comment != null && !comment.equals("")) {
                                String message = I18NUtil.getMessage(ATMNT_REJECTED_WITH_COMMENT);
                                eventProperties.put(HistoryModel.PROP_TASK_COMMENT, MessageFormat.format(message, title, comment));
                            } else {
                                String message = I18NUtil.getMessage(ATMNT_REJECTED_WITHOUT_COMMENT);
                                eventProperties.put(HistoryModel.PROP_TASK_COMMENT, MessageFormat.format(message, title));
                            }
                            historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
                            return;
                        }
                    }
                }
            }
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }
}
