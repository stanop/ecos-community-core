package ru.citeck.ecos.behavior.icase;

import org.alfresco.repo.policy.Behaviour;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.icase.CaseStatusPolicies;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.IdocsFinalStatusModel;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.List;

/**
 * Case status complete behavior
 */
public class CaseStatusCompleteBehaviour implements CaseStatusPolicies.OnCaseStatusChangedPolicy {

    /**
     * Store reference
     */
    private StoreRef storeRef;

    /**
     * Policy component
     */
    private PolicyComponent policyComponent;

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Search service
     */
    private SearchService searchService;

    /**
     * Init method
     */
    public void init() {
        storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
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

        List<NodeRef> finalStatusesRefs = getFinalStatuses(nodeService.getType(documentRef));

        for (NodeRef finalStatusRef : finalStatusesRefs) {
            List<NodeRef> caseStatuses = RepoUtils.getTargetAssoc(finalStatusRef,
                    IdocsFinalStatusModel.ASSOC_FINAL_STATUSES, nodeService);

            if (caseStatuses != null && caseStatuses.contains(statusRef)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get final statuses
     * @param documentType Document type
     * @return List of final statuses
     */
    private List<NodeRef> getFinalStatuses(QName documentType) {
        SearchParameters parameters = new SearchParameters();
        parameters.addStore(storeRef);
        parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
        parameters.setQuery("TYPE:\"idocs:documentFinalStatus\" AND " +
                "@idocs\\:documentType:\"" + documentType + "\"");
        parameters.addSort("@cm:created", true);
        ResultSet result = searchService.query(parameters);
        return result.getNodeRefs();
    }

    /** Setters */

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
}
