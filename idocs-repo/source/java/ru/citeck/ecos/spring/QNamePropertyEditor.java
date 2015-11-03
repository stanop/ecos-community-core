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
package ru.citeck.ecos.spring;

import java.beans.PropertyEditorSupport;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class QNamePropertyEditor extends PropertyEditorSupport 
{
	private NamespaceService namespaceService;
	
	@Override
	public void setAsText(String text) {
		QName qname = null;
		if(text.matches("^[{].+[}].+$")) {
			qname = QName.createQName(text);
		} else {
			qname = QName.createQName(text, namespaceService);
		}
		setValue(qname);
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
}
