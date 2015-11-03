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
package ru.citeck.ecos.orgstruct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Implementation of GroupSubTypeDAO.
 * All sub-types are nodes in repository and they:
 * 1) have type 'typeName';
 * 2) have applied aspect 'aspectName';
 * 3) are children of container 'rootNode'; 
 * 4) via child-association 'assocName'.
 * This implementation allows almost any representation of subtypes, 
 * including data-lists, categories, raw cm:cmobject nodes, and so on.
 * 
 * @author Sergey Tiunov
 *
 */
public class GroupSubTypeDAOImpl implements GroupSubTypeDAO {

	private NodeService nodeService;
	private QName assocName;
	private QName typeName;
	private QName aspectName;
	private NodeRef rootNode;

	/////////////////////////////////////////////////////////////////
	//                     SPRING INTERFACE                        //
	/////////////////////////////////////////////////////////////////
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	public void setAssocName(QName assocName) {
		this.assocName = assocName;
	}
	public void setTypeName(QName typeName) {
		this.typeName = typeName;
	}
	public void setAspectName(QName aspectName) {
		this.aspectName = aspectName;
	}
	public void setRootNode(NodeRef rootNode) {
		this.rootNode = rootNode;
	}

	/////////////////////////////////////////////////////////////////
	//              GroupSubTypeDAO IMPLEMENTATION                 //
	/////////////////////////////////////////////////////////////////

	@Override
	public NodeRef getSubType(String name) {
		NodeRef existing = null;
		List<ChildAssociationRef> children = nodeService.getChildAssocs(rootNode, assocName, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name));
		if(children.size() > 0) {
			existing = children.get(0).getChildRef();
		}
		return existing != null && nodeService.exists(existing) ? existing : null;
	}
	
	@Override
	public List<NodeRef> getAllSubTypes() {
		List<NodeRef> all = new ArrayList<NodeRef>();
		List<ChildAssociationRef> children = nodeService.getChildAssocs(rootNode, assocName, RegexQNamePattern.MATCH_ALL);
		for(ChildAssociationRef child : children) {
			all.add(child.getChildRef());
		}
		return all;
	}

	@Override
	public NodeRef createSubType(String name) {
		NodeRef existing = getSubType(name);
		if(existing != null) {
			return existing;
		}
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, name);
		ChildAssociationRef child = nodeService.createNode(rootNode, assocName, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), typeName, properties);
		if(child == null) {
			return null;
		}
		nodeService.addAspect(child.getChildRef(), aspectName, new HashMap<QName, Serializable>());
		return child.getChildRef();
	}
	
	@Override
	public void deleteSubType(String name) {
		NodeRef existing = getSubType(name);
		if(existing != null) {
			nodeService.deleteNode(existing);
		}
	}
	@Override
	public NodeRef getSubTypeRoot() {
		return this.rootNode;
	}

}
