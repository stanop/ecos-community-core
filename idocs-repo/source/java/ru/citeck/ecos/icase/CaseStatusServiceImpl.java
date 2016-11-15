package ru.citeck.ecos.icase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.utils.DictionaryUtils;
import ru.citeck.ecos.utils.LazyNodeRef;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.HashSet;
import java.util.List;

/**
 * @author Roman Makarskiy
 */
public class CaseStatusServiceImpl implements CaseStatusService {

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private LazyNodeRef caseStatusesPath;

    private ClassPolicyDelegate<CaseStatusPolicies.OnCaseStatusChangedPolicy> onCaseStatusChangedPolicyDelegate;

    public void init() {
        onCaseStatusChangedPolicyDelegate = policyComponent.registerClassPolicy(CaseStatusPolicies.OnCaseStatusChangedPolicy.class);
    }

    @Override
    public void setStatus(NodeRef documentRef, NodeRef caseStatusRef) {
        if (!nodeService.exists(documentRef)) {
            throw new AlfrescoRuntimeException("Document with nodeRef: " + documentRef + " doesn't exist");
        }

        if (!nodeService.exists(caseStatusRef)) {
            throw new AlfrescoRuntimeException("Case status with nodeRef: " + caseStatusRef + " doesn't exist");
        }

        List<NodeRef> caseStatusRefs = RepoUtils.getTargetNodeRefs(documentRef, ICaseModel.ASSOC_CASE_STATUS, nodeService);
        NodeRef beforeCaseStatus = caseStatusRefs.size() > 0 ? caseStatusRefs.get(0) : null;
        for (NodeRef ref : caseStatusRefs) {
            nodeService.removeAssociation(documentRef, ref, ICaseModel.ASSOC_CASE_STATUS);
        }
        nodeService.createAssociation(documentRef, caseStatusRef, ICaseModel.ASSOC_CASE_STATUS);

        HashSet<QName> classes = new HashSet<>(DictionaryUtils.getNodeClassNames(caseStatusRef, nodeService));
        CaseStatusPolicies.OnCaseStatusChangedPolicy changedPolicy;
        changedPolicy = onCaseStatusChangedPolicyDelegate.get(caseStatusRef, classes);
        changedPolicy.onCaseStatusChanged(documentRef, beforeCaseStatus, caseStatusRef);
    }

    @Override
    public NodeRef getStatusByName(String statusName) {
        if (statusName == null) {
            return null;
        }
        NodeRef root = caseStatusesPath.getNodeRef();
        if (root == null) {
            return null;
        }
        return nodeService.getChildByName(root, ContentModel.ASSOC_CONTAINS, statusName);
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setCaseStatusesPath(LazyNodeRef caseStatusesPath) {
        this.caseStatusesPath = caseStatusesPath;
    }
}
