package ru.citeck.ecos.behavior.copy;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.behavior.JavaBehaviour;
import ru.citeck.ecos.icase.CaseStatusPolicies;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author Roman Velikoselsky
 */

public class CopyMetadataFromTargetToSourceBehaviour implements NodeServicePolicies.OnCreateAssociationPolicy,
        NodeServicePolicies.OnDeleteAssociationPolicy,
        NodeServicePolicies.OnUpdatePropertiesPolicy,
        CaseStatusPolicies.OnCaseStatusChangedPolicy {

    private PolicyComponent policyComponent;
    private NodeService nodeService;

    private QName targetTypeQName;
    private QName sourceTypeQName;
    private QName caseStatusAssocQName;
    private QName assocTypeQName;
    private Map<QName, QName> assocsToCopy = Collections.emptyMap();
    private Map<QName, QName> propsToCopy = Collections.emptyMap();

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                targetTypeQName,
                new JavaBehaviour(this, "onUpdateProperties",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                sourceTypeQName,
                new JavaBehaviour(this, "onCreateAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                targetTypeQName,
                new JavaBehaviour(this, "onCreateAssociationOnTarget",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
                targetTypeQName,
                new JavaBehaviour(this, "onDeleteAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindClassBehaviour(
                CaseStatusPolicies.OnCaseStatusChangedPolicy.QNAME,
                ICaseModel.TYPE_CASE_STATUS,
                new JavaBehaviour(this, "onCaseStatusChanged",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onCreateAssociation(AssociationRef associationRef) {
        NodeRef sourceRef = associationRef.getSourceRef();
        NodeRef targetRef = associationRef.getTargetRef();

        if (!nodeService.exists(sourceRef) || !nodeService.exists(targetRef)) {
            return;
        }

        if (!nodeService.getType(targetRef).equals(targetTypeQName) ||
                !nodeService.getType(sourceRef).equals(sourceTypeQName))  {
            return;
        }

        updateSourceAssocs(sourceRef, targetRef);
    }

    @Override
    public void onDeleteAssociation(AssociationRef associationRef) {
        // It's correct! We need target to receive his source assocs
        if (assocTypeQName == null) {
            return;
        }

        NodeRef targetRef = associationRef.getSourceRef();
        NodeRef targetAssoc = associationRef.getTargetRef();

        if (!nodeService.exists(targetRef) || !nodeService.exists(targetAssoc)) {
            return;
        }

        List<NodeRef> sourceRefs = RepoUtils.getSourceNodeRefs(targetRef, assocTypeQName, nodeService);

        for (NodeRef sourceRef : sourceRefs) {
            deleteAssocs(sourceRef, targetAssoc);
        }

    }

    @Override
    public void onUpdateProperties(NodeRef targetRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (!nodeService.exists(targetRef)) {
            return;
        }

        checkChangesAtPropsAndUpdate(targetRef, before, after);
    }

    @Override
    public void onCaseStatusChanged(NodeRef caseRef, NodeRef caseStatusBefore, NodeRef caseStatusAfter) {
        if (caseStatusAssocQName == null) {
            return;
        }

        if (!nodeService.exists(caseRef)) {
            return;
        }

        if (!nodeService.getType(caseRef).equals(targetTypeQName))  {
            return;
        }

        List<NodeRef> sourceAssocs = RepoUtils.getSourceNodeRefs(caseRef, assocTypeQName, nodeService);

        for (NodeRef document : sourceAssocs) {
            if (RepoUtils.isAssociated(document, caseStatusBefore, caseStatusAssocQName, nodeService)) {
                RepoUtils.removeAssociation(document, caseStatusBefore, caseStatusAssocQName, true, nodeService);
            }

            if (!RepoUtils.isAssociated(document, caseStatusAfter, caseStatusAssocQName, nodeService)) {
                RepoUtils.createAssociation(document, caseStatusAfter, caseStatusAssocQName, true, nodeService);
            }
        }
    }

    public void onCreateAssociationOnTarget(AssociationRef associationRef) {
        NodeRef targetRef = associationRef.getSourceRef();
        NodeRef targetAssoc = associationRef.getTargetRef();

        if (!nodeService.exists(targetRef) || !nodeService.exists(targetAssoc)) {
            return;
        }

        List<NodeRef> sourceRefs = RepoUtils.getSourceNodeRefs(targetRef, assocTypeQName, nodeService);

        for (NodeRef sourceRef : sourceRefs) {
            updateAssocValue(sourceRef, targetRef);
        }

    }

    private void updateSourceAssocs(NodeRef sourceRef, NodeRef targetRef) {
        if (!nodeRefExists(sourceRef) || !nodeRefExists(targetRef)) {
            return;
        }

        for (Map.Entry<QName, QName> pair : propsToCopy.entrySet()) {
            Serializable propValue = nodeService.getProperty(targetRef, pair.getKey());
            nodeService.setProperty(sourceRef, pair.getValue(), propValue);
        }

        updateAssocValue(sourceRef, targetRef);
    }

    private void checkChangesAtPropsAndUpdate(NodeRef targetRef, Map<QName, Serializable> before,
                                              Map<QName, Serializable> after) {
        for (Map.Entry<QName, QName> pair : propsToCopy.entrySet()) {
            QName propCopyFrom = pair.getKey();

            Serializable propBefore = before.get(propCopyFrom);
            Serializable propAfter = after.get(propCopyFrom);

            if (!Objects.equals(propBefore, propAfter)) {
                List<NodeRef> sourceAssocs = RepoUtils.getSourceNodeRefs(targetRef, assocTypeQName, nodeService);

                for (NodeRef node : sourceAssocs) {
                    nodeService.setProperty(node, pair.getValue(), propAfter);
                }
            }
        }
    }

    private void updateAssocValue(NodeRef sourceRef, NodeRef targetRef) {
        for (Map.Entry<QName, QName> pair : assocsToCopy.entrySet()) {
            List<AssociationRef> associationRefs = nodeService.getTargetAssocs(targetRef, pair.getKey());

            if (associationRefs != null && associationRefs.size() > 0) {
                List<NodeRef> targetRefs = new ArrayList<>();

                for (AssociationRef associationRef : associationRefs) {
                    NodeRef associationRefTargetRef = associationRef.getTargetRef();
                    if (nodeRefExists(associationRefTargetRef)) {
                        targetRefs.add(associationRefTargetRef);
                    }
                }

                Map<QName, List<NodeRef>> assocs = Collections.singletonMap(pair.getValue(), targetRefs);
                RepoUtils.setAssocs(sourceRef, assocs, true, false, nodeService, null);
            }
        }
    }

    private void deleteAssocs(NodeRef sourceRef, NodeRef targetAssoc) {
        if (assocsToCopy != null && !assocsToCopy.isEmpty()) {
            for (Map.Entry<QName, QName> pair : assocsToCopy.entrySet()) {
                RepoUtils.removeAssociation(sourceRef, targetAssoc, pair.getValue(), true, nodeService);
            }
        }
    }

    private boolean nodeRefExists(NodeRef nodeRef) {
        return nodeRef != null && nodeService.exists(nodeRef);
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setTargetTypeQName(QName targetTypeQName) {
        this.targetTypeQName = targetTypeQName;
    }

    public void setSourceTypeQName(QName sourceTypeQName) {
        this.sourceTypeQName = sourceTypeQName;
    }

    public void setCaseStatusAssocQName(QName caseStatusAssocQName) {
        this.caseStatusAssocQName = caseStatusAssocQName;
    }

    public void setAssocsToCopy(Map<QName, QName> assocsToCopy) {
        this.assocsToCopy = assocsToCopy;
    }

    public void setPropsToCopy(Map<QName, QName> propsToCopy) {
        this.propsToCopy = propsToCopy;
    }

    public void setAssocTypeQName(QName assocTypeQName) {
        this.assocTypeQName = assocTypeQName;
    }
}
