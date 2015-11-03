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
import java.util.List;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Association Split is a Data Bundle Split, that splits Data Bundle, based on some node's peer associations.
 * 
 * Only one type of association and one direction can be specified. 
 * If other options are necessary, consider using ParallelDataBundleProcessor.
 * 
 * @author Sergey Tiunov
 *
 */
public class AssociationSplit extends AbstractAssociationSplit
{
	private QName assocName;
	private Boolean source = false;

	@Override
	protected List<Object> getAssociatedObjects(NodeRef nodeRef) {
		List<AssociationRef> assocs = null;
		if(source) {
			assocs = nodeService.getSourceAssocs(nodeRef, assocName);
		} else {
			assocs = nodeService.getTargetAssocs(nodeRef, assocName);
		}
		List<Object> results = new ArrayList<Object>(assocs.size());
		if(source) {
			for(AssociationRef assoc : assocs) {
				results.add(assoc.getSourceRef());
			}
		} else {
			for(AssociationRef assoc : assocs) {
				results.add(assoc.getTargetRef());
			}
		}
		return results;
	}

	public void setAssocName(QName assocName) {
		this.assocName = assocName;
	}

	public void setSource(Boolean source) {
		this.source = source;
	}

}
