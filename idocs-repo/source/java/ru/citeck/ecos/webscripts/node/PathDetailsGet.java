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
package ru.citeck.ecos.webscripts.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Repository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.webscripts.common.BaseAbstractWebscript;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class PathDetailsGet extends BaseAbstractWebscript {

    private static final String PARAM_NODE_REF = "nodeRef";

    private static final String PARAM_ROOT_NODE_PATH = "rootNodePathQname";

    private static final String STORE_ID = "SpacesStore";

    private static final String STORE_ROOT_PATH = "/";

    private static final String ENCODING = "utf-8";

    private static final String DEFAULT_ROOT = "/app:company_home";

    private NodeService nodeService;

    private SearchService searchService;

    private PermissionService permissionService;

    private String rootNodeName;

    private String escapePath;

    private FileFolderService fileFolderService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    @Override
    protected void executeInternal(WebScriptRequest request, WebScriptResponse response) throws Exception {
        NodeRef nodeRef = new NodeRef(request.getParameter(PARAM_NODE_REF));
        NodeRef rootNodeRef = getRootNodeRef(request);
        List<JSONObject> path = new ArrayList<JSONObject>();
        if (nodeService.exists(nodeRef) && (nodeService.exists(rootNodeRef))) {
            rootNodeName = nodeService.getProperty(rootNodeRef, ContentModel.PROP_NAME).toString();
            escapePath = Repository.getDisplayPath(nodeService.getPath(rootNodeRef)) + "/" + rootNodeName;
            NodeRef parentRef = getParent(nodeRef);
            if (isNotStoreRoot(parentRef)) {
                path = getPathItems(parentRef);
            }
            if (nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_ARCHIVE)) {
                path.add(0, getArchiveRoot());
            }
        }

        response.setContentType(MimetypeMap.MIMETYPE_JSON);
        response.setContentEncoding(ENCODING);

        JSONObject result = new JSONObject();
        result.put("path", path);
        result.write(response.getWriter());
    }

    private NodeRef getRootNodeRef(WebScriptRequest request) {
        String pathParameter = request.getParameter(PARAM_ROOT_NODE_PATH);
        String pathQName = pathParameter != null ? pathParameter : DEFAULT_ROOT;
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, STORE_ID);
        ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_XPATH, pathQName);
        return resultSet.length() > 0 ? resultSet.getNodeRef(0) : null;
    }

    private boolean isNotStoreRoot(NodeRef nodeRef) {
        Path parentPath = nodeService.getPath(nodeRef);
        return !parentPath.toString().equals(STORE_ROOT_PATH);
    }

    private NodeRef getParent(NodeRef nodeRef) {
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
        return parentAssoc.getParentRef();
    }

    private List<JSONObject> getPathItems(final NodeRef nodeRef) throws JSONException {
        final JSONObject pathItem = new JSONObject();
        String nodeName = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString();
        pathItem.put("name", nodeName);
        pathItem.put("isContainer", getIsContainer(nodeRef));
        pathItem.put("nodeRef", nodeRef);
        pathItem.put("displayPath", getDisplayPath(nodeRef, nodeName));
        pathItem.put("showLink", permissionService.hasPermission(nodeRef, PermissionService.READ).equals(AccessStatus.ALLOWED));
        List<JSONObject> pathItems = isNonRootNode(nodeRef, nodeName) ? getPathItems(getParent(nodeRef)) : new ArrayList<JSONObject>();
        pathItems.add(pathItem);
        return pathItems;
    }

    private Boolean getIsContainer(final NodeRef nodeRef) {
        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>() {
            public Boolean doWork() throws Exception {
                return fileFolderService.getFileInfo(nodeRef).isFolder();
            }
        });
    }

    private String getDisplayPath(NodeRef nodeRef, String nodeName) {
        String displayPath = "";
        if (isNonRootNode(nodeRef, nodeName)) {
            Path nodePath = nodeService.getPath(nodeRef);
            displayPath = nodePath.toDisplayPath(nodeService, permissionService).replace(escapePath, "") + "/" + nodeName;
        }
        return displayPath;
    }

    private boolean isNonRootNode(NodeRef nodeRef, String nodeName) {
        NodeRef parent = getParent(nodeRef);
        return parent != null && isNotStoreRoot(parent) && !nodeName.equals(rootNodeName);
    }

    private JSONObject getArchiveRoot() throws JSONException {
        JSONObject archiveRoot = new JSONObject();
        archiveRoot.put("isContainer", true);
        archiveRoot.put("showLink", true);
        archiveRoot.put("name", "");
        archiveRoot.put("nodeRef", "");
        archiveRoot.put("displayPath", "");
        return archiveRoot;
    }

}
