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

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class ICaseModel {

	// model
	public static final String MODEL_PREFIX = "icase";

	// namespace
	public static final String NAMESPACE = "http://www.citeck.ru/model/icase/1.0";

	// types
	public static final QName TYPE_ELEMENT_CONFIG = QName.createQName(NAMESPACE, "elementConfig");
	public static final QName TYPE_CLASS_CONFIG = QName.createQName(NAMESPACE, "classConfig");
	public static final QName TYPE_KEY_PROP_CONFIG = QName.createQName(NAMESPACE, "keyPropConfig");
	public static final QName TYPE_ASSOC_CONFIG = QName.createQName(NAMESPACE, "assocConfig");

	// aspects
	public static final QName ASPECT_CASE = QName.createQName(NAMESPACE, "case");
	public static final QName ASPECT_CASE_TEMPLATE = QName.createQName(NAMESPACE, "caseTemplate");
	public static final QName ASPECT_COPIED_FROM_TEMPLATE = QName.createQName(NAMESPACE, "copiedFromTemplate");

	// properties
	public static final QName PROP_CASE_CLASS = QName.createQName(NAMESPACE, "caseClass");
	public static final QName PROP_ELEMENT_TYPE = QName.createQName(NAMESPACE, "elementType");
	public static final QName PROP_COPY_ELEMENTS = QName.createQName(NAMESPACE, "copyElements");
	public static final QName PROP_ELEMENT_KEY = QName.createQName(NAMESPACE, "elementKey");
	public static final QName PROP_CASE_KEY = QName.createQName(NAMESPACE, "caseKey");
	public static final QName PROP_ASSOC_NAME = QName.createQName(NAMESPACE, "assocName");
	public static final QName PROP_ASSOC_TYPE = QName.createQName(NAMESPACE, "assocType");
	public static final QName PROP_CASE_FOLDER_ASSOC_TYPE = QName.createQName(NAMESPACE, "caseFolderAssocName");
	public static final QName PROP_FOLDER_NAME = QName.createQName(NAMESPACE, "folderName");
	public static final QName PROP_FOLDER_TYPE = QName.createQName(NAMESPACE, "folderType");
	public static final QName PROP_FOLDER_ASSOC_TYPE = QName.createQName(NAMESPACE, "folderAssocName");
	public static final QName PROP_ELEMENT_FOLDER = QName.createQName(NAMESPACE, "elementFolder");
	public static final QName PROP_TYPE_KIND = QName.createQName(NAMESPACE, "typeKind");
	public static final QName PROP_CASE_ECOS_KIND = QName.createQName(NAMESPACE, "caseEcosKind");
	public static final QName PROP_CASE_ECOS_TYPE = QName.createQName(NAMESPACE, "caseEcosType");

	// icase:subcase
	public static final QName ASPECT_SUBCASE = QName.createQName(NAMESPACE, "subcase");
	public static final QName ASSOC_SUBCASE_ELEMENT = QName.createQName(NAMESPACE, "subcaseElement");
	public static final QName ASSOC_SUBCASE_ELEMENT_CONFIG = QName.createQName(NAMESPACE, "subcaseElementConfig");
	public static final QName ASSOC_PARENT_CASE = QName.createQName(NAMESPACE, "parentCase");

	// subcase configuration properties
	public static final QName PROP_CREATE_SUBCASE = QName.createQName(NAMESPACE, "createSubcase");
	public static final QName PROP_REMOVE_SUBCASE = QName.createQName(NAMESPACE, "removeSubcase");
	public static final QName PROP_REMOVE_EMPTY_SUBCASE = QName.createQName(NAMESPACE, "removeEmptySubcase");
	public static final QName PROP_SUBCASE_TYPE = QName.createQName(NAMESPACE, "subcaseType");
    public static final QName PROP_SUBCASE_ASSOC = QName.createQName(NAMESPACE, "subcaseAssoc");

    // case element aspect
    public static final QName ASPECT_ELEMENT = QName.createQName(NAMESPACE, "element");
    
    // category element config:
    public static final QName TYPE_CATEGORY_CONFIG = QName.createQName(NAMESPACE, "categoryConfig");
    public static final QName PROP_CATEGORY_PROPERTY = QName.createQName(NAMESPACE, "categoryProperty");

    public static final QName TYPE_CASE_TEMPLATE = QName.createQName(NAMESPACE, "template");
    public static final QName PROP_CASE_TYPE = QName.createQName(NAMESPACE, "caseType");
    public static final QName PROP_CONDITION = QName.createQName(NAMESPACE, "condition");

	public static final QName TYPE_CASE_STATUS = QName.createQName(NAMESPACE, "caseStatus");
	public static final QName ASSOC_CASE_STATUS = QName.createQName(NAMESPACE, "caseStatusAssoc");
	public static final QName PROP_CASE_STATUS_CHANGED_DATETIME = QName.createQName(NAMESPACE, "caseStatusChangedDateTime");

	public static final QName ASSOC_DOCUMENTS = QName.createQName(NAMESPACE, "documents");
}
