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
package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public class ArchiveServiceConfigurationModel {

	// model
	public static final String MODEL_PREFIX = "arch";

	// namespace
	public static final String NAMESPACE = "http://www.citeck.ru/model/archive-service-configuration/1.0";
	
	// types
	public static final QName TYPE_ARCH_CONFIG = QName.createQName(NAMESPACE, "archConfig");

	// properties
	public static final QName PROP_NODE_TYPE = QName.createQName(NAMESPACE, "nodeType");
	public static final QName PROP_ASSOC_TYPE = QName.createQName(NAMESPACE, "assocType");
	public static final QName PROP_DESTINATION = QName.createQName(NAMESPACE, "destination");

}
