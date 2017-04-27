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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

import ru.citeck.ecos.utils.LazyQName;

/**
 * Child-Association-based supplementary-files DAO.
 * Access to supplementary files is based on child-associations.
 * - get: gets all specified child-associations
 * - add: moves file to document
 * - remove: removes child-association
 * 
 * @author Sergey Tiunov
 */
public class SupplementaryFilesDAOChildImpl extends SupplementaryFilesDAOAbstractImpl implements SupplementaryFilesDAO
{
	// assocTypeName - child-association, where supplementary files are stored
	// aspectTypeName - aspect, that declares child-association
	private String assocTypeName, aspectTypeName;
	private LazyQName assocTypeQName, aspectTypeQName;
	
	private NodeService nodeService;
	private NamespaceService namespaceService;
	
	public void init() {
		this.assocTypeQName = new LazyQName(namespaceService, assocTypeName);
		this.aspectTypeQName = new LazyQName(namespaceService, aspectTypeName);
	}
	
	@Override
	public List<NodeRef> getSupplementaryFiles(NodeRef document) {
		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(document, assocTypeQName.getQName(), RegexQNamePattern.MATCH_ALL);
		List<NodeRef> files = new ArrayList<NodeRef>(assocs.size());
		for(ChildAssociationRef assoc : assocs) {
			NodeRef file = assoc.getChildRef();
			if(file.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {
				files.add(file);
			}
		}
		return files;
	}

	@Override
	public List<NodeRef> getParentFiles(NodeRef document) {
		List<ChildAssociationRef> assocs = nodeService.getParentAssocs(document, assocTypeQName.getQName(), RegexQNamePattern.MATCH_ALL);
		List<NodeRef> files = new ArrayList<NodeRef>(assocs.size());
		for(ChildAssociationRef assoc : assocs) {
			NodeRef file = assoc.getParentRef();
			if(file.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {
				files.add(file);
			}
		}
		return files;
	}
	
	@Override
	public void addSupplementaryFiles(final NodeRef document, List<NodeRef> files) {
		if(files.size() > 0 && !nodeService.hasAspect(document, aspectTypeQName.getQName())) {
			AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
				public Object doWork() throws Exception {
					nodeService.addAspect(document, aspectTypeQName.getQName(), Collections.<QName, Serializable> emptyMap());
					return null;
				}
			});
		}
		for(NodeRef file : files) {
			ChildAssociationRef parent = nodeService.getPrimaryParent(file);
			nodeService.moveNode(file, document, assocTypeQName.getQName(), parent.getQName());
		}
	}

	@Override
	public void removeSupplementaryFiles(NodeRef document, List<NodeRef> files) {
		for(NodeRef file : files) {
			List<ChildAssociationRef> parents = nodeService.getParentAssocs(file, assocTypeQName.getQName(), RegexQNamePattern.MATCH_ALL);
			for(ChildAssociationRef parent : parents) {
				if(parent.getParentRef().equals(document)) {
					nodeService.removeChildAssociation(parent);
					break;
				}
			}
		}
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setAssocTypeName(String assocTypeName) {
		this.assocTypeName = assocTypeName;
	}

	public void setAspectTypeName(String aspectTypeName) {
		this.aspectTypeName = aspectTypeName;
	}

}
