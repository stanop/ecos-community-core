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

import java.util.Map;

import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Behaviour of 'granted' aspect.
 * When adding (removing) association, adds (removes) permissions to (from) it.
 * When copying file (except working copies), revokes all granted permissions.
 * 
 * @author Sergey Tiunov
 *
 */
public class GrantedBehaviour extends AssociationWalkerBehaviour implements CopyServicePolicies.OnCopyCompletePolicy
{
	private GrantPermissionServiceImpl grantPermissionService;

	private boolean enabled;

	@Override
	public void init() {
		super.init();
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, className, 
				new JavaBehaviour(this, "onCopyComplete", NotificationFrequency.TRANSACTION_COMMIT));
	}

	public void setGrantPermissionService(GrantPermissionServiceImpl grantPermissionService) {
		this.grantPermissionService = grantPermissionService;
	}

	@Override
	protected void onCreateAssociation(NodeRef child, NodeRef parent, boolean primary) {
		if (!enabled) {
			return;
		}

		grantPermissionService.grantPermissionsImpl(child, parent);
	}

	@Override
	protected void onDeleteAssociation(NodeRef child, NodeRef parent, boolean primary) {
		if (!enabled) {
			return;
		}

		grantPermissionService.revokePermissionsImpl(child, parent);
	}

	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef,
			NodeRef targetNodeRef, boolean copyToNewNode,
			Map<NodeRef, NodeRef> copyMap)
	{
		if (!enabled) {
			return;
		}

		grantPermissionService.revokePermissionsOnCopy(sourceNodeRef, targetNodeRef);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
