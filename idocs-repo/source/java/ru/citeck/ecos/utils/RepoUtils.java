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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

import ru.citeck.ecos.model.IdocsTemplateModel;

public class RepoUtils {

	/**
	 * It returns a property value or throws an exception.
	 * 
	 * @param nodeRef
	 *            - node reference
	 * @param name
	 *            - name of the property of specified node
	 * @param nodeService
	 *            - node service
	 * @return value of the property of the node
	 * @throws IllegalArgumentException
	 *             it throws this exception when required parameters are not
	 *             specified or specified node reference is not exist or
	 *             specified property is not exist in the specified node
	 *             reference.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getMandatoryProperty(NodeRef nodeRef, QName name, NodeService nodeService) throws IllegalArgumentException {

		if (nodeRef == null || name == null || nodeService == null)
			throw new IllegalArgumentException("One of required parameters are not specified. nodeRef=" + nodeRef + "; name=" + name
					+ "; nodeService=" + nodeService);
		if (!nodeService.exists(nodeRef))
			throw new IllegalArgumentException("Specified node reference is not exist. nodeRef=" + nodeRef);
		Serializable object = nodeService.getProperty(nodeRef, name);
		if (object == null)
			throw new IllegalArgumentException("Can not get property name=" + name + " from nodeRef=" + nodeRef);
		return (T) object;
	}

	/**
	 * It returns a property value or {@code null} if it is not set.
	 * 
	 * @param nodeRef
	 *            - node reference
	 * @param name
	 *            - name of the property of specified node
	 * @param nodeService
	 *            - node service
	 * @return value of the property of the node
	 * @throws IllegalArgumentException
	 *             it throws this exception when required parameters are not
	 *             specified or specified node reference is not exist.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProperty(NodeRef nodeRef, QName name, NodeService nodeService) throws IllegalArgumentException {

		if (nodeRef == null || name == null || nodeService == null)
			throw new IllegalArgumentException("One of required parameters are not specified. nodeRef=" + nodeRef + "; name=" + name
					+ "; nodeService=" + nodeService);
		if (!nodeService.exists(nodeRef))
			throw new IllegalArgumentException("Specified node reference is not exist. nodeRef=" + nodeRef);
		Serializable object = nodeService.getProperty(nodeRef, name);
		return (T) object;
	}

	public static NodeRef getSubFolder(NodeRef parentNode, QName assocType, String name, NodeService nodeService) {

		NodeRef result = null;
		List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(parentNode, assocType, RegexQNamePattern.MATCH_ALL);
		for (ChildAssociationRef childAssoc : childAssocs) {
			NodeRef assocRef = childAssoc.getChildRef();
			String assocName = getMandatoryProperty(assocRef, ContentModel.PROP_NAME, nodeService);
			if (assocName != null && assocName.equals(name)) {
				result = assocRef;
				break;
			}
		}
		return result;
	}

	public static NodeRef getSubFolder(NodeRef parentFolder, String name, FileFolderService fileFolderService) {

		NodeRef result = null;
		List<FileInfo> subFolders = fileFolderService.listFolders(parentFolder);
		for (FileInfo subFolder : subFolders) {
			if (name.equals(subFolder.getName())) {
				result = subFolder.getNodeRef();
				break;
			}
		}
		return result;
	}

	public static NodeRef createSubFolder(NodeRef parentNode, QName assocType, String name, NodeService nodeService) {

		QName assocName = QName.createQNameWithValidLocalName(NamespaceService.CONTENT_MODEL_1_0_URI, name);
		Map<QName, Serializable> properties = Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, name);
		ChildAssociationRef folderRef = nodeService.createNode(parentNode, assocType, assocName, ContentModel.TYPE_FOLDER, properties);
		return folderRef.getChildRef();
	}

	public static NodeRef createSubFolder(NodeRef parentFolder, String name, FileFolderService fileFolderService) {

		FileInfo fileInfo = fileFolderService.create(parentFolder, name, ContentModel.TYPE_FOLDER);
		return fileInfo.getNodeRef();
	}
	
    public static NodeRef getChildByName(NodeRef parentRef, QName assocQName, String childName, NodeService nodeService) {
        return nodeService.getChildByName(parentRef, assocQName, childName);
    }
    
    public static NodeRef createChildWithName(NodeRef parentRef, QName assocQName, QName typeQName, String childName, NodeService nodeService) {
        ChildAssociationRef childAssoc = nodeService.createNode(parentRef, 
                assocQName, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(childName)), 
                typeQName, 
                Collections.<QName, Serializable>singletonMap(ContentModel.PROP_NAME, childName));
        return childAssoc.getChildRef();
    }
    
    public static NodeRef getOrCreateChildByName(NodeRef parentRef, QName assocQName, QName typeQName, String childName, NodeService nodeService) {
        NodeRef child = getChildByName(parentRef, assocQName, childName, nodeService);
        if(child == null) {
            child = createChildWithName(parentRef, assocQName, typeQName, childName, nodeService);
        }
        return child;
    }
    
    public static NodeRef getChildByQName(NodeRef parentRef, QName assocQName, QName childQName, NodeService nodeService) {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(parentRef, assocQName, childQName);
        return childAssocs == null || childAssocs.isEmpty() ? null : childAssocs.get(0).getChildRef();
    }
    
    public static NodeRef createChildWithQName(NodeRef parentRef, QName assocQName, QName typeQName, QName childQName, NodeService nodeService) {
        ChildAssociationRef childAssoc = nodeService.createNode(parentRef, 
                assocQName, 
                childQName, 
                typeQName);
        return childAssoc.getChildRef();
    }
    
    public static NodeRef getOrCreateChildByQName(NodeRef parentRef, QName assocQName, QName typeQName, QName childQName, NodeService nodeService) {
        NodeRef child = getChildByQName(parentRef, assocQName, childQName, nodeService);
        if(child == null) {
            child = createChildWithQName(parentRef, assocQName, typeQName, childQName, nodeService);
        }
        return child;
    }
    
	public static boolean hasParent(NodeRef nodeRef, NodeRef parent, QName assocType, Boolean primary, NodeService nodeService) {

		if (nodeRef == null)
			return false;

		if (nodeRef.equals(parent))
			return true;

		boolean result = false;
		List<ChildAssociationRef> assocs;
		if (assocType == null)
			assocs = nodeService.getParentAssocs(nodeRef);
		else
			assocs = nodeService.getParentAssocs(nodeRef, assocType, RegexQNamePattern.MATCH_ALL);
		for (ChildAssociationRef assoc : assocs) {
			if (primary != null) {
				if (primary.booleanValue() && !assoc.isPrimary())
					continue;
			}
			NodeRef pr = assoc.getParentRef();
			if (pr == null)
				break;
			if (pr.equals(parent)) {
				result = true;
				break;
			} else {
				result = hasParent(pr, parent, assocType, primary, nodeService);
			}
		}
		return result;
	}

	/**
	 * It returns list of node references, which are primary parents from
	 * nodeRef to parentRef. Output list does not contain parameters nodeRef and
	 * parentRef
	 * 
	 * @param nodeRef
	 *            - node reference
	 * @param parentRef
	 *            - parent node reference
	 * @param assocType
	 *            - association type, it can be null
	 * @param parents
	 *            - output list
	 * @param nodeService
	 * @throws AlfrescoRuntimeException
	 */
	public static void getPrimaryParents(NodeRef nodeRef, NodeRef parentRef, QName assocType, List<NodeRef> parents, NodeService nodeService)
			throws AlfrescoRuntimeException {

		if (!hasParent(nodeRef, parentRef, assocType, true, nodeService))
			throw new AlfrescoRuntimeException("Specified nodeRef and parentRef are not in the one primary hierarchy tree. nodeRef=" + nodeRef
					+ ", parentRef=" + parentRef);

		ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
		if (parentAssoc == null)
			return;

		NodeRef parent = parentAssoc.getParentRef();
		if (parent == null)
			return;

		if (parent.equals(parentRef))
			return;

		parents.add(parent);
		getPrimaryParents(parent, parentRef, assocType, parents, nodeService);
	}

