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
package ru.citeck.ecos.workflow.listeners;

import java.util.HashSet;
import java.util.Set;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Assigns task to a user, if there is only one candidate user.
 *
 * @author Sergey Tiunov
 */
public class AssignTaskToSingleCandidate implements TaskListener
{
	private AuthorityService authorityService;
	
	@Override
	public void notify(DelegateTask delegateTask) {
		// if task is already assigned - there is nothing to do
		if(delegateTask.getAssignee() != null) {
			return;
		}

		// otherwise - get candidate groups and users
		// note: cast to TaskEntity is required for compatibility with Alfresco 4.0.c
		Set<IdentityLink> candidates = ((TaskEntity) delegateTask).getCandidates();

		// resolve users from groups
		Set<String> userNames = getCandidateUsers(candidates);

		// assign, if there is only one candidate user
		if(userNames.size() == 1) {
			delegateTask.setAssignee(userNames.iterator().next());
		}
	}

	// resolve IdentityLinks to userNames
	private Set<String> getCandidateUsers(Set<IdentityLink> candidates) {
		Set<String> userNames = new HashSet<String>();
		for(IdentityLink candidate : candidates) {
			String groupId = candidate.getGroupId(),
					userId = candidate.getUserId();
			if(groupId != null) {
				Set<String> users = authorityService.getContainedAuthorities(AuthorityType.USER, groupId, false);
				userNames.addAll(users);
			}
			if(userId != null) {
				userNames.add(userId);
			}
		}
		return userNames;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}
	
}
