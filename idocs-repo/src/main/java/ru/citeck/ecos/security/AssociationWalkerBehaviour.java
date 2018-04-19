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
package ru.citeck.ecos.security;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public abstract class AssociationWalkerBehaviour implements 
	OnCreateAssociationPolicy, 
	OnDeleteAssociationPolicy,
	OnCreateChildAssociationPolicy,
	OnDeleteChildAssociationPolicy
{
    protected PolicyComponent policyComponent;
    protected QName className;
    private AssociationWalker walker;

	/////////////////////////////////////////////////////////////////
	//                     SPRING INTERFACE                        //
	/////////////////////////////////////////////////////////////////

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
	public void setClassName(QName className) {
		this.className = className;
	}

	public void setWalker(AssociationWalker walker) {
		this.walker = walker;
	}

	public void init() {
		
		if(walker.getPropagatePrimaryChildAssociations()
		|| walker.getPropagateSecondaryChildAssociations())
		{
			bind(OnCreateChildAssociationPolicy.QNAME, "onCreateChildAssociation");
			bind(OnDeleteChildAssociationPolicy.QNAME, "onDeleteChildAssociation");
		}
		
		if(walker.getPropagateTargetAssociations()) 
		{
			bind(OnCreateAssociationPolicy.QNAME, "onCreateAssociation");
			bind(OnDeleteAssociationPolicy.QNAME, "onDeleteAssociation");
		}
	}
	
	private void bind(QName policy, String method) {
		policyComponent.bindAssociationBehaviour(policy, className, 
				new JavaBehaviour(this, method, NotificationFrequency.TRANSACTION_COMMIT));
	}
	
	/////////////////////////////////////////////////////////////////
	//                   ABSTRACT IMPLEMENTATION                   //
	/////////////////////////////////////////////////////////////////

	/**
	 * Event handler - association was created.
	 * @param child
	 * @param parent
	 */
	protected abstract void onCreateAssociation(NodeRef child, NodeRef parent, boolean primary);
	
	/**
	 * Event handler - association was deleted.
	 * @param child
	 * @param parent
	 */
	protected abstract void onDeleteAssociation(NodeRef child, NodeRef parent, boolean primary);

	/////////////////////////////////////////////////////////////////
	//                   BEHAVIOUR IMPLEMENTATION                  //
	/////////////////////////////////////////////////////////////////

	@Override
	public void onDeleteChildAssociation(ChildAssociationRef childAssocRef) {
		if(childAssocRef.isPrimary() && walker.getPropagatePrimaryChildAssociations()
		|| !childAssocRef.isPrimary() && walker.getPropagateSecondaryChildAssociations())
		{
			this.onDeleteAssociation(childAssocRef.getChildRef(), childAssocRef.getParentRef(), childAssocRef.isPrimary());
		}
	}

	@Override
	public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
		if(childAssocRef.isPrimary() && walker.getPropagatePrimaryChildAssociations()
		|| !childAssocRef.isPrimary() && walker.getPropagateSecondaryChildAssociations())
		{
			this.onCreateAssociation(childAssocRef.getChildRef(), childAssocRef.getParentRef(), childAssocRef.isPrimary());
		}
	}

	@Override
	public void onDeleteAssociation(AssociationRef nodeAssocRef) {
		this.onDeleteAssociation(nodeAssocRef.getTargetRef(), nodeAssocRef.getSourceRef(), false);
	}

	@Override
	public void onCreateAssociation(AssociationRef nodeAssocRef) {
		this.onCreateAssociation(nodeAssocRef.getTargetRef(), nodeAssocRef.getSourceRef(), false);
	}

}
