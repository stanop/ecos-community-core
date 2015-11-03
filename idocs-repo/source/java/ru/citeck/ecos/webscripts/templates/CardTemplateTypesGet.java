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
package ru.citeck.ecos.webscripts.templates;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.template.CardTemplateService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Alexander Nemerov
 * @date: 05.12.13
 */
public class CardTemplateTypesGet extends DeclarativeWebScript {

	private CardTemplateService cardTemplateService;

	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		List<NodeRef> types = cardTemplateService.getTemplateTypes();
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("types", types);
		return results;
	}

	public void setCardTemplateService(CardTemplateService cardTemplateService) {
		this.cardTemplateService = cardTemplateService;
	}
}
