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
package ru.citeck.ecos.icase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

import ru.citeck.ecos.icase.AssociationCaseElementDAOImpl.AssociationType;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.utils.RepoUtils;

public class CaseUtils {

	/**
	 * It returns case folder of the folder case element services.
	 * @param caseNode - node reference of the case.
	 * @param config - node reference of the case configuration
	 * @param nodeService - node service
	 * @return node reference of case folder or {@code null} if it is not
	 * exist.
	 */
	public static NodeRef getCaseFolder(
			NodeRef caseNode,
			NodeRef config,
			NodeService nodeService) {
		String folderName = RepoUtils.getProperty(config, ICaseModel.PROP_FOLDER_NAME, nodeService);
		if(folderName == null) return null;
		
		QName folderAssocType = RepoUtils.getProperty(config, ICaseModel.PROP_FOLDER_ASSOC_TYPE, nodeService);
		if(folderAssocType == null) return null;
		
		return nodeService.getChildByName(caseNode, folderAssocType, folderName);
	}

	public static NodeRef createCaseFolder(
			NodeRef caseNode,
			NodeRef config,
			NodeService nodeService) {

		String folderName = RepoUtils.getProperty(config, ICaseModel.PROP_FOLDER_NAME, nodeService);
		if(folderName == null) return null;
		
		QName folderType = RepoUtils.getProperty(config, ICaseModel.PROP_FOLDER_TYPE, nodeService);
		if(folderType == null) return null;

		QName assocType = RepoUtils.getProperty(config, ICaseModel.PROP_FOLDER_ASSOC_TYPE, nodeService);
		if(assocType == null) return null;
		
		QName assocName = QName.createQNameWithValidLocalName(NamespaceService.CONTENT_MODEL_1_0_URI, folderName);
		Map<QName, Serializable> properties = Collections.<QName, Serializable>singletonMap(ContentModel.PROP_NAME, folderName);
		ChildAssociationRef folderRef = nodeService.createNode(caseNode, assocType, assocName, folderType, properties);

		return folderRef.getChildRef();
	}

	public static List<NodeRef> getSubFolders(
			NodeRef folder,
			FileFolderService fileFolderService) {

		List<FileInfo> folders = fileFolderService.listFolders(folder);
		List<NodeRef> result = new ArrayList<NodeRef>(folders.size());
		for(FileInfo folderInfo : folders)
			result.add(folderInfo.getNodeRef());
		return result;
	}

	public static List<NodeRef> getElements(
			NodeRef folder,
			NodeRef config,
			NodeService nodeService) {

		QName elementType = RepoUtils.getMandatoryProperty(config, ICaseModel.PROP_ELEMENT_TYPE, nodeService);
		QName folderAssocType = RepoUtils.getMandatoryProperty(config, ICaseModel.PROP_FOLDER_ASSOC_TYPE, nodeService);
		List<ChildAssociationRef> elements = nodeService.getChildAssocs(
				folder,
				folderAssocType,
				RegexQNamePattern.MATCH_ALL);
		List<NodeRef> result = new ArrayList<NodeRef>(elements.size());
		for (ChildAssociationRef element : elements) {
			NodeRef elementRef = element.getChildRef();
			if (nodeService.getType(elementRef).equals(elementType))
				result.add(elementRef);
		}
		return result;
	}

	public static NodeRef getCaseKindFolder(
			NodeRef caseNode,
			NodeRef config,
			NodeRef kind,
			NodeService nodeService,
			FileFolderService fileFolderService) {

		NodeRef result = null;
		NodeRef folder = CaseUtils.getCaseFolder(caseNode, config, nodeService);
		if (folder == null)
			folder = CaseUtils.createCaseFolder(caseNode, config, nodeService);

		if (kind == null) {
			result = folder;
		}
		else {
			String name = RepoUtils.getMandatoryProperty(kind, ContentModel.PROP_NAME, nodeService);
			List<FileInfo> subFolders = fileFolderService.listFolders(folder);
			for (FileInfo subFolder : subFolders) {
				if (name.equals(subFolder.getName())) {
					result = subFolder.getNodeRef();
					break;
				}
			}
		}
		return result;
	}

