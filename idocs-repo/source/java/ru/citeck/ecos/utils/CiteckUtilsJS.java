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
package ru.citeck.ecos.utils;

import org.alfresco.service.namespace.QName;

public class CiteckUtilsJS extends AlfrescoScopableProcessorExtension {

	public QName createQName(String s) {
		QName qname;
		if (s.indexOf(QName.NAMESPACE_BEGIN) != -1) {
			qname = QName.createQName(s);
		} else {
			qname = QName.createQName(s, this.serviceRegistry.getNamespaceService());
		}
		return qname;
	}

}