	public static NodeRef getNestedFolder(NodeRef parentFolder, List<NodeRef> children, String searchedFolderName, NodeService nodeService,
			FileFolderService fileFolderService) {

		NodeRef result = null;
		NodeRef parent = parentFolder;
		for (NodeRef child : children) {
			String name = RepoUtils.getMandatoryProperty(child, ContentModel.PROP_NAME, nodeService);
			result = getSubFolder(parent, name, fileFolderService);
			if (result == null)
				break;
			else
				parent = result;
		}
		if (parent != null)
			result = getSubFolder(parent, searchedFolderName, fileFolderService);
		return result;
	}

	public static NodeRef createNestedFolder(NodeRef parentFolder, List<NodeRef> children, String searchedFolderName, NodeService nodeService,
			FileFolderService fileFolderService) {

		NodeRef result = null;
		NodeRef parent = parentFolder;
		for (NodeRef child : children) {
			String name = RepoUtils.getMandatoryProperty(child, ContentModel.PROP_NAME, nodeService);
			result = getSubFolder(parent, name, fileFolderService);
			if (result == null)
				parent = createSubFolder(parent, name, fileFolderService);
			else
				parent = result;
		}
		result = getSubFolder(parent, searchedFolderName, fileFolderService);
		if (result == null)
			result = createSubFolder(parent, searchedFolderName, fileFolderService);
		return result;
	}

