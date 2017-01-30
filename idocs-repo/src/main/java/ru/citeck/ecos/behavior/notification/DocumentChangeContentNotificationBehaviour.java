package ru.citeck.ecos.behavior.notification;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roman.Makarskiy on 11/1/2016.
 */
public class DocumentChangeContentNotificationBehaviour extends AbstractICaseDocumentNotificationBehaviour
        implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private String documentNamespace;
    private String documentType;
    private HashMap<String, Object> addition = new HashMap<>();

    private final static String PARAM_METHOD = "method";
    private final static String PARAM_METHOD_UPDATE = "update";
    private final static String PARAM_METHOD_DELETE = "delete";

    public void init() {
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                QName.createQName(documentNamespace, documentType),
                new OrderedBehaviour(this, "onUpdateProperties",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> beforeMap,
                                   Map<QName, Serializable> afterMap) {
        if (!nodeService.exists(nodeRef) || !isActive(nodeRef)) {
            return;
        }

        ContentData contentDataBefore = (ContentData) beforeMap.get(ContentModel.PROP_CONTENT);
        ContentData contentDataAfter = (ContentData) afterMap.get(ContentModel.PROP_CONTENT);

        if (!Objects.equals(contentDataBefore, contentDataAfter)
                && caseStatusService.getStatusRef(nodeRef) != null) {
            if (contentDataAfter == null) {
                addition.put(PARAM_METHOD, PARAM_METHOD_DELETE);
            } else {
                addition.put(PARAM_METHOD, PARAM_METHOD_UPDATE);
            }
            sender.setAdditionArgs(addition);
            sender.sendNotification(nodeRef, null, recipients,
                    notificationType, subjectTemplate);
        }
    }

    public void setDocumentNamespace(String documentNamespace) {
        this.documentNamespace = documentNamespace;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

}
