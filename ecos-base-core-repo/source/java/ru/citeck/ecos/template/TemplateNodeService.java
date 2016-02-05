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
package ru.citeck.ecos.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.cmis.client.impl.AlfrescoUtils;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.exporter.NodeContentData;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.jscript.ScriptNode.ScriptContentData;
import org.alfresco.repo.template.BaseContentNode.TemplateContentData;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameMap;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;

import ru.citeck.ecos.attr.NodeAttributeService;
import ru.citeck.ecos.service.AlfrescoServices;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.DictionaryUtils;

/**
 * Provides extended node service capabilities for templates.
 * 
 * @author Sergey Tiunov
 *
 */
public class TemplateNodeService extends BaseTemplateProcessorExtension 
{
	private ServiceRegistry serviceRegistry;
	private NodeService nodeService;
	private DictionaryService dictionaryService;
	private NamespaceService namespaceService;
    private PermissionService permissionService;
    private NodeAttributeService nodeAttributeService;
    private WorkflowService workflowService;
    private MessageService messageService;
	
	public Map<String, List<TemplateNode>> getParentAssocs(TemplateNode node) {
		@SuppressWarnings("unchecked")
		Map<String, List<TemplateNode>> result = new QNameMap<String, List<TemplateNode>>(node);
        
		List<ChildAssociationRef> assocs = nodeService.getParentAssocs(node.getNodeRef());
        for(ChildAssociationRef assoc : assocs) {
        	NodeRef parent = assoc.getParentRef();
        	QName assocType = assoc.getTypeQName();
        	String assocTypeStr = assocType.toString();
        	List<TemplateNode> nodes = result.get(assocTypeStr);
        	if(nodes == null) {
        		nodes = new ArrayList<TemplateNode>();
        		result.put(assocTypeStr, nodes);
        	}
        	nodes.add(new TemplateNode(parent, serviceRegistry, node.getImageResolver()));
        }
		return result;
	}
	
	public List<TemplateNode> getParentAssocs(TemplateNode node, String assocType) {
		QName assocTypeQName = QName.createQName(assocType, namespaceService);
		List<ChildAssociationRef> assocs = nodeService.getParentAssocs(node.getNodeRef(), assocTypeQName, RegexQNamePattern.MATCH_ALL);
		List<TemplateNode> result = new ArrayList<TemplateNode>(assocs.size());
		for(ChildAssociationRef assoc : assocs) {
			result.add(new TemplateNode(assoc.getParentRef(), serviceRegistry, node.getImageResolver()));
		}
		return result;
	}

    public Boolean isSubType(TemplateNode node, String typeName) {
        DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
        QName type = QName.createQName(typeName, namespaceService);
        return dictionaryService.isSubClass(node.getType(), type);
    }

    public String getClassTitle(String className) {
        QName classQName = QName.resolveToQName(namespaceService, className);
        ClassDefinition classDef = dictionaryService.getClass(classQName);
        if(classDef == null) {
            return null;
        }
        return classDef.getTitle();
    }

    public String getPropertyTitle(String propertyName) {
        QName propertyQName = QName.resolveToQName(namespaceService, propertyName);
        PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
        if(propDef == null) {
            return null;
        }
        return propDef.getTitle();
    }

    public String getPropertyLabel(String propertyName, String propertyValue) {
        QName propertyQName = QName.resolveToQName(namespaceService, propertyName);
        PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
        if(propDef == null) {
            return null;
        }
        for(ConstraintDefinition constraintDef : propDef.getConstraints()) {
            if(!(constraintDef.getConstraint() instanceof ListOfValuesConstraint))
                continue;
            ListOfValuesConstraint constraint = (ListOfValuesConstraint) constraintDef.getConstraint();
            List<String> allowedValues = constraint.getAllowedValues();
            if(!allowedValues.contains(propertyValue))
                continue;
            return constraint.getDisplayLabel(propertyValue, messageService);
        }
        return null;
    }
    
