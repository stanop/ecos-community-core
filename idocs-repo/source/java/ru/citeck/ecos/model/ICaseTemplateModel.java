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

public class ICaseTemplateModel {

	// model
	public static final String MODEL_PREFIX = "icasetpl";

	// namespace
	public static final String NAMESPACE = "http://www.citeck.ru/model/icase/template/1.0";

	// element type
    public static final QName TYPE_ELEMENT_TYPE = QName.createQName(NAMESPACE, "elementType");
    public static final QName ASSOC_ELEMENT_CONFIG = QName.createQName(NAMESPACE, "elementConfig");
    public static final QName ASSOC_EXTERNAL_ELEMENTS = QName.createQName(NAMESPACE, "externalElements");
    public static final QName ASSOC_INTERNAL_ELEMENTS = QName.createQName(NAMESPACE, "internalElements");

    // template
    public static final QName TYPE_TEMPLATE = QName.createQName(ICaseModel.NAMESPACE, "template");
    public static final QName PROP_CASE_TYPE = QName.createQName(ICaseModel.NAMESPACE, "caseType");
    public static final QName PROP_CONDITION = QName.createQName(ICaseModel.NAMESPACE, "condition");
    public static final QName ASSOC_ELEMENT_TYPES = QName.createQName(NAMESPACE, "elementTypes");
    
}
