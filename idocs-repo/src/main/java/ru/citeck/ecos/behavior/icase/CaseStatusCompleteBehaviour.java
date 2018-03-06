package ru.citeck.ecos.behavior.icase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.icase.CaseStatusPolicies;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.IdocsModel;
import java.util.Collections;
import java.util.List;

/**
 * Case status complete behavior
 */
public class CaseStatusCompleteBehaviour implements CaseStatusPolicies.OnCaseStatusChangedPolicy {

    /**
     * Document class
     */
    private QName documentClass;

    /**
     * Complete status name
     */
    private List<String> statusNames;

    /**
     * Policy component
     */
    private PolicyComponent policyComponent;

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Init method
     */
    public void init() {
        if (statusNames == null) {
            statusNames = Collections.emptyList();
        }
        policyComponent.bindClassBehaviour(
                CaseStatusPolicies.OnCaseStatusChangedPolicy.QNAME, ICaseModel.TYPE_CASE_STATUS,
                new OrderedBehaviour(this, "onCaseStatusChanged", Behaviour.NotificationFrequency.TRANSACTION_COMMIT, 80)
        );
    }

    /**
     * Called when case status changed.
     * @param caseRef          case nodeRef which changed case status
     * @param caseStatusBefore case status nodeRef before changed
     * @param caseStatusAfter  case status nodeRef after changed
     */
    @Override
    public void onCaseStatusChanged(NodeRef caseRef, NodeRef caseStatusBefore, NodeRef caseStatusAfter) {
        if (isAppropriateDocTypeAndStatus(caseRef, caseStatusAfter)) {
            nodeService.setProperty(caseRef, IdocsModel.PROP_DOCUMENT_CASE_COMPLETED, true);
        }
    }

    /**
     * Check document type and status name
     * @param documentRef Document reference
     * @param statusRef Status reference
     * @return Check result
     */
    private boolean isAppropriateDocTypeAndStatus(NodeRef documentRef, NodeRef statusRef) {
        if (documentRef == null || statusRef == null) {
            return false;
        }
        if (!documentClass.equals(nodeService.getType(documentRef))) {
            return false;
        }
        String statusName = (String) nodeService.getProperty(statusRef, ContentModel.PROP_NAME);
        return statusNames.contains(statusName);
    }

    /** Setters */

    public void setDocumentClass(QName documentClass) {
        this.documentClass = documentClass;
    }

    public void setStatusNames(List<String> statusNames) {
        this.statusNames = statusNames;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
