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
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteAssociationPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * This behaviour caches target associations.
 * For example, we have A -a-> B -b-> C
 * (object A is associated with B, object B is associated with C).
 * After properly configured, this behaviour does this: A -b-> C
 * 
 * It can help to display such "remote" associations in form.
 * 
 * All associations to be cached should be defined in separate aspects, 
 * that are applied both to source object (B) and to cache object (A).
 * 
 * @author Sergey Tiunov
 *
 */
public class CacheAssocsBehaviour implements
	OnCreateAssociationPolicy, 
	OnDeleteAssociationPolicy
{

	private NodeService nodeService;
	private PolicyComponent policyComponent;
	
	// class name to bind this behaviour
	private QName bindClassName;
	
	// association type to sources of associations
	private QName sourceAssocName;
	
	// map assocName -> aspectName to be synchronized
	private Map<QName,QName> assocsToSync;

	// marks of old and new elements
	private static final int MARK_OLD = 1;
	private static final int MARK_NEW = 2;

	public void init() {
		// bind only for specified associations
		this.policyComponent.bindAssociationBehaviour(OnCreateAssociationPolicy.QNAME, 
				bindClassName, sourceAssocName, 
				new JavaBehaviour(this, "onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT));
		this.policyComponent.bindAssociationBehaviour(OnDeleteAssociationPolicy.QNAME, 
				bindClassName, sourceAssocName, 
				new JavaBehaviour(this, "onDeleteAssociation", NotificationFrequency.TRANSACTION_COMMIT));
	}
	
	@Override
	public void onCreateAssociation(AssociationRef nodeAssocRef) {
		if(nodeAssocRef.getTypeQName().equals(sourceAssocName)) {
			syncAssocs(nodeAssocRef.getSourceRef());
		}
	}

	@Override
	public void onDeleteAssociation(AssociationRef nodeAssocRef) {
		if(nodeAssocRef.getTypeQName().equals(sourceAssocName)) {
			syncAssocs(nodeAssocRef.getSourceRef());
		}
	}

	// synchronize associations
	private void syncAssocs(NodeRef cacheObject) {
		if(!nodeService.exists(cacheObject)) {
			return;
		}
		for(Map.Entry<QName,QName> assocConfig : assocsToSync.entrySet()) {
			QName assocToSync = assocConfig.getKey();
			QName aspectToSync = assocConfig.getValue();

			// this aspect should be already set on document
			// e.g. in mandatory-aspects of type
			if(!nodeService.hasAspect(cacheObject, aspectToSync)) {
				continue;
			}
			
			// mark elements, already existing in cache with MARK_OLD
			List<AssociationRef> existingAssocs = nodeService.getTargetAssocs(cacheObject, assocToSync);
			Map<NodeRef,Integer> existanceMap = new HashMap<NodeRef,Integer>(existingAssocs.size());
			markExisting(existanceMap, existingAssocs, MARK_OLD);
			
			// mark elements, that should be cached with 2
			List<AssociationRef> sourceAssocs = nodeService.getTargetAssocs(cacheObject, sourceAssocName);
			for(AssociationRef sourceAssoc : sourceAssocs) {
				NodeRef elementSource = sourceAssoc.getTargetRef();
				List<AssociationRef> assocsToCache = nodeService.getTargetAssocs(elementSource, assocToSync);
				markExisting(existanceMap, assocsToCache, MARK_NEW);
			}
			
			// now old elements are marked with MARK_OLD
			// new elements are marked with MARK_NEW
			// and elements that should remain with MARK_OLD|MARK_NEW

			// so we remove old ones and add new ones
			for(NodeRef element : existanceMap.keySet()) {
				
				switch(existanceMap.get(element)) {
				case MARK_OLD:
					nodeService.removeAssociation(cacheObject, element, assocToSync);
					break;
				case MARK_NEW:
					nodeService.createAssociation(cacheObject, element, assocToSync);
					break;
				}
			}
		}
	}

	// mark associated elements with mark in the map
	private void markExisting(Map<NodeRef,Integer> map, List<AssociationRef> assocs, int mark) 
	{
		for(AssociationRef assoc : assocs) {
			NodeRef nodeRef = assoc.getTargetRef();
			Integer current = map.get(nodeRef);
			if(current == null) {
				current = 0;
			}
			map.put(nodeRef, current | mark);
		}
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setBindClassName(QName className) {
		this.bindClassName = className;
	}

	public void setSourceAssocName(QName assocName) {
		this.sourceAssocName = assocName;
	}

	public void setAssocsToSync(Map<QName,QName> assocsToSync) {
		this.assocsToSync = assocsToSync;
	}

}
