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

public final class ClassificationModel {
	// models
	public static final String CLASSIFICATION_MODEL_PREFIX = "class";

	// namespaces
	public static final String CLASSIFICATION_NAMESPACE = "http://www.citeck.ru/model/content/classification/1.0";
	public static final String CLASSIFICATION_TYPE_KIND_NAMESPACE = "http://www.citeck.ru/model/content/classification/tk/1.0";

	// aspects
	public static final QName ASPECT_DOCUMENT_TYPE_KIND = QName.createQName(CLASSIFICATION_TYPE_KIND_NAMESPACE, "documentTypeKind");
	public static final QName ASPECT_DOCUMENT_TYPE_KIND_TEMPLATE = QName.createQName(CLASSIFICATION_TYPE_KIND_NAMESPACE, "documentTypeKindTemplate");

	// properties
	public static final QName PROP_DOCUMENT_TYPE = QName.createQName(CLASSIFICATION_TYPE_KIND_NAMESPACE, "type");
	public static final QName PROP_DOCUMENT_KIND = QName.createQName(CLASSIFICATION_TYPE_KIND_NAMESPACE, "kind");

	//now properties
	public static final QName PROP_DOCUMENT_APPLIES_TO_TYPE = QName.createQName(CLASSIFICATION_TYPE_KIND_NAMESPACE, "appliesToType");
	public static final QName PROP_DOCUMENT_APPLIES_TO_KIND = QName.createQName(CLASSIFICATION_TYPE_KIND_NAMESPACE, "appliesToKind");

    public static final QName ASPECT_APPLIED_CLASSES = QName.createQName(CLASSIFICATION_TYPE_KIND_NAMESPACE, "appliedClasses");
    public static final QName PROP_APPLIED_TYPE = QName.createQName(CLASSIFICATION_TYPE_KIND_NAMESPACE, "appliedType");
    public static final QName PROP_APPLIED_ASPECTS = QName.createQName(CLASSIFICATION_TYPE_KIND_NAMESPACE, "appliedAspects");

}
