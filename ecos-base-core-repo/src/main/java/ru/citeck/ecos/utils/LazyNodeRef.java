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
package ru.citeck.ecos.utils;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.context.ApplicationContext;

/**
 * Lazy nodeRef from path initializer.
 * Useful, because SearchService is not accessible at startup.
 * 
 * Though you should avoid using it and better use Spring Property Editors (see package ru.citeck.ecos.spring).
 * 
 * @author Sergey Tiunov
 *
 */
public class LazyNodeRef {

	private NodeRef nodeRef;
	private String path;
	private ApplicationContext applicationContext;
	private NodeService nodeService;
	private SearchService searchService;
	private NamespaceService namespaceService;
	private String nodeServiceName;
	private String searchServiceName;
	private String namespaceServiceName;

	/**
	 * Constructor
	 * @param searchService
	 * @param path - xpath to node
	 */
	public LazyNodeRef(ApplicationContext applicationContext, String path) {
		this.applicationContext = applicationContext;
		this.path = path;
	}
	
	/** 
	 * Get NodeRef value.
	 * @return
	 */
	public synchronized NodeRef getNodeRef() {
		if(nodeRef == null || getNodeService().exists(nodeRef)) {
		    NodeRef root = getNodeService().getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
		    List<NodeRef> results = getSearchService().selectNodes(root, path, null, getNamespaceService(), false);
			if(results.size() > 0) {
				nodeRef = results.get(0);
			} else {
				nodeRef = null;
			}
		}
		return nodeRef;
	}

	private NodeService getNodeService() {
		if(nodeService == null) {
			nodeService = applicationContext.getBean(nodeServiceName, NodeService.class);
		}
		return nodeService;
	}

    private SearchService getSearchService() {
        if(searchService == null) {
            searchService = applicationContext.getBean(searchServiceName, SearchService.class);
        }
        return searchService;
    }

    private NamespaceService getNamespaceService() {
        if(namespaceService == null) {
            namespaceService = applicationContext.getBean(namespaceServiceName, NamespaceService.class);
        }
        return namespaceService;
    }

	public void setNodeServiceName(String nodeServiceName) {
		this.nodeServiceName = nodeServiceName;
	}

    public void setSearchServiceName(String searchServiceName) {
        this.searchServiceName = searchServiceName;
    }

    public void setNamespaceServiceName(String namespaceServiceName) {
        this.namespaceServiceName = namespaceServiceName;
    }

}
