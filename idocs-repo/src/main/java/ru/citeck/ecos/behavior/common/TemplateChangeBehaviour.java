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

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.DmsModel;
import ru.citeck.ecos.template.GenerateContentActionExecuter;

import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;

/**
 * This is a behavior class. It adds or removes aspect corresponded to
 * the added or removed template.
 * 
 * @author Alexander Nemerov
 * @date 26.07.13
 */
public class TemplateChangeBehaviour implements NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnDeleteAssociationPolicy,
		NodeServicePolicies.OnAddAspectPolicy,
		NodeServicePolicies.BeforeRemoveAspectPolicy {

	private NodeService nodeService;
	private PolicyComponent policyComponent;
	private ServiceRegistry serviceRegistry;

	public void init() {
		this.policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				DmsModel.ASPECT_TEMPLATEABLE,
				DmsModel.ASSOC_TEMPLATE,
				new JavaBehaviour(this, "onCreateAssociation",
						Behaviour.NotificationFrequency.TRANSACTION_COMMIT
				)
		);
		this.policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
				DmsModel.ASPECT_TEMPLATEABLE,
				DmsModel.ASSOC_TEMPLATE,
				new JavaBehaviour(this, "onDeleteAssociation",
						Behaviour.NotificationFrequency.TRANSACTION_COMMIT
				)
		);
		this.policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnAddAspectPolicy.QNAME,
				DmsModel.ASPECT_TEMPLATEABLE,
				new JavaBehaviour( this, "onAddAspect",
						Behaviour.NotificationFrequency.TRANSACTION_COMMIT
				)
		);
		this.policyComponent.bindClassBehaviour(
				NodeServicePolicies.BeforeRemoveAspectPolicy.QNAME,
				DmsModel.ASPECT_TEMPLATEABLE,
				new JavaBehaviour(this, "beforeRemoveAspect",
						Behaviour.NotificationFrequency.EVERY_EVENT
				)
		);
	}

	@Override
	public void onDeleteAssociation(AssociationRef nodeAssocRef) {
		NodeRef sourceRef = nodeAssocRef.getSourceRef();
		NodeRef templateRef = nodeAssocRef.getTargetRef();
		if (sourceRef != null && nodeService.exists(sourceRef) && nodeService.exists(templateRef))
			removeTemplateTypeAspect(sourceRef, templateRef);
	}

	@Override
	public void onCreateAssociation(AssociationRef nodeAssocRef) {
		NodeRef sourceRef = nodeAssocRef.getSourceRef();
		NodeRef templateRef = nodeAssocRef.getTargetRef();
		if (sourceRef != null && nodeService.exists(sourceRef) && nodeService.exists(templateRef)) {
			addTemplateTypeAspect(sourceRef, templateRef);
			if (Boolean.TRUE.equals(nodeService.getProperty(sourceRef, DmsModel.PROP_UPDATE_CONTENT))) {
				ActionService actionService = serviceRegistry.getActionService();
				Action actionGenerateContent = actionService.createAction(GenerateContentActionExecuter.NAME);
				actionService.executeAction(actionGenerateContent, sourceRef);
			}
		}
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		if (!nodeService.exists(nodeRef))
			return;

		List<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, DmsModel.ASSOC_TEMPLATE);
		if (assocs != null && assocs.size() > 0) {
			NodeRef template = assocs.get(0).getTargetRef();
			addTemplateTypeAspect(nodeRef, template);
		}
	}

	@Override
	public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
		if (!nodeService.exists(nodeRef))
			return;

		List<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, DmsModel.ASSOC_TEMPLATE);
		if (assocs != null && assocs.size() > 0) {
			NodeRef template = assocs.get(0).getTargetRef();
			removeTemplateTypeAspect(nodeRef, template);
		}
	}

	private void removeTemplateTypeAspect(NodeRef thisNodeRef, NodeRef templateRef) {
		QName oldAspect = (QName) nodeService.getProperty(templateRef, DmsModel.PROP_ASPECT);
		if (oldAspect == null)
			return;

		nodeService.removeAspect(thisNodeRef, oldAspect);
	}

	private void addTemplateTypeAspect(NodeRef thisNodeRef, NodeRef templateRef) {
		QName newAspect = (QName) nodeService.getProperty(templateRef, DmsModel.PROP_ASPECT);
		if (newAspect == null)
			return;

		nodeService.addAspect(thisNodeRef, newAspect, new TreeMap<QName, Serializable>());
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
