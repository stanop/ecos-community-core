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
package ru.citeck.ecos.workflow.activiti;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

public class ActivitiScriptUtils extends BaseScopableProcessorExtension
{
	private ServiceRegistry serviceRegistry;

	public Object getSerializable(Object someObject) {
		if(someObject instanceof ActivitiScriptNode) {
			return someObject;
		}
		if(someObject instanceof ScriptNode) {
			ScriptNode node = (ScriptNode) someObject;
			return new ActivitiScriptNode(node.getNodeRef(), serviceRegistry);
		}
		if(someObject instanceof NodeRef) {
			NodeRef node = (NodeRef) someObject;
			return new ActivitiScriptNode(node, serviceRegistry);
		}
		
		return someObject;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
}
