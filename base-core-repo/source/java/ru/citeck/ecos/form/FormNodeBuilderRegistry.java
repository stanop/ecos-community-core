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
package ru.citeck.ecos.form;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class FormNodeBuilderRegistry {
	
	private DictionaryService dictionaryService;
	private NamespaceService namespaceService;
	
	private Map<String, FormNodeBuilder> registry = new HashMap<String, FormNodeBuilder>();
	
	public void addFormNodeBuilder(String typeName, FormNodeBuilder builder) {
		registry.put(typeName, builder);
	}
	
	public FormNodeBuilder getNodeBuilder(TypeDefinition typeDef) {
		// search for typeDef:
		for(String builderTypeName : registry.keySet()) {
			if(dictionaryService.isSubClass(typeDef.getName(), QName.resolveToQName(namespaceService, builderTypeName))) {
				return registry.get(builderTypeName);
			}
		}
		return null;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
}
