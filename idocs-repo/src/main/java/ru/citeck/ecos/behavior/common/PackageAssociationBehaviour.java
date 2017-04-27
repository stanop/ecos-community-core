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
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.CiteckWorkflowModel;
import ru.citeck.ecos.model.DmsModel;

/**
 * @author Anton Fateev
 */
public class PackageAssociationBehaviour implements OnCreateChildAssociationPolicy, OnDeleteChildAssociationPolicy {

    private PolicyComponent policyComponent;

    private NodeService nodeService;

    private QName className;

    public void init() {
        bind(OnCreateChildAssociationPolicy.QNAME, "onCreateChildAssociation");
        bind(OnDeleteChildAssociationPolicy.QNAME, "onDeleteChildAssociation");
    }

    private void bind(QName policy, String method) {
        policyComponent.bindAssociationBehaviour(policy, className,
                new JavaBehaviour(this, method, Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean isNewNode) {
        NodeRef parentNode = childAssociationRef.getParentRef();
        NodeRef childNode = childAssociationRef.getChildRef();
        if (nodeService.exists(parentNode) && isPackageContent(childNode, childAssociationRef.getTypeQName())) {
            appendFileAssociation(parentNode, childNode);
        }
    }

    @Override
    public void onDeleteChildAssociation(ChildAssociationRef childAssociationRef) {
        NodeRef parentNode = childAssociationRef.getParentRef();
        NodeRef childNode = childAssociationRef.getChildRef();
        if (nodeService.exists(parentNode) && isPackageContent(childNode, childAssociationRef.getTypeQName())) {
            removeFileAssociation(parentNode, childNode);
        }
    }

    private void appendFileAssociation(NodeRef workflowPackage, NodeRef additionalFile) {
        NodeRef document = (NodeRef) nodeService.getProperty(workflowPackage, CiteckWorkflowModel.PROP_ATTACHED_DOCUMENT);
        if (nodeService.exists(document)) {
            nodeService.createAssociation(document, additionalFile, DmsModel.ASSOC_AGREEMENT_TO_FILES);
        }
    }

    private void removeFileAssociation(NodeRef workflowPackage, NodeRef additionalFile) {
        NodeRef document = (NodeRef) nodeService.getProperty(workflowPackage, CiteckWorkflowModel.PROP_ATTACHED_DOCUMENT);
        if (nodeService.exists(document)) {
            nodeService.removeAssociation(document, additionalFile, DmsModel.ASSOC_AGREEMENT_TO_FILES);
        }
    }

    private boolean isPackageContent(NodeRef childNode, QName assocType) {
        return nodeService.exists(childNode)
                && assocType.equals(WorkflowModel.ASSOC_PACKAGE_CONTAINS)
                && nodeService.getType(childNode).equals(ContentModel.TYPE_CONTENT);
    }
}
