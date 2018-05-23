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
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Confiscated aspect behaviour - maintains confiscated state of all child objects.
 * When copying file, resets "inheritsPermissions" flag of new object.
 * 
 * @author Sergey Tiunov
 * //TODO: Remove?
 */
public class ConfiscatedBehaviour extends AssociationWalkerBehaviour implements CopyServicePolicies.OnCopyCompletePolicy
{
	private ConfiscateServiceImpl confiscateService;

	@Override
	public void init() {
		super.init();
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, className,
				new JavaBehaviour(this, "onCopyComplete", NotificationFrequency.TRANSACTION_COMMIT));
	}

	public void setConfiscateService(ConfiscateServiceImpl confiscateService) {
		this.confiscateService = confiscateService;
	}

	@Override
	protected void onCreateAssociation(final NodeRef child, NodeRef parent, final boolean primary) {
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {

			@Override
			public Object doWork() throws Exception {
				// do not restrict inheritance on primary associations:
				boolean restrictInheritance = !primary;
				confiscateService.confiscateNodeImpl(child, restrictInheritance);
				return null;
			}

		});
	}

	@Override
	protected void onDeleteAssociation(final NodeRef child, NodeRef parent, boolean primary) {
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {

			@Override
			public Object doWork() throws Exception {
				confiscateService.returnNodeImpl(child, true);
				return null;
			}

		});
	}

	@Override
	public void onCopyComplete(QName classRef, NodeRef source, final NodeRef target,
			boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {

			@Override
			public Object doWork() throws Exception {
				confiscateService.returnNodeImpl(target, false);
				return null;
			}

		});
	}

}