	public static void createVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties, NodeService nodeService,
			VersionService versionService) {

		if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			Map<QName, Serializable> versioningProperties = new HashMap<QName, Serializable>();
			versioningProperties.put(ContentModel.PROP_AUTO_VERSION, true);
			versioningProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
			versioningProperties.put(ContentModel.PROP_INITIAL_VERSION, false);
			nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, versioningProperties);

			Map<String, Serializable> properties = new HashMap<String, Serializable>(versionProperties.size() + 1);
			properties.putAll(versionProperties);
			properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
			versionProperties = properties;
		}
		versionService.createVersion(nodeRef, versionProperties);
	}

	public static String getOriginalName(NodeRef nodeRef, NodeService nodeService, MimetypeService mimetypeService) {

		String result = (String) nodeService.getProperty(nodeRef, IdocsTemplateModel.PROP_GENERATED_NAME);
		if (result == null)
			result = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
		ContentData content = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
		if (content != null) {
			String mimeType = mimetypeService.guessMimetype(result);
			String extension = ('.' + mimetypeService.getExtension(mimeType)).toLowerCase();
			if (result.toLowerCase().endsWith(extension))
				result = result.substring(0, result.length() - extension.length());
		}
		return result;
	}

	public static String getUniqueName(NodeRef nodeRef, String nameWithoutExt, String extension, NodeService nodeService) {

		int index = 0;
		String newNameWithIndex = nameWithoutExt + extension;
		ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
		NodeRef nr = null;
		while ((nr = nodeService.getChildByName(parentAssoc.getParentRef(), parentAssoc.getTypeQName(), newNameWithIndex)) != null
				&& !nodeRef.equals(nr)) {
			index++;
			newNameWithIndex = nameWithoutExt + " (" + index + ")" + extension;
		}
		return newNameWithIndex;
	}

	public static String getExtension(NodeRef nodeRef, String defaultExtension, NodeService nodeService, MimetypeService mimetypeService) {

		// get content
		ContentData content = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
		if (content == null) {
			return defaultExtension;
		}
		// get mimetype
		String mimetype = content.getMimetype();
		if (mimetype == null) {
			return defaultExtension;
		}
		// get default extension
		String extension = mimetypeService.getExtension(mimetype);
		if (extension == null) {
			return defaultExtension;
		}
		return "." + extension;
	}

	public static void setUniqueOriginalName(NodeRef nodeRef, String defaultExtension, NodeService nodeService, MimetypeService mimetypeService) {

		String originalName = getOriginalName(nodeRef, nodeService, mimetypeService);
		String extension = getExtension(nodeRef, defaultExtension, nodeService, mimetypeService);
		String uniqueName = getUniqueName(nodeRef, originalName, extension, nodeService);
		nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, uniqueName);
	}

	/**
	 * 
	 * @param nodeRef
	 * @param type 
	 * @param nodeService
	 * @return list of children of nodeRef by specified type, returns empty list if node does not have children with specified type
	 * 
	 * @throws IllegalArgumentException
	 *             it throws this exception when required parameters are not
	 *             specified or specified node reference is not exist.
	 */
	public static List<NodeRef> getChildrenByType(NodeRef nodeRef, QName type, NodeService nodeService) {

		if (nodeRef == null || type == null || nodeService == null)
			throw new IllegalArgumentException("One of required parameters are not specified. nodeRef=" + nodeRef + "; type=" + type
					+ "; nodeService=" + nodeService);

		if (!nodeService.exists(nodeRef))
			throw new IllegalArgumentException("Specified node reference is not exist. nodeRef=" + nodeRef);

		List<NodeRef> result = new ArrayList<NodeRef>();

		Set<QName> childNodeTypeQNames = new HashSet<QName>(Arrays.asList(type));
		List<ChildAssociationRef> childs = nodeService.getChildAssocs(nodeRef, childNodeTypeQNames);

		for (ChildAssociationRef childRef : childs) {
			result.add(childRef.getChildRef());
		}

		return result;
	}

	/**
	 * 
	 * @param nodeRef
	 * @param assocTypeQName
	 *            - name of child association
	 * @param nodeService
	 * @return list of children of nodeRef, by specified assocTypeQName
	 * @throws IllegalArgumentException
	 *             it throws this exception when required parameters are not
	 *             specified or specified node reference is not exist.
	 */
	public static List<NodeRef> getChildrenByAssoc(NodeRef nodeRef, QName assocTypeQName, NodeService nodeService) {

		if (nodeRef == null || assocTypeQName == null || nodeService == null)
			throw new IllegalArgumentException("One of required parameters are not specified. nodeRef=" + nodeRef + "; assocTypeQName="
					+ assocTypeQName + "; nodeService=" + nodeService);

		if (!nodeService.exists(nodeRef))
			throw new IllegalArgumentException("Specified node reference is not exist. nodeRef=" + nodeRef);

		List<NodeRef> result = new ArrayList<NodeRef>();

		List<ChildAssociationRef> childs = nodeService.getChildAssocs(nodeRef, assocTypeQName, RegexQNamePattern.MATCH_ALL);
		
		for (ChildAssociationRef childRef : childs) {
			result.add(childRef.getChildRef());
		}

		return result;
	}

	/**
	 * 
	 * @param nodeRef
	 * @param assocTypeQName
	 *            - name target association
	 * @param nodeService
	 * @return list of target Node refs, for specified nodeRef and
	 *         assocTypeQName
	 * @throws IllegalArgumentException
	 *             it throws this exception when required parameters are not
	 *             specified or specified node reference is not exist.
	 */
	public static List<NodeRef> getTargetAssoc(NodeRef nodeRef, QName assocTypeQName, NodeService nodeService) {

		if (nodeRef == null || assocTypeQName == null || nodeService == null)
			throw new IllegalArgumentException("One of required parameters are not specified. nodeRef=" + nodeRef + "; assocTypeQName="
					+ assocTypeQName + "; nodeService=" + nodeService);

		if (!nodeService.exists(nodeRef))
			throw new IllegalArgumentException("Specified node reference is not exist. nodeRef=" + nodeRef);

		List<NodeRef> result = new ArrayList<NodeRef>();

		List<AssociationRef> childs = nodeService.getTargetAssocs(nodeRef, assocTypeQName);

		for (AssociationRef childRef : childs) {
			result.add(childRef.getTargetRef());
		}

		return result;
	}

	/**
	 * 
	 * @param nodeRef
	 * @param assocTypeQName
	 *            - name of target association
	 * @param nodeService
	 * @return first target association for specified nodeRef, return null if no
	 *         target association exists
	 * 
	 * @throws IllegalArgumentExceptionit
	 *             throws this exception when required parameters are not
	 *             specified or specified node reference is not exist.
	 */
	public static NodeRef getFirstTargetAssoc(NodeRef nodeRef, QName assocTypeQName, NodeService nodeService) {

		if (nodeRef == null || assocTypeQName == null || nodeService == null)
			throw new IllegalArgumentException("One of required parameters are not specified. nodeRef=" + nodeRef + "; assocTypeQName="
					+ assocTypeQName + "; nodeService=" + nodeService);

		if (!nodeService.exists(nodeRef))
			throw new IllegalArgumentException("Specified node reference is not exist. nodeRef=" + nodeRef);

		List<AssociationRef> childs = nodeService.getTargetAssocs(nodeRef, assocTypeQName);

		if (childs.isEmpty()) {
			return null;
		} else {
			return childs.get(0).getTargetRef();
		}

	}

	/**
	 * 
	 * @param nodeRef
	 * @param nodeService
	 * @return primary parent of nodeRef
	 * @throws IllegalArgumentExceptionit
	 *             throws this exception when required parameters are not
	 *             specified or specified node reference is not exist.
	 */
	public static NodeRef getPrimaryParentRef(NodeRef nodeRef, NodeService nodeService) {

		if (nodeRef == null || nodeService == null)
			throw new IllegalArgumentException("One of required parameters are not specified. nodeRef=" + nodeRef + "; nodeService=" + nodeService);

		if (!nodeService.exists(nodeRef))
			throw new IllegalArgumentException("Specified node reference is not exist. nodeRef=" + nodeRef);

		ChildAssociationRef childAssoc = nodeService.getPrimaryParent(nodeRef);

		return childAssoc.getParentRef();
	}

	/**
	 * Completely deletes node without moving to archive store
	 * 
	 * @param nodeRef
	 * @param nodeService
	 * @throws IllegalArgumentException
	 *             it throws this exception when required parameters are not
	 *             specified or specified node reference is not exist.
	 */
	public static void deleteNode(NodeRef nodeRef, NodeService nodeService) {

		if (nodeRef == null || nodeService == null)
			throw new IllegalArgumentException("One of required parameters are not specified. nodeRef=" + nodeRef + "; nodeService=" + nodeService);

		if (!nodeService.exists(nodeRef))
			throw new IllegalArgumentException("Specified node reference is not exist. nodeRef=" + nodeRef);

		if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
			nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
			nodeService.deleteNode(nodeRef);
		}

	}

	/**
	 * return
	 * 
	 * @param nodeRef
	 * @param assocTypeQName
	 *            - name of source association
	 * @param nodeService
	 * @return list of source association for node for passed assocType, returns
	 *         empty list of no source associations exists
	 * @throws IllegalArgumentException
	 *             it throws this exception when required parameters are not
	 *             specified or specified node reference is not exist.
	 */
	public static List<NodeRef> getSourceNodeRefs(NodeRef nodeRef, QName assocTypeQName, NodeService nodeService) {

		if (nodeRef == null || nodeService == null || assocTypeQName == null)
			throw new IllegalArgumentException("One of required parameters are not specified. nodeRef=" + nodeRef + "; assocTypeQName="
					+ assocTypeQName + "; nodeService=" + nodeService);

		if (!nodeService.exists(nodeRef))
			throw new IllegalArgumentException("Specified node reference is not exist. nodeRef=" + nodeRef);

		List<NodeRef> result = new ArrayList<NodeRef>();

		List<AssociationRef> childs = nodeService.getSourceAssocs(nodeRef, assocTypeQName);

		for (AssociationRef childRef : childs) {
			result.add(childRef.getSourceRef());
		}

		return result;
	}

	/**
	 * @param nodeRef
	 * @param assocTypeQName
	 *            - name of source association
	 * @param nodeService
	 * @return first nodeRef of source association by assocTypeQName, if passed
	 *         nodeRef does not have source association returns null;
	 * @throws IllegalArgumentException
	 *             it throws this exception when required parameters are not
	 *             specified or specified node reference is not exist.
	 */
	public static NodeRef getFirstSourceAssoc(NodeRef nodeRef, QName assocTypeQName, NodeService nodeService) {

		if (nodeRef == null || nodeService == null || assocTypeQName == null)
			throw new IllegalArgumentException("One of required parameters are not specified. nodeRef=" + nodeRef + "; assocTypeQName="
					+ assocTypeQName + "; nodeService=" + nodeService);

		if (!nodeService.exists(nodeRef))
			throw new IllegalArgumentException("Specified node reference is not exist. nodeRef=" + nodeRef);

		List<AssociationRef> childs = nodeService.getSourceAssocs(nodeRef, assocTypeQName);

		if (childs.isEmpty()) {
			return null;
		} else {
			return childs.get(0).getSourceRef();
		}
	}
	
	public static Map<String, Object> buildDefaultModel(NodeRef person, NodeRef companyHome, NodeRef userHome) {
	    Map<String, Object> model = new HashMap<>(3);
	    model.put("person", person);
	    model.put("companyhome", companyHome);
	    model.put("userhome", userHome);
	    return model;
	}
	
	public static Map<String, Object> buildDefaultModel(ServiceRegistry serviceRegistry) {
	    String currentUserName = serviceRegistry.getAuthenticationService().getCurrentUserName();
	    NodeRef person = serviceRegistry.getPersonService().getPerson(currentUserName);
	    NodeService nodeService = serviceRegistry.getNodeService();
	    NodeRef userHome = (NodeRef) nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
	    NodeRef rootHome = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
	    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(rootHome, 
	            ContentModel.ASSOC_CHILDREN, 
	            QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "company_home"));
	    if(childAssocs.size() != 1) {
	        throw new IllegalStateException("Could not find company home: " + childAssocs.size() + " results");
	    }
	    NodeRef companyHome = childAssocs.get(0).getChildRef();
	    return buildDefaultModel(person, companyHome, userHome);
	}
	
    public static List<NodeRef> getTargetNodeRefs(List<AssociationRef> targetAssocs) {
        if(targetAssocs == null) return Collections.emptyList();
        List<NodeRef> nodeRefs = new ArrayList<>(targetAssocs.size());
        for(AssociationRef assoc : targetAssocs) {
            nodeRefs.add(assoc.getTargetRef());
        }
        return nodeRefs;
    }
    
    public static List<NodeRef> getTargetNodeRefs(NodeRef nodeRef, QName assocType, NodeService nodeService) {
        return getTargetNodeRefs(nodeService.getTargetAssocs(nodeRef, assocType));
    }
    
    public static Map<QName, List<NodeRef>> getTargetAssocs(NodeRef nodeRef, NodeService nodeService) {
        // TODO add custom qname type filtering
        List<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
        Map<QName, List<NodeRef>> assocMap = new HashMap<>();
        for(AssociationRef assoc : assocs) {
            QName assocType = assoc.getTypeQName();
            List<NodeRef> nodeRefs = assocMap.get(assocType);
            if(nodeRefs == null) {
                nodeRefs = new LinkedList<>();
                assocMap.put(assocType, nodeRefs);
            }
            nodeRefs.add(assoc.getTargetRef());
        }
        return assocMap;
    }
    
    public static Map<QName, List<NodeRef>> getSourceAssocs(NodeRef nodeRef, NodeService nodeService) {
        List<AssociationRef> assocs = nodeService.getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
        Map<QName, List<NodeRef>> assocMap = new HashMap<>();
        for(AssociationRef assoc : assocs) {
            QName assocType = assoc.getTypeQName();
            List<NodeRef> nodeRefs = assocMap.get(assocType);
            if(nodeRefs == null) {
                nodeRefs = new LinkedList<>();
                assocMap.put(assocType, nodeRefs);
            }
            nodeRefs.add(assoc.getSourceRef());
        }
        return assocMap;
    }
    
    public static void setAssocs(NodeRef nodeRef, Map<QName, List<NodeRef>> assocs, boolean isTarget, boolean full, NodeService nodeService) {
        Map<QName, List<NodeRef>> existingAssocs = isTarget
                ? getTargetAssocs(nodeRef, nodeService)
                : getSourceAssocs(nodeRef, nodeService);

        // walk through assocs, that we should add
        for (Map.Entry<QName, List<NodeRef>> entry : assocs.entrySet()) {
            QName name = entry.getKey();
            Set<NodeRef> nodesToLink = new HashSet<NodeRef>(entry.getValue());
            List<NodeRef> linkedNodes = existingAssocs.containsKey(name) 
                    ? existingAssocs.get(name) 
                    : Collections.<NodeRef>emptyList();
            for (NodeRef nodeToLink : nodesToLink) {
                if (!linkedNodes.contains(nodeToLink)) {
                    createAssociation(nodeRef, nodeToLink, name, isTarget, nodeService);
                }
            }
            // delete existing assocs
            if (linkedNodes != null) {
                for (NodeRef linkedNode : linkedNodes) {
                    if (!nodesToLink.contains(linkedNode)) {
                        removeAssociation(nodeRef, linkedNode, name, isTarget,
                                nodeService);
                    }
                }
            }
        }

        // walk through assocs, that we should remove
        if (full) {
            for (Map.Entry<QName, List<NodeRef>> entry : existingAssocs.entrySet()) {
                QName name = entry.getKey();
                if (assocs.containsKey(name)) continue;
                List<NodeRef> nodesToUnlink = entry.getValue();
                for (NodeRef nodeToUnlink : nodesToUnlink) {
                    removeAssociation(nodeRef, nodeToUnlink, name, isTarget, nodeService);
                }
            }
        }
    }

    public static void createAssociation(NodeRef nodeRef, NodeRef nodeToLink,
            QName name, boolean isTarget, NodeService nodeService) {
        if (isTarget) {
            nodeService.createAssociation(nodeRef, nodeToLink, name);
        } else {
            nodeService.createAssociation(nodeToLink, nodeRef, name);
        }
    }

    public static void removeAssociation(NodeRef nodeRef, NodeRef linkedNode,
            QName name, boolean isTarget, NodeService nodeService) {
        if (isTarget) {
            nodeService.removeAssociation(nodeRef, linkedNode, name);
        } else {
            nodeService.removeAssociation(linkedNode, nodeRef, name);
        }
    }

    public static Map<QName, Object> convertStringMapToQNameMap(
            Map<?,?> stringMap, NamespacePrefixResolver prefixResolver) {
        Map<QName, Object> qnameMap = new HashMap<>(stringMap.size());
        for(Object key : stringMap.keySet()) {
            if(!(key instanceof String)) continue; // anything else ?
            qnameMap.put(QName.createQName((String) key, prefixResolver), stringMap.get(key));
        }
        return qnameMap;
    }

    public static List<NodeRef> anyToNodeRefs(Object value) {
        if(value == null) {
            return Collections.emptyList();
        } else if(value instanceof NodeRef) {
            return Collections.singletonList((NodeRef) value);
        } else if(value instanceof String) {
            String nodeRefStr = (String) value;
            if(!NodeRef.isNodeRef(nodeRefStr)) {
                throw new IllegalArgumentException("Not a valid nodeRef: " + nodeRefStr);
            }
            return Collections.singletonList(new NodeRef(nodeRefStr));
        } else if(value instanceof Collection) {
            Collection<?> items = (Collection<?>) value;
            List<NodeRef> nodeRefs = new ArrayList<>(items.size());
            for(Object item : items) {
                nodeRefs.addAll(anyToNodeRefs(item));
            }
            return nodeRefs;
        } else {
            throw new IllegalArgumentException("Expected collection, but got " + value.getClass());
        }
    }

    public static void setChildAssocs(NodeRef nodeRef, Map<QName, List<NodeRef>> childAssocs, boolean primary, boolean full, NodeService nodeService) {
        for(QName assocName : childAssocs.keySet()) {
            Set<NodeRef> newChildren = new HashSet<>(childAssocs.get(assocName));
            
            List<ChildAssociationRef> oldChildAssocs = nodeService.getChildAssocs(nodeRef, assocName, RegexQNamePattern.MATCH_ALL);
            Set<NodeRef> oldChildren = new HashSet<>(oldChildAssocs.size());
            for(ChildAssociationRef childAssoc : oldChildAssocs) {
                oldChildren.add(childAssoc.getChildRef());
            }
            
            // ensure correct type/aspect
            // TODO refactor with correct type/aspect check
            QName className = QName.createQName("{http://www.citeck.ru/model/content/dms/1.0}hasSupplementaryFiles");
            if(!nodeService.hasAspect(nodeRef, className)) {
                nodeService.addAspect(nodeRef, className, null);
            }
            
            // add new children:
            for(NodeRef child : newChildren) {
                if(!oldChildren.contains(child))
                    createChildAssociation(nodeRef, child, assocName, primary, nodeService);
            }
            
            // remove old children:
            for(ChildAssociationRef childAssoc : oldChildAssocs) {
                if(!newChildren.contains(childAssoc.getChildRef()))
                    nodeService.removeChildAssociation(childAssoc);
            }
        }
        
        if(full) {
            List<ChildAssociationRef> allChildAssocs = nodeService.getChildAssocs(nodeRef);
            for(ChildAssociationRef childAssoc : allChildAssocs) {
                if(!childAssocs.containsKey(childAssoc.getTypeQName()))
                    nodeService.removeChildAssociation(childAssoc);
            }
        }
    }

    private static void createChildAssociation(NodeRef nodeRef, NodeRef child,
            QName assocName, boolean primary, NodeService nodeService) {
        ChildAssociationRef primaryAssoc = nodeService.getPrimaryParent(nodeRef);
        if(!primary) {
            nodeService.addChild(nodeRef, child, assocName, primaryAssoc.getQName());
        } else if(!primaryAssoc.getParentRef().equals(nodeRef)) {
            nodeService.moveNode(child, nodeRef, assocName, primaryAssoc.getQName());
        }
    }

    public static boolean isSubType(NodeRef nodeRef, QName typeName,
            NodeService nodeService, DictionaryService dictionaryService) {
        QName nodeType = nodeService.getType(nodeRef);
        return dictionaryService.isSubClass(nodeType, typeName);
    }

    public static boolean isAssociated(NodeRef sourceRef, NodeRef targetRef, QName assocType, NodeService nodeService) {
        if(sourceRef == null || !nodeService.exists(sourceRef)) return false;
        if(targetRef == null || !nodeService.exists(targetRef)) return false;
        List<AssociationRef> assocs = nodeService.getTargetAssocs(sourceRef, assocType);
        for(AssociationRef assoc : assocs) {
            if(assoc.getTargetRef().equals(targetRef)) {
                return true;
            }
        }
        return false;
    }

    public static List<NodeRef> getChildNodeRefs(List<ChildAssociationRef> childAssocs) {
        if(childAssocs == null) return Collections.emptyList();
        List<NodeRef> nodeRefs = new ArrayList<>(childAssocs.size());
        for(ChildAssociationRef assoc : childAssocs) {
            nodeRefs.add(assoc.getChildRef());
        }
        return nodeRefs;
    }
    
    public static List<NodeRef> getParentNodeRefs(List<ChildAssociationRef> childAssocs) {
        if(childAssocs == null) return Collections.emptyList();
        List<NodeRef> nodeRefs = new ArrayList<>(childAssocs.size());
        for(ChildAssociationRef assoc : childAssocs) {
            nodeRefs.add(assoc.getParentRef());
        }
        return nodeRefs;
    }
    
    public static List<NodeRef> getChildrenByProperty(NodeRef nodeRef,
            QName propertyName, Serializable propertyValue, NodeService nodeService) {
        return getChildNodeRefs(nodeService.getChildAssocsByPropertyValue(nodeRef, propertyName, propertyValue));
    }

    public static Map<NodeRef, ChildAssociationRef> getChildAssociationMap(
            NodeRef nodeRef, QName assocName, NodeService nodeService) {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef, 
                assocName, RegexQNamePattern.MATCH_ALL);
        Map<NodeRef, ChildAssociationRef> childAssocMap = new HashMap<>(childAssocs.size());
        for(ChildAssociationRef assoc : childAssocs) {
            childAssocMap.put(assoc.getChildRef(), assoc);
        }
        return childAssocMap;
    }

	public static String getAuthorityName(NodeRef authorityRef, NodeService nodeService, DictionaryService dictionaryService) {
		String name = null;
		if(nodeService.exists(authorityRef)) {
			QName type = nodeService.getType(authorityRef);
			if(dictionaryService.isSubClass(type, ContentModel.TYPE_AUTHORITY_CONTAINER)) {
				name = (String)nodeService.getProperty(authorityRef, ContentModel.PROP_AUTHORITY_NAME);
			} else if(dictionaryService.isSubClass(type, ContentModel.TYPE_PERSON)) {
				name = (String)nodeService.getProperty(authorityRef, ContentModel.PROP_USERNAME);
			}
		}
		return name;
	}
}
