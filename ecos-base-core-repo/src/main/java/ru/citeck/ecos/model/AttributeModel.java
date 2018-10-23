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

public interface AttributeModel {

    String NAMESPACE = "http://www.citeck.ru/model/attribute/1.0";
    String PREFIX = "attr";
    
    String NAMESPACE_SOURCE_ASSOC = "http://www.citeck.ru/model/attribute/source-assoc/1.0";
    String PREFIX_SOURCE_ASSOC = "source";
    
    QName ATTR_TYPES = QName.createQName(NAMESPACE, "types");
    QName ATTR_ASPECTS = QName.createQName(NAMESPACE, "aspects");
    QName ATTR_NODEREF = QName.createQName(NAMESPACE, "noderef");
    QName ATTR_PARENT = QName.createQName(NAMESPACE, "parent");
    QName ATTR_PARENT_ASSOC = QName.createQName(NAMESPACE, "parentassoc");
    QName ATTR_IS_CONTAINER = QName.createQName(NAMESPACE, "isContainer");
    QName ATTR_IS_DOCUMENT = QName.createQName(NAMESPACE, "isDocument");

    QName TYPE_VIRTUAL = QName.createQName(NAMESPACE, "virtual");
    QName TYPE_PROPERTY = QName.createQName(NAMESPACE, "property");
    QName TYPE_TARGET_ASSOCIATION = QName.createQName(NAMESPACE, "target-association");
    QName TYPE_SOURCE_ASSOCIATION = QName.createQName(NAMESPACE, "source-association");
    QName TYPE_CHILD_ASSOCIATION = QName.createQName(NAMESPACE, "child-association");
}
