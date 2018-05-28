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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MapParentPropsAssocsBehaviour implements NodeServicePolicies.OnCreateNodePolicy
{
	private static Log logger = LogFactory.getLog(MapParentPropsAssocsBehaviour.class);
	
	private PolicyComponent policyComponent;
	private NodeService nodeService;
	private DictionaryService dictionaryService;
	private Map<QName, QName> mapping;
	private QName className;
	private Boolean enabled = null;

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		if(!Boolean.TRUE.equals(enabled)) {
			return;
		}
		NodeRef parent = childAssocRef.getParentRef();
		NodeRef node = childAssocRef.getChildRef();
		// check that nodes exist
		if(!nodeService.exists(parent) || !nodeService.exists(node)) {
			return;
		}
		
		Map<QName, Serializable> newProperties = new HashMap<QName, Serializable>(mapping.size());
		
		// process mapping
		for(QName parentAttrib : mapping.keySet()) {
			QName childAttrib = mapping.get(parentAttrib);
			
			// try to get parent property
			PropertyDefinition parentPropDef = dictionaryService.getProperty(parentAttrib);
			
			// if it is found, get value
			Serializable parentPropValue = null;
			if(parentPropDef != null) {
				parentPropValue = nodeService.getProperty(parent, parentAttrib);
			} 
			else {
				// TODO support associations for parent node
				throw new IllegalArgumentException("No such property found: " + parentAttrib);
			}
			
			// TODO support associations for child node
			PropertyDefinition childPropDef = null;
			if(parentAttrib.equals(childAttrib)) {
				childPropDef = parentPropDef;
			} else {
				childPropDef = dictionaryService.getProperty(childAttrib);
			}
			
			// if child property definition exists, set the value
			if(childPropDef != null) {
				newProperties.put(childAttrib, parentPropValue);
			}
			
		}
		
		if(newProperties.size() > 0) {
			nodeService.addProperties(node, newProperties);
		}
		
	}
	
	public void init() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, className, 
				new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.nodeService = serviceRegistry.getNodeService();
		this.dictionaryService = serviceRegistry.getDictionaryService();
	}

	public void setMapping(Map<QName, QName> mapping) {
		this.mapping = mapping;
	}

	public void setClassName(QName className) {
		this.className = className;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

}
