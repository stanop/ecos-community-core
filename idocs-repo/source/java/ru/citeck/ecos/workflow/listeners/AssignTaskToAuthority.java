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

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.context.Context;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authority.script.ScriptGroup;
import org.alfresco.repo.security.authority.script.ScriptUser;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;

/**
 * Assign task to authority task-create listener.
 * Gets 'authority' parameter - a string containing nodeRef of authority.
 * If it is nodeRef of person, task is assigned to him/she.
 * If it is nodeRef of group, it is added to candidate groups of task.
 * 
 * @author Sergey Tiunov
 */
public class AssignTaskToAuthority implements TaskListener {

	private Expression authority;
	
	@Override
	public void notify(DelegateTask delegateTask) {
		
		Object authorityObj = authority.getValue(delegateTask);

		if(authorityObj == null) return;

		ServiceRegistry services = (ServiceRegistry) 
				Context.getProcessEngineConfiguration().getBeans()
				.get(ActivitiConstants.SERVICE_REGISTRY_BEAN_KEY);
		if(services == null) return;

		NodeRef authorityRef = null;
		String authorityName = null;
		
		if(authorityObj instanceof NodeRef) {
			authorityRef = (NodeRef) authorityObj;
		} else if(authorityObj instanceof String) {
			String authorityString = (String) authorityObj;
			if(NodeRef.isNodeRef(authorityString)) {			
				authorityRef = new NodeRef((String) authorityObj);
			} else {
				authorityName = authorityString;
			}
		} else if(authorityObj instanceof ScriptGroup) {
			authorityName = ((ScriptGroup)authorityObj).getFullName();
		} else if(authorityObj instanceof ScriptUser) {
			authorityName = ((ScriptUser)authorityObj).getUserName();
		} else if(authorityObj instanceof ScriptNode) {
			authorityRef = ((ScriptNode)authorityObj).getNodeRef();
		} else {
			throw new IllegalArgumentException("Can not convert value of type " + authorityObj.getClass().getName() + " to NodeRef");
		}
		
		if(authorityName == null) {
			
			if(authorityRef == null || !services.getNodeService().exists(authorityRef)) 
				return;
			
			QName authorityType = services.getNodeService().getType(authorityRef);
			if(services.getDictionaryService().isSubClass(authorityType, ContentModel.TYPE_PERSON)) 
			{
				authorityName = (String) services.getNodeService().getProperty(authorityRef, ContentModel.PROP_USERNAME);
			}
			else if(services.getDictionaryService().isSubClass(authorityType, ContentModel.TYPE_AUTHORITY_CONTAINER))
			{
				authorityName = (String) services.getNodeService().getProperty(authorityRef, ContentModel.PROP_AUTHORITY_NAME);
			}
			else
			{
				throw new IllegalArgumentException("Unknown authority type: " + authorityType + " of node " + authorityRef);
			}
		}

		if(authorityName.startsWith(AuthorityType.GROUP.getPrefixString())) {
			// if authority is group - add to candidate groups
			delegateTask.addCandidateGroup(authorityName);
		} else {
			// if authority is person - assign task to him/she
			delegateTask.setAssignee(authorityName);
		}
		
	}

}
