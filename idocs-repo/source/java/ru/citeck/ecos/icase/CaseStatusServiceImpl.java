package ru.citeck.ecos.icase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.utils.DictionaryUtils;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.HashSet;
import java.util.List;

/**
 * @author Roman.Makarskiy on 11/14/2016.
 */
public class CaseStatusServiceImpl implements CaseStatusService {

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private ServiceRegistry serviceRegistry;

    private static final String CASE_STATUSES_PATH = "app:company_home/app:dictionary/cm:dataLists/cm:case-status";

    private ClassPolicyDelegate<CaseStatusPolicies.OnCaseStatusChangedPolicy> onCaseStatusChangedPolicyDelegate;

    private static Log logger = LogFactory.getLog(CaseStatusServiceImpl.class);

    public void init() {
        onCaseStatusChangedPolicyDelegate = policyComponent.registerClassPolicy(CaseStatusPolicies.OnCaseStatusChangedPolicy.class);
    }

    @Override
    public void setCaseStatus(NodeRef documentRef, NodeRef caseStatusRef) {
        if (!nodeService.exists(documentRef)) {
            throw new AlfrescoRuntimeException("Document with nodeRef: " + documentRef + " doesn't exist");
        }

        if (!nodeService.exists(caseStatusRef)) {
            throw new AlfrescoRuntimeException("Case status with nodeRef: " + caseStatusRef + " doesn't exist");
        }

        List<NodeRef> caseStatusRefs = RepoUtils.getTargetNodeRefs(documentRef, ICaseModel.ASSOC_CASE_STATUS, nodeService);
        for (NodeRef ref : caseStatusRefs) {
            nodeService.removeAssociation(documentRef, ref, ICaseModel.ASSOC_CASE_STATUS);
        }
        nodeService.createAssociation(documentRef, caseStatusRef, ICaseModel.ASSOC_CASE_STATUS);

        HashSet<QName> classes = new HashSet<>(DictionaryUtils.getNodeClassNames(caseStatusRef, nodeService));
        CaseStatusPolicies.OnCaseStatusChangedPolicy changedPolicy;
        changedPolicy = onCaseStatusChangedPolicyDelegate.get(caseStatusRef, classes);

        NodeRef beforeCaseStatus = RepoUtils.getFirstTargetAssoc(documentRef, ICaseModel.ASSOC_CASE_STATUS, nodeService);
        changedPolicy.onCaseStatusChanged(documentRef, beforeCaseStatus, caseStatusRef);
    }

    @Override
    public NodeRef getCaseStatusByName(String statusName) {
        if (statusName == null) {
            return null;
        }
        NodeRef root = getCaseStatusesRoot();
        if (root == null) {
            return null;
        }
        return nodeService.getChildByName(root, ContentModel.ASSOC_CONTAINS, statusName);
    }

    private NodeRef getCaseStatusesRoot() {
        NodeRef caseStatusesRoot;
        NamespaceService namespaceService = serviceRegistry.getNamespaceService();
        SearchService searchService = serviceRegistry.getSearchService();

        NodeRef storeRoot = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRoot, CASE_STATUSES_PATH, null, namespaceService, false);
        caseStatusesRoot = nodeRefs != null && nodeRefs.size() > 0 ? nodeRefs.get(0) : null;

        if (caseStatusesRoot == null) {
            logger.warn("Case statuses root doesn't found! Path: " + CASE_STATUSES_PATH);
        }

        return caseStatusesRoot;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
