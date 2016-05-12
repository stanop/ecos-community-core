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

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Output Content is a Data Bundle Generator, that outputs content of specified node.
 * If node does not exist, null is output.
 * If node does not have content, Data Bundle with null input stream is output.
 * 
 * Target nodeRef can be specified as expressions, supported by expression evaluator.
 * 
 * @author Sergey Tiunov
 *
 */
public class OutputContent extends AbstractDataBundleLine {

	private ContentService contentService;
	private String nodeRef;
	private QName contentPropertyName = ContentModel.PROP_CONTENT;
	
	@Override
	public void init() {
		this.contentService = serviceRegistry.getContentService();
	}
	
	@Override
	public DataBundle process(DataBundle input) {
		
		Map<String,Object> model = input.needModel();

		NodeRef document = helper.getExistingNodeRef(evaluateExpression(nodeRef, model));
		if(document == null) {
			return null;
		}
		
		ContentReader reader = contentService.getReader(document, contentPropertyName);
		return helper.getDataBundle(reader, model);
	}

	/**
	 * Set nodeRef.
	 * It can be specified as expression in the supported format.
	 * @param nodeRef
	 */
	public void setNodeRef(String nodeRef) {
		this.nodeRef = nodeRef;
	}

	/**
	 * Set content property name.
	 * If it is not set, cm:content is assumed by default.
	 * 
	 * @param contentPropertyName
	 */
	public void setContentPropertyName(QName contentPropertyName) {
		this.contentPropertyName = contentPropertyName;
	}

}
