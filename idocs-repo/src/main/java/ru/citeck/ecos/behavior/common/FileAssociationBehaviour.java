/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.behavior.common;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import ru.citeck.ecos.model.CiteckWorkflowModel;

import java.util.List;

/**
 * @author Anton Fateev
 */
public class FileAssociationBehaviour implements OnCreateAssociationPolicy, OnDeleteAssociationPolicy,
        OnCreateChildAssociationPolicy, OnDeleteChildAssociationPolicy {

    private PolicyComponent policyComponent;

    private NodeService nodeService;

    private QName assocName;

    private QName className;

    public void init() {
        bind(OnCreateChildAssociationPolicy.QNAME, "onCreateChildAssociation");
        bind(OnDeleteChildAssociationPolicy.QNAME, "onDeleteChildAssociation");
        bind(OnCreateAssociationPolicy.QNAME, "onCreateAssociation");
        bind(OnDeleteAssociationPolicy.QNAME, "onDeleteAssociation");
    }

    private void bind(QName policy, String method) {
        policyComponent.bindAssociationBehaviour(policy, className, assocName,
                new JavaBehaviour(this, method, NotificationFrequency.TRANSACTION_COMMIT));
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setAssocName(QName assocName) {
        this.assocName = assocName;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean isNewNode) {
        NodeRef sourceNode = childAssociationRef.getParentRef();
        NodeRef targetNode = childAssociationRef.getChildRef();
        if (nodeService.exists(sourceNode) && nodeService.exists(targetNode)) {
            appendFileAssociations(sourceNode, targetNode);
        }
    }

    @Override
    public void onDeleteChildAssociation(ChildAssociationRef childAssociationRef) {
        NodeRef sourceNode = childAssociationRef.getParentRef();
        NodeRef targetNode = childAssociationRef.getChildRef();
        if (nodeService.exists(sourceNode) && nodeService.exists(targetNode)) {
            removeFileAssociations(sourceNode, targetNode);
        }
    }

    @Override
    public void onCreateAssociation(AssociationRef associationRef) {
        NodeRef sourceNode = associationRef.getSourceRef();
        NodeRef targetNode = associationRef.getTargetRef();
        if (nodeService.exists(sourceNode) && nodeService.exists(targetNode)) {
            appendFileAssociations(sourceNode, targetNode);
        }
    }

    @Override
    public void onDeleteAssociation(AssociationRef associationRef) {
        NodeRef sourceNode = associationRef.getSourceRef();
        NodeRef targetNode = associationRef.getTargetRef();
        if (nodeService.exists(sourceNode) && nodeService.exists(targetNode)) {
            removeFileAssociations(sourceNode, targetNode);
        }
    }

    private void appendFileAssociations(NodeRef document, NodeRef additionalFile) {
        List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(document, WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef parentAssoc : parentAssocs) {
            NodeRef workflowPackage = parentAssoc.getParentRef();
            if (nodeService.exists(workflowPackage) && nodeService.hasAspect(workflowPackage, CiteckWorkflowModel.ASPECT_ATTACHED_DOCUMENT)
                    && !hasAssociation(workflowPackage, additionalFile)) {
                String additionalFileName = nodeService.getProperty(additionalFile, ContentModel.PROP_NAME).toString();
                nodeService.addChild(workflowPackage, additionalFile, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, additionalFileName));
            }
        }
    }

    private boolean hasAssociation(NodeRef parentNode, NodeRef childNode) {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(parentNode);
        for (ChildAssociationRef childAssoc : childAssocs) {
            if (childAssoc.getChildRef().equals(childNode)) {
                return true;
            }
        }
        return false;
    }

    private void removeFileAssociations(NodeRef document, NodeRef additionalFile) {
        List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(document, WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef parentAssoc : parentAssocs) {
            NodeRef workflowPackage = parentAssoc.getParentRef();
            if (nodeService.exists(workflowPackage) && nodeService.hasAspect(workflowPackage, CiteckWorkflowModel.ASPECT_ATTACHED_DOCUMENT)) {
                nodeService.removeChild(workflowPackage, additionalFile);
            }
        }
    }
}
