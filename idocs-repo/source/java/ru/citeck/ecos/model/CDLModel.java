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
import org.alfresco.service.namespace.QNamePattern;

public final class CDLModel {

	// namespace
	public static final String CDL_NAMESPACE = "http://www.citeck.ru/model/cdl/1.0";

	// types
	public static final QName TYPE_CARD_TEMPLATE_TYPE = QName.createQName(CDL_NAMESPACE, "cardTemplateType");

	// aspects

	// properties

	public static final QName PROP_CONVERSION_TO_EUR = QName.createQName(CDL_NAMESPACE, "conversionToEUR");
	public static final QName PROP_DEPARTMENT = QName.createQName(CDL_NAMESPACE, "department");
	public static final QName PROP_NOMENCLATURE_INDEX = QName.createQName(CDL_NAMESPACE, "nomenclatureIndex");

	// associations
	public static final QName ASSOC_NOMENCLATURE_TO_DEPARTMENT = QName.createQName(CDL_NAMESPACE, "nomenclature_to_department");

	public static final QName ASSOC_DEFAULT_FOLDER = QName.createQName(CDL_NAMESPACE, "defaultFolder");

}
