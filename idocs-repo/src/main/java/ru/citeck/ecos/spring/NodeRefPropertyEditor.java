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
package ru.citeck.ecos.spring;

import java.beans.PropertyEditorSupport;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;

public class NodeRefPropertyEditor extends PropertyEditorSupport {

    private SearchService searchService;
    private NodeService nodeService;
    private NamespaceService namespaceService;

    public void setAsText(String text) {
        setValue(NodeRef.isNodeRef(text) ? new NodeRef(text) : getByPath(text));
    }

    private NodeRef getByPath(String path) {

        NodeRef root = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> results = searchService.selectNodes(root, path, null,
                                                          namespaceService, false);
        if (results.isEmpty()) {
            throw new IllegalArgumentException("Node not found by path: " + path);
        }
        return results.get(0);
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        searchService = serviceRegistry.getSearchService();
        nodeService = serviceRegistry.getNodeService();
        namespaceService = serviceRegistry.getNamespaceService();
    }
}
