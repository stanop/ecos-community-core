package ru.citeck.ecos.behavior.icase;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.icase.CaseStatusPolicies;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.IdocsFinalStatusModel;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.NodeUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Case status complete behavior
 */
public class CaseStatusCompleteBehaviour implements CaseStatusPolicies.OnCaseStatusChangedPolicy {

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

    private NodeUtils nodeUtils;

    private LoadingCache<QName, Set<NodeRef>> finalStatusesByType;

    private long cacheAge = 300;

    /**
     * Init method
     */
    public void init() {
        policyComponent.bindClassBehaviour(
                CaseStatusPolicies.OnCaseStatusChangedPolicy.QNAME, ICaseModel.TYPE_CASE_STATUS,
                new OrderedBehaviour(this, "onCaseStatusChanged",
                                     Behaviour.NotificationFrequency.TRANSACTION_COMMIT, 80)
        );
        finalStatusesByType = CacheBuilder.newBuilder()
                                          .expireAfterWrite(cacheAge, TimeUnit.SECONDS)
                                          .maximumSize(100)
                                          .build(CacheLoader.from(this::getFinalStatuses));
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
        if (documentRef != null && statusRef != null) {
            QName type = nodeService.getType(documentRef);
            return finalStatusesByType.getUnchecked(type).contains(statusRef);
        }
        return false;
    }

    /**
     * Get final statuses
     * @param documentType Document type
     * @return List of final statuses
     */
    private Set<NodeRef> getFinalStatuses(QName documentType) {
        return AuthenticationUtil.runAsSystem(() ->
                FTSQuery.create()
                        .type(IdocsFinalStatusModel.TYPE_DOC_FINAL_STATUS).and()
                        .exact(IdocsFinalStatusModel.PROP_DOC_TYPE, documentType)
                        .transactional()
                        .query(searchService)
                        .stream()
                        .flatMap(ref -> getFinalStatuses(ref).stream())
                        .collect(Collectors.toSet())
        );
    }

    private List<NodeRef> getFinalStatuses(NodeRef nodeRef) {
        return nodeUtils.getAssocTargets(nodeRef, IdocsFinalStatusModel.ASSOC_FINAL_STATUSES);
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

    public void setCacheAge(long cacheAge) {
        this.cacheAge = cacheAge;
    }

    public void resetCache() {
        finalStatusesByType.invalidateAll();
    }

    public void setNodeUtils(NodeUtils nodeUtils) {
        this.nodeUtils = nodeUtils;
    }
}

