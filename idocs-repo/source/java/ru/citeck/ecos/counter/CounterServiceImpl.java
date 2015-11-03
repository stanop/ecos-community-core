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
package ru.citeck.ecos.counter;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.model.CounterModel;

public class CounterServiceImpl implements CounterService {
	
	private NodeService nodeService;
	private NodeRef counterRoot;

	@Override
	public void setCounterLast(String counterName, int value) {
		NodeRef counter = this.getCounter(counterName, true);
		this.setCounter(counter, value);
	}

	@Override
	public Integer getCounterLast(String counterName) {
		NodeRef counter = this.getCounter(counterName, false);
		if(counter == null) {
			return null;
		}
		return getCounter(counter);
	}

	@Override
	public synchronized Integer getCounterNext(String counterName, boolean increment) {
		NodeRef counter = this.getCounter(counterName, increment);
		if(counter == null) {
			return null;
		}
		int value = getCounter(counter) + 1;
		if(increment) {
			setCounter(counter, value);
		}
		return value;
	}
	
	private NodeRef getCounter(String counterName, boolean createIfAbsent) {
		NodeRef counter = nodeService.getChildByName(counterRoot, ContentModel.ASSOC_CONTAINS, counterName);
		if(counter == null && createIfAbsent) {
			ChildAssociationRef counterRef = nodeService.createNode(counterRoot, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, counterName), CounterModel.TYPE_COUNTER);
			counter = counterRef.getChildRef();
			nodeService.setProperty(counter, ContentModel.PROP_NAME, counterName);
		}
		return counter;
	}
	
	private int getCounter(NodeRef counter) {
		return (Integer) nodeService.getProperty(counter, CounterModel.PROP_VALUE);
	}

	private void setCounter(NodeRef counter, int value) {
		nodeService.setProperty(counter, CounterModel.PROP_VALUE, value);
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setCounterRoot(String counterRoot) {
		this.counterRoot = new NodeRef(counterRoot);
	}

}
