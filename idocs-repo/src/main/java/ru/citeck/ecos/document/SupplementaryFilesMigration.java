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
package ru.citeck.ecos.document;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

public class SupplementaryFilesMigration extends BaseScopableProcessorExtension {
	
	private NodeService nodeService;
	private Map<String,SupplementaryFilesDAO> daos;
	
	public void migrate(NodeRef nodeRef, String oldScheme, String newScheme) 
	{
		if(!nodeService.exists(nodeRef)) {
			throw new IllegalArgumentException("Illegal nodeRef: " + nodeRef);
		}
		SupplementaryFilesDAO oldDao = daos.get(oldScheme);
		SupplementaryFilesDAO newDao = daos.get(newScheme);
		if(oldDao == null) {
			throw new IllegalArgumentException("Scheme not found: " + oldScheme);
		}
		if(newDao == null) {
			throw new IllegalArgumentException("Scheme not found: " + newScheme);
		}
		
		List<NodeRef> files = oldDao.getSupplementaryFiles(nodeRef);
		newDao.addSupplementaryFiles(nodeRef, files);
	}
	
	public void migrate(ScriptNode node, String oldScheme, String newScheme) {
		this.migrate(node.getNodeRef(), oldScheme, newScheme);
	}

	public void setDaos(Map<String,SupplementaryFilesDAO> daos) {
		this.daos = daos;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	
}
