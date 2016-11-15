package ru.citeck.ecos.behavior.notification;


import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.icase.CaseStatusPolicies;
import ru.citeck.ecos.model.ICaseModel;

import java.util.Objects;

/**
 * @author Roman.Makarskiy on 10/19/2016.
 */
public class DocumentChangeCaseStatusNotificationBehaviour extends AbstractICaseDocumentNotificationBehaviour
        implements CaseStatusPolicies.OnCaseStatusChangedPolicy {

    private NodeService nodeService;
    private String documentNamespace;
    private String documentType;
    private String caseStatus;

    private boolean enabled;

    private final static String ALL_STATUS_KEY = "AllStatus";

    public void init() {
        OrderedBehaviour statChangeBehaviour = new OrderedBehaviour(
                this, "onCaseStatusChanged",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order
        );

        this.policyComponent.bindClassBehaviour(
                CaseStatusPolicies.OnCaseStatusChangedPolicy.QNAME,
                ICaseModel.TYPE_CASE_STATUS,
                statChangeBehaviour
        );
    }

    @Override
    public void onCaseStatusChanged(NodeRef documentRef, NodeRef caseStatusBefore, NodeRef caseStatusAfter) {

        if (!enabled || sender == null || !nodeService.exists(documentRef) || !nodeService.exists(caseStatusAfter)) {
            return;
        }

        QName documentQName = nodeService.getType(documentRef);
        QName requiredQName = QName.createQName(documentNamespace, documentType);

        if (!Objects.equals(documentQName, requiredQName)) {
            return;
        }

        if (caseStatus.equals(ALL_STATUS_KEY)) {
            sender.sendNotification(documentRef, caseStatusAfter, recipients,
                    notificationType, subjectTemplate);
        } else {
            String currentStatus = (String) nodeService.getProperty(caseStatusAfter,
                    ContentModel.PROP_NAME);
            if (currentStatus.equals(caseStatus)) {
                sender.sendNotification(documentRef, caseStatusAfter, recipients,
                        notificationType, subjectTemplate);
            }
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setDocumentNamespace(String documentNamespace) {
        this.documentNamespace = documentNamespace;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
