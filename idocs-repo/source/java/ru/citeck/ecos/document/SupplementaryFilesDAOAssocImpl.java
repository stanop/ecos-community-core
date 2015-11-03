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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;

import ru.citeck.ecos.utils.LazyQName;

/**
 * Association-based supplementary-files DAO.
 * Access to supplementary files is based on associations.
 * - get: gets all target associations of specified type
 * - add: adds target association
 * - remove: removes target association
 * 
 * @author Sergey Tiunov
 */
public class SupplementaryFilesDAOAssocImpl extends SupplementaryFilesDAOAbstractImpl implements SupplementaryFilesDAO
{
	private String assocTypeName;
	private LazyQName assocTypeQName;
	
	private NodeService nodeService;
	private NamespaceService namespaceService;
	
	public void init() {
		this.assocTypeQName = new LazyQName(namespaceService, assocTypeName);
	}
	
	@Override
	public List<NodeRef> getSupplementaryFiles(NodeRef document) {
		List<AssociationRef> assocs = nodeService.getTargetAssocs(document, assocTypeQName.getQName());
		List<NodeRef> files = new ArrayList<NodeRef>(assocs.size());
		for(AssociationRef assoc : assocs) {
			NodeRef file = assoc.getTargetRef();
			if(file.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {
				files.add(file);
			}
		}
		return files;
	}
	
	@Override
	public List<NodeRef> getParentFiles(NodeRef document) {
		List<AssociationRef> assocs = nodeService.getSourceAssocs(document, assocTypeQName.getQName());
		List<NodeRef> files = new ArrayList<NodeRef>(assocs.size());
		for(AssociationRef assoc : assocs) {
			NodeRef file = assoc.getSourceRef();
			if(file.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {
				files.add(file);
			}
		}
		return files;
	}

	@Override
	public void addSupplementaryFiles(NodeRef document, List<NodeRef> files) {
		for(NodeRef file : files) {
			nodeService.createAssociation(document, file, assocTypeQName.getQName());
		}
	}

	@Override
	public void removeSupplementaryFiles(NodeRef document, List<NodeRef> files) {
		for(NodeRef file : files) {
			nodeService.removeAssociation(document, file, assocTypeQName.getQName());
		}
	}

	public void setAssocTypeName(String assocTypeName) {
		this.assocTypeName = assocTypeName;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

}