    public List<QName> getNodeTypes(TemplateNode node) {
        return DictionaryUtils.getAllNodeTypeNames(node.getNodeRef(), nodeService, dictionaryService);
    }
    
    public List<QName> getNodeClasses(TemplateNode node) {
        return DictionaryUtils.getAllNodeClassNames(node.getNodeRef(), nodeService, dictionaryService);
    }
    
    public Collection<QName> getParentClasses(String className) {
        QName classQName = QName.resolveToQName(namespaceService, className);
        return DictionaryUtils.getParentClassNames(classQName, dictionaryService);
    }
    
    public Collection<QName> getChildClasses(String className, boolean recursive) {
        QName classQName = QName.resolveToQName(namespaceService, className);
        return DictionaryUtils.getChildClassNames(classQName, recursive, dictionaryService);
    }
    
    public Set<String> getAllowedPermissions(TemplateNode node) {
        NodeRef nodeRef = node.getNodeRef();
        Set<String> permissions = permissionService.getSettablePermissions(nodeRef);
        Set<String> allowedPermissions = new HashSet<>(permissions.size());
        for(String permission : permissions) {
            if(permissionService.hasPermission(nodeRef, permission) == AccessStatus.ALLOWED) {
                allowedPermissions.add(permission);
            }
        }
        return allowedPermissions;
    }
    
    public Map<String, Object> getAttributes(TemplateNode node) {
        return convertQNameToString(nodeAttributeService.getAttributes(node.getNodeRef()));
    }

    public boolean isContentProperty(Object value) {
        return value instanceof ScriptContentData
            || value instanceof TemplateContentData
            || value instanceof ContentData;
    }
    
    public String getContentUrl(Object value) {
        String urlRegexp = "^/d/d/(.+)/(.+)/(.+)/.+$";
        String urlReplace = "$1://$2/$3";
        if(value instanceof ScriptContentData) {
            return ((ScriptContentData)value).getUrl().replaceFirst(urlRegexp, urlReplace);
        } else if(value instanceof TemplateContentData) {
            return ((TemplateContentData)value).getUrl().replaceFirst(urlRegexp, urlReplace);
        } else if(value instanceof NodeContentData) {
            return ((NodeContentData)value).getNodeRef().toString();
        } else if(value instanceof ContentData) {
            return ((ContentData)value).getContentUrl();
        } else {
            return null;
        }
    }
    
    private Map<String, Object> convertQNameToString(Map<QName, Object> attributes) {
        Map<String, Object> converted = new HashMap<>(attributes.size());
        for(Map.Entry<QName, Object> entry : attributes.entrySet()) {
            converted.put(entry.getKey().toPrefixString(namespaceService), entry.getValue());
        }
        return converted;
    }
    
    public String getWofrkflowName(String workflowID) {
		WorkflowInstance wf = workflowService.getWorkflowById(workflowID);
		if(wf!=null)
		{
			WorkflowDefinition definition = wf.getDefinition();
			if(definition!=null)
			{
				return definition.getTitle();
			}
		}
		return null;
	}

	public String getAssocTitle(String assocName) {
        QName assocQName = QName.resolveToQName(namespaceService, assocName);
		AssociationDefinition assoc = dictionaryService.getAssociation(assocQName);
		if(assoc!=null)
		{
			return assoc.getTitle();
		}
		return null;
	}
    
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
		this.nodeService = serviceRegistry.getNodeService();
		this.dictionaryService = serviceRegistry.getDictionaryService();
		this.namespaceService = serviceRegistry.getNamespaceService();
		this.permissionService = serviceRegistry.getPermissionService();
		this.nodeAttributeService = (NodeAttributeService) serviceRegistry.getService(CiteckServices.NODE_ATTRIBUTE_SERVICE);
		this.workflowService = serviceRegistry.getWorkflowService();
        this.messageService = (MessageService) serviceRegistry.getService(AlfrescoServices.MESSAGE_SERVICE);
	}
	
}
