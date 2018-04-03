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
import ru.citeck.ecos.history.HistoryService;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.model.SecurityModel;
import ru.citeck.ecos.model.ContractsModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrey Platunov
 */

public class ContractsCreateEventsByFailBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private final static Logger logger = Logger.getLogger(ContractsCreateEventsByFailBehaviour.class);
    private final static Serializable NODE_UPDATED = "node.updated";
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private HistoryService historyService;
    private int order;

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                ContractsModel.CONTRACTS_TYPE,
                new OrderedBehaviour(this,
                        "onUpdateProperties",
                        Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

        // create event if delivery failed
        String packageStatusCopyBefore = (String) before.get(SecurityModel.PROP_PACKAGE_STATUS_COPY);
        String packageStatusCopyAfter = (String) after.get(SecurityModel.PROP_PACKAGE_STATUS_COPY);

        if (packageStatusCopyAfter != null) {
            if (!packageStatusCopyAfter.equals(packageStatusCopyBefore)) {
                if (packageStatusCopyAfter.equals(SecurityModel.PKG_ATMNT_STATUS_DELIVERY_FAILED)) {
                    logger.info("Document " + nodeRef + " : delivery failed");
                    String comment = (String) after.get(SecurityModel.PROP_PACKAGE_ERROR_CODE);
                    if (comment != null && !comment.equals("")) {
                        Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>(7);
                        eventProperties.put(HistoryModel.PROP_NAME, NODE_UPDATED);
                        eventProperties.put(HistoryModel.ASSOC_DOCUMENT, nodeRef);
                        eventProperties.put(HistoryModel.PROP_DOCUMENT_VERSION, after.get(ContentModel.PROP_VERSION_LABEL));
                        eventProperties.put(HistoryModel.PROP_TASK_COMMENT, comment);
                        historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
                        logger.info("Document " + nodeRef + " : delivery failed, event was created");
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
