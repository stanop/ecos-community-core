package ru.citeck.ecos.behavior;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.history.HistoryService;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.model.SecurityModel;
import ru.citeck.ecos.model.ContractsModel;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrey Platunov
 */

public class ContractsCreateEventsByRejectionBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private final static Logger logger = Logger.getLogger(ContractsCreateEventsByRejectionBehaviour.class);
    private final static Serializable NODE_UPDATED = "node.updated";
    private final static String REJECTED_WITH_COMMENT = "idocs.rejected-by-counterparty-with-comment";
    private final static String REJECTED_WITHOUT_COMMENT = "idocs.rejected-by-counterparty-without-comment";
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private HistoryService historyService;
    private int order;

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                ContractsModel.CONTRACTS_TYPE,
                new OrderedBehaviour(this,
                        "onUpdateProperties",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

        String packageAtmntStatusBefore = (String) before.get(SecurityModel.PROP_PACKAGE_ATMNT_STATUS);
        String packageAtmntStatusAfter = (String) after.get(SecurityModel.PROP_PACKAGE_ATMNT_STATUS);

        // create event if main document was rejected
        if (packageAtmntStatusAfter != null && !packageAtmntStatusAfter.equals(packageAtmntStatusBefore)) {
            if (packageAtmntStatusAfter.equals(SecurityModel.PKG_ATMNT_STATUS_REJECTED)) {
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
