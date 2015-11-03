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
package ru.citeck.ecos.behavior.common;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

import org.alfresco.model.ContentModel;
import ru.citeck.ecos.model.DmsModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.CyclicChildRelationshipException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.repo.policy.OrderedBehaviour;

public class DocumentCreateBasedOnFolderBehaviour implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnCreateChildAssociationPolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {
	// common properties
	protected PolicyComponent policyComponent;
	protected NodeService nodeService;
	protected ServiceRegistry services;
	protected FileFolderService fileFolderService;

	// distinct properties
	protected QName className;
	protected QName folderQName;
	protected QName nameDetermineProp;
	protected boolean createCpecialFolder;
	protected String createCpecialFolderPath;
	protected String supAgreementParentFolderPath;
	protected Map <QName, String> createCpecialFolderConditions;
	
	private static final Log logger = LogFactory.getLog(DocumentCreateBasedOnFolderBehaviour.class);
	private int order = 65;
	
	public void init() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, className, 
			new OrderedBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT, order));
			
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, className,
				new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.TRANSACTION_COMMIT));
				
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, className, 
			new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
	}
	
	@Override
	public void onCreateNode(final ChildAssociationRef childAssocRef) 
	{
		logger.debug("onCreateNode event");
		NodeRef nodeRef = childAssocRef.getChildRef();
		if(nodeService.exists(nodeRef))
		{
			String oldName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef);
			NodeRef initialParentFolder = null;
			NodeRef defaultParentFolder = null;
			NodeRef cpecialParentFolderNodeRef = null;
			boolean evaluateConditions = true;
			if(createCpecialFolder)
			{
				if(createCpecialFolderConditions!=null)
				{
					for(Map.Entry<QName, String> entry : createCpecialFolderConditions.entrySet())
					{
						QName property = (QName) entry.getKey();
						String value = (String) entry.getValue();
						String actualValue = (String) nodeService.getProperty(nodeRef,property);
						if(actualValue!=null && !actualValue.equals(value) || actualValue==null && value!=null)
						{
							evaluateConditions = false;
						}
					}
				}
				else
				{
					evaluateConditions = false;
				}
			}
			else
			{
				evaluateConditions = false;
			}
			if(evaluateConditions)
			{
				SearchParameters sp = new SearchParameters();
				StringBuffer sb = new StringBuffer();
				sb.append("PATH:\"").append(createCpecialFolderPath).append("\"");
				sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
				sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
				sp.setQuery(sb.toString());
				ResultSet results = null;
				try {
					results = services.getSearchService().query(sp);
					List<NodeRef> folders = results.getNodeRefs();
					if (folders != null && !folders.isEmpty()) {
						cpecialParentFolderNodeRef = folders.get(0);
					}
				} finally {
					if (results != null) {
						results.close();
					}
				}
			}
			
			for(ChildAssociationRef parent : parents)
			{
				if(nodeService.exists(parent.getParentRef()) && ContentModel.TYPE_FOLDER.equals(nodeService.getType(parent.getParentRef())))
				{
					initialParentFolder = parent.getParentRef();
					defaultParentFolder = parent.getParentRef();
				}
			}
			if(initialParentFolder==null)
			{
				if(parents.get(0)!=null)
					initialParentFolder = parents.get(0).getParentRef();
				SearchParameters sp = new SearchParameters();
				StringBuffer sb = new StringBuffer();
				sb.append("PATH:\"").append(supAgreementParentFolderPath).append("\"");
				sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
				sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
				sp.setQuery(sb.toString());
				ResultSet results = null;
				try {
					results = services.getSearchService().query(sp);
					List<NodeRef> folders = results.getNodeRefs();
					if (folders != null && !folders.isEmpty()) {
						defaultParentFolder = folders.get(0);
					}
				} finally {
					if (results != null) {
						results.close();
					}
				}
			}

			if(initialParentFolder!=null && defaultParentFolder!=null && nodeService.exists(initialParentFolder) && nodeService.getProperty(nodeRef,ContentModel.PROP_NAME)!=null && oldName!=null  && folderQName!=null)
			{
				try
				{
					FileInfo newParentFolder = fileFolderService.create(defaultParentFolder, oldName.replace("/","_")+"_", folderQName);
					NodeRef parentNodeRef = childAssocRef.getParentRef();
					NodeRef parentFolderNodeRef = newParentFolder.getNodeRef();
					FileInfo newFile = fileFolderService.move(nodeRef, parentFolderNodeRef, nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString());
					QName childQName = QName.createQName(ContentModel.ASSOC_CONTAINS.getNamespaceURI(), oldName+"_1_");
					nodeService.setProperty(parentFolderNodeRef, ContentModel.PROP_NAME, oldName.replace("/","_"));
					if(cpecialParentFolderNodeRef!=null)
					{
						FileInfo movedFile = fileFolderService.moveFrom(parentFolderNodeRef, initialParentFolder, cpecialParentFolderNodeRef, nodeService.getProperty(parentFolderNodeRef, ContentModel.PROP_NAME).toString());
						logger.debug("movedFile.getNodeRef() "+movedFile.getNodeRef());
					}
					logger.debug("folder created with name "+parentFolderNodeRef);
				}
				catch(FileNotFoundException e)
				{
					logger.error("File not found");
				}
				catch(FileExistsException e)
				{
					logger.error("File already exist");
				}
			}
		}
	}
	
	@Override
	public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean isNew) {
		logger.debug("onCreateChildAssociation event");
		NodeRef nodeParentSource = null;
		NodeRef nodeTarget = childAssociationRef.getChildRef();
		NodeRef nodeSource = childAssociationRef.getParentRef();
		if(DmsModel.ASSOC_SUPPLEMENARY_FILES.equals(childAssociationRef.getTypeQName()))
		{
			for(ChildAssociationRef parent : nodeService.getParentAssocs(nodeSource))
			{
				if(nodeService.exists(parent.getParentRef()) && folderQName!=null && folderQName.equals(nodeService.getType(parent.getParentRef())))
				{
					nodeParentSource = parent.getParentRef();
					break;
				}
			}
			if(nodeParentSource!=null && nodeService.exists(nodeParentSource) && nodeTarget!=null && nodeService.exists(nodeTarget))
			{
				try
				{
					nodeService.addChild(nodeParentSource, nodeTarget, ContentModel.ASSOC_CONTAINS, nodeService.getPrimaryParent(nodeTarget).getQName());
					logger.debug("added new child to node "+nodeParentSource);
				}
				catch(DuplicateChildNodeNameException e)
				{
					logger.error("DuplicateChildNodeNameException: Duplicate child name not allowed");
				}
				catch(CyclicChildRelationshipException e)
				{
					logger.error("CyclicChildRelationshipException: Node has been pasted into its own tree");
				}
				catch(AssociationExistsException e)
				{
					logger.error("AssociationExistsException: Association Already Exists");
				}
			}
		}
	}
	
	@Override
	public void onUpdateProperties(final NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) 
	{
		logger.debug("onUpdateProperties event");
		if(nodeService.exists(nodeRef)) 
		{
			if(nameDetermineProp!=null)
			{
				Object propBefore = (Object) before.get(nameDetermineProp);
				Object propAfter = (Object) after.get(nameDetermineProp);
				NodeRef currentParentFolder = null;
				if((propBefore!=null && !propBefore.equals(propAfter)) || (propBefore==null && propAfter!=null))
				{
					for(ChildAssociationRef parent : nodeService.getParentAssocs(nodeRef))
					{
						if(nodeService.exists(parent.getParentRef()) && folderQName!=null && folderQName.equals(nodeService.getType(parent.getParentRef())))
						{
							currentParentFolder = parent.getParentRef();
							break;
						}
					}
					if(currentParentFolder!=null && nodeService.exists(currentParentFolder))
					{
						nodeService.setProperty(currentParentFolder, ContentModel.PROP_NAME, propAfter.toString());
					}
				}
			}
		}
	}
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.services = serviceRegistry;
		this.fileFolderService = services.getFileFolderService();
	}
	
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setClassName(QName className) {
		this.className = className;
	}

	public void setFolderQName(QName folderQName) {
		this.folderQName = folderQName;
	}

	public void setNameDetermineProp(QName nameDetermineProp) {
		this.nameDetermineProp = nameDetermineProp;
	}
	
	public void setCreateCpecialFolder(boolean createCpecialFolder) {
		this.createCpecialFolder = createCpecialFolder;
	}
	
	public void setCreateCpecialFolderPath(String createCpecialFolderPath) {
		this.createCpecialFolderPath = createCpecialFolderPath;
	}
	
	public void setSupAgreementParentFolderPath(String supAgreementParentFolderPath) {
		this.supAgreementParentFolderPath = supAgreementParentFolderPath;
	}
	
	public void setCreateCpecialFolderConditions(Map <QName, String> createCpecialFolderConditions) {
		this.createCpecialFolderConditions = createCpecialFolderConditions;
	}
	
}
