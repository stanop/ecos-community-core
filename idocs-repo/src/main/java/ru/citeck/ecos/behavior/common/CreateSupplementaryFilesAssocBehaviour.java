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
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.RegexQNamePattern;
import ru.citeck.ecos.document.SupplementaryFilesDAO;
import org.alfresco.service.cmr.repository.CyclicChildRelationshipException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.AssociationRef;

public class CreateSupplementaryFilesAssocBehaviour implements NodeServicePolicies.OnCreateChildAssociationPolicy {
	// common properties
	protected PolicyComponent policyComponent;
	protected NodeService nodeService;
	protected ServiceRegistry services;
	protected DictionaryService dictionaryService;
	private TemplateService templateService;
	private String nodeVariable;
	private String templateEngine;
	private SupplementaryFilesDAO supplFilesDAO;

	// distinct properties
	protected QName className;
	protected List<QName> allowedDocTypes;
    private static final Log logger = LogFactory.getLog(CreateSupplementaryFilesAssocBehaviour.class);
	protected List <QName> ignoredTypes;

	public void init() {
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, className,
				new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.TRANSACTION_COMMIT)
		);
	}
	

	@Override
	public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean isNew) {
		logger.debug("onCreateChildAssociation event");
		NodeRef nodeNewSource = null;
		List<NodeRef> suppFiles = new ArrayList<NodeRef>();
		NodeRef nodeTarget = childAssociationRef.getChildRef(); //supp file
		if(nodeService.exists(nodeTarget) && (ignoredTypes==null || !ignoredTypes.contains(nodeService.getType(nodeTarget))))
		{
			suppFiles.add(nodeTarget);
		}
		NodeRef nodeSource = childAssociationRef.getParentRef(); //folder
		if(nodeService.exists(nodeSource) && ContentModel.ASSOC_CONTAINS.equals(childAssociationRef.getTypeQName()))
		{
			for(ChildAssociationRef child : nodeService.getChildAssocs(nodeSource,ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL))
			{
				NodeRef docNode = child.getChildRef();
				if(nodeService.exists(docNode) && allowedDocTypes!=null && allowedDocTypes.size()>0 && allowedDocTypes.contains(nodeService.getType(docNode)) && !nodeTarget.equals(docNode))
				{
					String nameTemplate = null;
					QName folderType = nodeService.getType(nodeSource);
					if(folderType!=null && className!=null && folderType.equals(className))
					{
						try
						{
							List<ChildAssociationRef> existingAssocs = nodeService.getChildAssocs(docNode, DmsModel.ASSOC_SUPPLEMENARY_FILES, RegexQNamePattern.MATCH_ALL);
							for (ChildAssociationRef assoc : existingAssocs)
							{
								if (assoc.getChildRef().equals(nodeTarget))
								{
									logger.debug("Attempt to add existing association prevented. " + assoc);
									return;
								}
							}
							supplFilesDAO.addSupplementaryFiles(docNode,suppFiles);
							logger.debug("assoc added between document "+docNode+" and files "+suppFiles);
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
		}
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.services = serviceRegistry;
		this.dictionaryService = services.getDictionaryService();
	}
	
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}

	public void setClassName(QName className) {
		this.className = className;
	}

	public void setTemplateEngine(String templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void setNodeVariable(String nodeVariable) {
		this.nodeVariable = nodeVariable;
	}

	public void setAllowedDocTypes(List<QName> allowedDocTypes) {
		this.allowedDocTypes = allowedDocTypes;
	}

	public void setSupplFilesDAO(SupplementaryFilesDAO supplFilesDAO) {
		this.supplFilesDAO = supplFilesDAO;
	}
	
	public void setIgnoredTypes(List <QName> ignoredTypes) {
		this.ignoredTypes = ignoredTypes;
	}

}
