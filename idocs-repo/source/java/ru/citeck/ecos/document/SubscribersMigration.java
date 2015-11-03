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
import java.util.Map;
import java.util.HashSet;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import ru.citeck.ecos.utils.LazyQName;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import ru.citeck.ecos.security.NodeOwnerDAO;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.repository.AssociationExistsException;

public class SubscribersMigration extends BaseScopableProcessorExtension {
	
	private Map<String,String> assocDocSubsTypeName;
	private NodeService nodeService;
	private PersonService personService;
	private NamespaceService namespaceService;
	private NodeOwnerDAO nodeOwnerDAO;
	protected boolean includeOwnerToSubs;
	protected String assocTypeName;
	private static Log logger = LogFactory.getLog(SubscribersMigration.class);
	private String aspectTypeName;

	public void migrate(NodeRef nodeRef) 
	{
		if(!nodeService.exists(nodeRef)) {
			throw new IllegalArgumentException("Illegal nodeRef: " + nodeRef);
		}
		
		List<NodeRef> subscribersNodes = getSubscribersNode(nodeRef);
		setSubscribersToAssoc(nodeRef, subscribersNodes);
	}
	
	public void migrate(ScriptNode node) {
		this.migrate(node.getNodeRef());
	}

	//people node which should be subscribed on notification 
	public void setAssocDocSubsTypeName(Map<String,String> assocDocSubsTypeName) {
		this.assocDocSubsTypeName = assocDocSubsTypeName;
	}
	
	/**
	* Include owner of document to subscribers
	* @param true or false
	*/
	public void setIncludeOwnerToSubs(Boolean includeOwnerToSubs) {
    	this.includeOwnerToSubs = includeOwnerToSubs.booleanValue();
    }
	//subscriber association name
	public void setAssocTypeName(String assocTypeName) {
		this.assocTypeName = assocTypeName;
	}
	//subscriber aspect name
	public void setAspectTypeName(String aspectTypeName) {
		this.aspectTypeName = aspectTypeName;
	}

	//get corrector and owner nodes for document
	public List<NodeRef> getSubscribersNode(NodeRef document) {
		WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(namespaceService);
		String assocQName = assocDocSubsTypeName.get(qNameConverter.mapQNameToName(nodeService.getType(document)));
		List<AssociationRef> assocs = nodeService.getTargetAssocs(document, qNameConverter.mapNameToQName(assocQName));
		List<NodeRef> nodes = new ArrayList<NodeRef>(assocs.size());
		for(AssociationRef assoc : assocs) {
			NodeRef file = assoc.getTargetRef();
			nodes.add(file);
		}
		if(includeOwnerToSubs)
		{
			String owner = nodeOwnerDAO.getOwner(document);
			NodeRef ownerRef = personService.getPerson(owner, false);
			nodes.add(ownerRef);
		}
		return nodes;
	}
	
	//get existing subscriber node for association type
	public List<NodeRef> getExistingSubscribers(NodeRef document) {
		WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(namespaceService);
		List<AssociationRef> assocs = nodeService.getTargetAssocs(document, qNameConverter.mapNameToQName(assocTypeName));
		List<NodeRef> nodeRefs = new ArrayList<NodeRef>(assocs.size());
		for(AssociationRef assoc : assocs) {
			NodeRef file = assoc.getTargetRef();
			nodeRefs.add(file);
		}
		return nodeRefs;
	}
	
	public void setSubscribersToAssoc(NodeRef document, List<NodeRef> subscribersNodes) {
		
		HashSet<NodeRef> existingSubscribers = new HashSet<NodeRef>();
		HashSet<NodeRef> allSubscribers = new HashSet<NodeRef>();
		existingSubscribers.addAll(getExistingSubscribers(document));
		allSubscribers.addAll(subscribersNodes);
		
		// get lists of subscribers to add:
		List<NodeRef> subsToAdd = new ArrayList<NodeRef>(allSubscribers.size());
		for(NodeRef sub : allSubscribers) {
			if(!existingSubscribers.contains(sub)) {
				subsToAdd.add(sub);
			}
		}
		// actually add new subscriber associations:
		addSubscribers(document, subsToAdd);
	}
	
	public void addSubscribers(final NodeRef document, final List<NodeRef> subscribers) {
		final LazyQName assocTypeQName = new LazyQName(namespaceService, assocTypeName);
		final LazyQName aspectTypeQName = new LazyQName(namespaceService, aspectTypeName);
		if(subscribers.size() > 0)
		{
			AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>()
			{
				public Object doWork() throws Exception
				{
					for(final NodeRef ref : subscribers)
					{
						try
						{
							nodeService.addAspect(document, aspectTypeQName.getQName(), Collections.<QName, Serializable> emptyMap());
							nodeService.createAssociation(document, ref, assocTypeQName.getQName());
						}
						catch (AssociationExistsException e)
						{
							logger.error(e.getStackTrace());
						}
						
					}
					return null;
				}
			});
		}
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	public void setNodeOwnerDAO(NodeOwnerDAO nodeOwnerDAO) {
		this.nodeOwnerDAO = nodeOwnerDAO;
	}

}
