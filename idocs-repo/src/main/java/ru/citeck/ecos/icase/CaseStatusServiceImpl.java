package ru.citeck.ecos.icase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.utils.DictionaryUtils;
import ru.citeck.ecos.utils.LazyNodeRef;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.*;

/**
 * @author Roman Makarskiy
 * @author Pavel Simonov
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
    public void setStatus(NodeRef caseRef, NodeRef caseStatusRef) {

        mandatoryNodeRef("Case", caseRef);
        mandatoryNodeRef("Case status", caseStatusRef);

        NodeRef beforeCaseStatus = RepoUtils.getFirstTargetAssoc(caseRef, ICaseModel.ASSOC_CASE_STATUS, nodeService);

        if (!Objects.equals(beforeCaseStatus, caseStatusRef)) {
            if (beforeCaseStatus != null) {
                List<AssociationRef> beforeCaseStatusSavedAssocs =
                        nodeService.getTargetAssocs(caseRef, ICaseModel.ASSOC_BEFORE_CASE_STATUS);
                if (!beforeCaseStatusSavedAssocs.isEmpty()) {
                    for (AssociationRef assoc : beforeCaseStatusSavedAssocs) {
                        nodeService.removeAssociation(assoc.getSourceRef(), assoc.getTargetRef(), assoc.getTypeQName());
                    }
                }
                nodeService.createAssociation(caseRef, beforeCaseStatus, ICaseModel.ASSOC_BEFORE_CASE_STATUS);
                nodeService.removeAssociation(caseRef, beforeCaseStatus, ICaseModel.ASSOC_CASE_STATUS);
            }
            nodeService.createAssociation(caseRef, caseStatusRef, ICaseModel.ASSOC_CASE_STATUS);
            nodeService.setProperty(caseRef, ICaseModel.PROP_CASE_STATUS_CHANGED_DATETIME, new Date());
            Set<QName> classes = new HashSet<>(DictionaryUtils.getNodeClassNames(caseStatusRef, nodeService));
            CaseStatusPolicies.OnCaseStatusChangedPolicy changedPolicy;
            changedPolicy = onCaseStatusChangedPolicyDelegate.get(caseStatusRef, classes);
            changedPolicy.onCaseStatusChanged(caseRef, beforeCaseStatus, caseStatusRef);
        }
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

    @Override
    public void setStatus(NodeRef document, String status) {
        NodeRef statusRef = getStatusByName(status);
        if (statusRef == null) {
            throw new IllegalArgumentException("Status " + status + " not found!");
        }
        setStatus(document, statusRef);
    }

    @Override
    public String getStatus(NodeRef caseRef) {
        NodeRef statusRef = getStatusRef(caseRef);
        return statusRef != null ? (String) nodeService.getProperty(statusRef, ContentModel.PROP_NAME) : null;
    }

    @Override
    public NodeRef getStatusRef(NodeRef caseRef) {
        return RepoUtils.getFirstTargetAssoc(caseRef, ICaseModel.ASSOC_CASE_STATUS, nodeService);
    }

    private void mandatoryNodeRef(String strParamName, NodeRef nodeRef) {
        if (nodeRef == null) {
            throw new IllegalArgumentException(strParamName + " is a mandatory parameter");
        }
        if (!nodeService.exists(nodeRef)) {
            throw new IllegalArgumentException(strParamName + " with nodeRef: " + nodeRef + " doesn't exist");
        }
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
