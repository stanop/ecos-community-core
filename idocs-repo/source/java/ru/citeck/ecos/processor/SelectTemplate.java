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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.template.CardTemplateService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: alexander.nemerov
 * Date: 13.11.13
 */
public class SelectTemplate extends AbstractDataBundleLine{

	private NodeService nodeService;
	private CardTemplateService cardTemplateService;
	private String nodeExpr;
	private String templateTypeExpr;
	private String variable;

	@Override
	public void init() {
		this.nodeService = serviceRegistry.getNodeService();
		this.cardTemplateService = (CardTemplateService) serviceRegistry.getService(CiteckServices.CARD_TEMPLATE_SERVICE);
	}


	@Override
	public DataBundle process(DataBundle input) {
		Map<String,Object> model = input.needModel();
		Map<String,Object> newModel = new HashMap<String, Object>();
		newModel.putAll(model);

		String nodeRefStr = evaluateExpression(nodeExpr, model).toString();
		NodeRef nodeRef = new NodeRef(nodeRefStr);
		newModel.put("document", nodeRef);

		QName documentType = nodeService.getType(nodeRef);
		String templateType = (String) this.evaluateExpression(templateTypeExpr, input);
		List<NodeRef> templates = cardTemplateService.getTemplatesForType(documentType, templateType);
		if(templates.size() > 0) {
			String template = templates.get(0).toString();
			newModel.put(variable, template);
		} else {
			throw new AlfrescoRuntimeException("There is no template of type '" + templateType + "' for document type: '" + documentType);
		}
		return new DataBundle(input, newModel);
	}

	public void setNodeExpr(String nodeExpr) {
		this.nodeExpr = nodeExpr;
	}

	public void setTemplateType(String templateTypeExpr) {
		this.templateTypeExpr = templateTypeExpr;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

}