	public static NodeRef createCaseKindFolder(
			NodeRef caseNode,
			NodeRef config,
			NodeRef kind,
			NodeService nodeService,
			FileFolderService fileFolderService) {

		NodeRef result = null;
		NodeRef folder = CaseUtils.getCaseFolder(caseNode, config, nodeService);
		if (folder == null)
			folder = CaseUtils.createCaseFolder(caseNode, config, nodeService);

		if (kind == null) {
			result = folder;
		}
		else {
			String name = RepoUtils.getMandatoryProperty(kind, ContentModel.PROP_NAME, nodeService);
			FileInfo subFolder = fileFolderService.create(folder, name, ContentModel.TYPE_FOLDER);
			result = subFolder.getNodeRef();
		}
		return result;
	}

	public static List<NodeRef> getConfigs(
			NodeRef caseNodeRef,
			CaseElementService caseElementService) {
	    return caseElementService.getElements(caseNodeRef, CaseConstants.ELEMENT_TYPES);
	}
	
    public static NodeRef getConfigByPropertyValue(NodeRef caseNodeRef,
            QName propertyName, Object value, NodeService nodeService, CaseElementService caseElementService) {
        NodeRef result = null;
        List<NodeRef> configs = getConfigs(caseNodeRef, caseElementService);
        for (NodeRef config: configs) {
            Serializable prop = nodeService.getProperty(config, propertyName);
            if (prop == null && value == null || prop != null && prop.equals(value)) {
                result = config;
                break;
            }
        }
        return result;
    }

    public static List<NodeRef> getConfigsByPropertyValue(NodeRef caseNodeRef,
            QName propertyName, Object requiredValue, NodeService nodeService, CaseElementService caseElementService) {
        List<NodeRef> configs = getConfigs(caseNodeRef, caseElementService);
        if(configs.isEmpty()) return Collections.emptyList();
        List<NodeRef> result = new LinkedList<>();
        for (NodeRef config: configs) {
            Serializable configValue = nodeService.getProperty(config, propertyName);
            if (configValue == null && requiredValue == null || configValue != null && configValue.equals(requiredValue)) {
                result.add(config);
            }
        }
        return result.isEmpty() ? Collections.<NodeRef>emptyList() : result;
    }

	public static NodeRef getFolderByPath(
			String elementFolder,
			SearchService searchService) {

		NodeRef result = null;
		ResultSet rs = null;
		try {
			rs = searchService.query(
					StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
					SearchService.LANGUAGE_LUCENE,
					"PATH:\"" + elementFolder + "\"");
			result = rs.getNodeRef(0);
		}
		finally {
			if (rs != null)
				rs.close();
		}
		return result;
	}

	public static ChildAssociationRef getCaseReference(
			NodeRef nodeRef,
			NodeRef caseNode,
			NodeRef config,
			NodeService nodeService) {

		ChildAssociationRef result = null;
		NodeRef parent = CaseUtils.getCaseFolder(caseNode, config, nodeService);
		List<ChildAssociationRef> assocs = nodeService.getParentAssocs(nodeRef);
		for (ChildAssociationRef assoc : assocs) {
			if (RepoUtils.hasParent(assoc.getParentRef(), parent, ContentModel.ASSOC_CONTAINS, true, nodeService)) {
				result = assoc;
				break;
			}
		}
		return result;
	}

    public static QName getElementConfigAssocName(NodeRef config, NodeService nodeService) {
        return (QName) nodeService.getProperty(config, ICaseModel.PROP_ASSOC_NAME);
    }

    public static CaseElementDAO getStrategy(NodeRef config, CaseElementServiceImpl caseElementService) {
        return caseElementService.getStrategy(config);
    }

}
