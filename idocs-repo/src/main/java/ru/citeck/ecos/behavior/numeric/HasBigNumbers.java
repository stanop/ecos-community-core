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
package ru.citeck.ecos.behavior.numeric;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.model.CiteckNumericModel;
import ru.citeck.ecos.utils.BigNumberEncoder;

/**
 * HasBigNumbers aspect behaviour.
 * As big numbers are stored as simple strings, 
 * we need special field to support search.
 * This behaviour finds big-number fields based on assumption, 
 * that their names are ended with _BIG, 
 * and sets index field with suffix _INDEX to support search.
 * Therefore, search on big numbers should be done on _INDEX fields, 
 * and for _BIG fields indexing can be disabled.
 * 
 * @author Sergey Tiunov
 *
 */
public class HasBigNumbers
        implements
        NodeServicePolicies.OnUpdatePropertiesPolicy {

	private NodeService nodeService;
    private PolicyComponent policyComponent;

    public void init() {
    	// EVERY_EVENT - ensures that index properties will be updated as soon as possible
        this.policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME,
                CiteckNumericModel.ASPECT_HAS_BIG_NUMBERS, new JavaBehaviour(this,
                "onUpdateProperties", NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) 
    {
    	refreshBigNumbersIndex(nodeRef, before, after);
    }

	// refresh index fields for properties with big number strings
    private void refreshBigNumbersIndex(NodeRef nodeRef,
			Map<QName, Serializable> before, Map<QName, Serializable> after) 
    {
    	if(!nodeService.exists(nodeRef)) {
    		return;
    	}
    	
    	Set<QName> props = new HashSet<QName>();
    	props.addAll(before.keySet());
    	props.addAll(after.keySet());
    	
    	for(QName prop : props) {
        	// for all big-number properties, based on name suffix
    		if(CiteckNumericModel.isBigProp(prop)) {
    			// get index name - _BIG replaced with _INDEX
    			QName indexProp = CiteckNumericModel.getIndexProp(prop);
    			// refresh index property
    			refreshBigNumbersIndex(nodeRef, before, after, prop, indexProp);
    		}
    	}
    	
	}

    // refresh index field for one big-number property
    // uses BigNumberEncoder to handle index-compatible to-string-conversion
    private void refreshBigNumbersIndex(NodeRef nodeRef,
			Map<QName, Serializable> before, Map<QName, Serializable> after,
			QName valueProperty, QName indexProperty) 
    {
    	String valueBefore = (String) before.get(valueProperty);
		String valueAfter = (String) after.get(valueProperty);
		if(valueBefore == null && valueAfter == null) {
			return;
		}
		if(valueAfter == null) {
			nodeService.setProperty(nodeRef, indexProperty, null);
		} else if(valueBefore == null || !valueBefore.equals(valueAfter)) {
			String valueForIndex = BigNumberEncoder.encode(new BigDecimal(valueAfter));
			nodeService.setProperty(nodeRef, indexProperty, valueForIndex);
		}
	}
    
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

}
