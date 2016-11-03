package ru.citeck.ecos.behavior.notification;


import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.ICaseModel;

/**
 * @author Roman.Makarskiy on 10/19/2016.
 */
public class DocumentChangeCaseStatusNotificationBehaviour extends AbstractICaseDocumentNotificationBehaviour
        implements NodeServicePolicies.OnCreateAssociationPolicy {

    private NodeService nodeService;
    private String documentNamespace;
    private String documentType;
    private String caseStatus;

    private boolean enabled;

    private final static String ALL_STATUS_KEY = "AllStatus";


    public void init() {
        OrderedBehaviour createBehaviour = new OrderedBehaviour(
                this, "onCreateAssociation",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order
        );
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                QName.createQName(documentNamespace, documentType),
                ICaseModel.ASSOC_CASE_STATUS,
                createBehaviour
        );
    }

    @Override
    public void onCreateAssociation(final AssociationRef associationRef) {
        if (sender == null || !nodeService.exists(associationRef.getTargetRef())
                           || !nodeService.exists(associationRef.getSourceRef()) || !enabled) {
            return;
        }

        if (caseStatus.equals(ALL_STATUS_KEY)) {
            sender.sendNotification(associationRef.getSourceRef(), associationRef.getTargetRef(), recipients,
                    notificationType, subjectTemplate);
        } else {
            String currentStatus = (String) nodeService.getProperty(associationRef.getTargetRef(),
                    ContentModel.PROP_NAME);
            if (currentStatus.equals(caseStatus)) {
                sender.sendNotification(associationRef.getSourceRef(), associationRef.getTargetRef(), recipients,
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
