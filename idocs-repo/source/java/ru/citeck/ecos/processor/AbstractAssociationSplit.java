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
package ru.citeck.ecos.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Abstract Association Split Base is a Data Bundle Split, that splits Data Bundle, based on some node's associations.
 * The type of association is specified by subclasses.
 * 
 * @author Sergey Tiunov
 *
 */
public abstract class AbstractAssociationSplit extends AbstractDataBundleSplit
{
	protected NodeService nodeService;
	
	private String nodeRefExpr;
	private String assocRefKey;

	public void init() {
		this.nodeService = serviceRegistry.getNodeService();
	}
	
	@Override
	public final List<DataBundle> split(DataBundle input) {
		
		Map<String,Object> model = input.needModel();
		
		NodeRef nodeRef = helper.needExistingNodeRef(this.evaluateExpression(nodeRefExpr, model));
		
		List<Object> assocs = this.getAssociatedObjects(nodeRef);
		
		List<DataBundle> outputs = new ArrayList<DataBundle>(assocs.size());
		for(Object assoc : assocs) {
			Map<String,Object> outputModel = new HashMap<String,Object>(model.size() + 1);
			outputModel.putAll(model);
			outputModel.put(assocRefKey, assoc);
			outputs.add(new DataBundle(outputModel));
		}

		return outputs;
	}
	
	protected abstract List<Object> getAssociatedObjects(NodeRef nodeRef);

	/**
	 * Set the expression, that should be evaluated to nodeRef.
	 * 
	 * @param nodeRef
	 */
	public void setNodeRef(String nodeRef) {
		this.nodeRefExpr = nodeRef;
	}

	/**
	 * Set the key, that should be used to store associated objects in split Data Bundle models.
	 * @param assocRefKey
	 */
	public void setAssocRefKey(String assocRefKey) {
		this.assocRefKey = assocRefKey;
	}

}
