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

import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.node.NodeInfo;

public class TypeNumberFormProcessor extends AbstractNumberFormProcessor<TypeDefinition>
{
	private static Log logger = LogFactory.getLog(TypeNumberFormProcessor.class);
	
	@Override
	protected Log getLogger() {
		return logger;
	}

	@Override
	protected String getItemType(TypeDefinition type) {
		return type.getName().toPrefixString(namespaceService);
	}

	@Override
	protected String getItemURI(TypeDefinition type) {
		return "api/classes/" + this.getItemType(type).replace(':', '_');
	}

	@Override
	protected TypeDefinition getTypedItem(Item item) {
		String itemId = item.getId();
		QName typeName = QName.createQName(itemId, namespaceService);
		return dictionaryService.getType(typeName);
	}

	@Override
	protected NodeInfo createNodeInfo(TypeDefinition item) {
		// return empty nodeInfo
		return nodeInfoFactory.createNodeInfo();
	}

}
