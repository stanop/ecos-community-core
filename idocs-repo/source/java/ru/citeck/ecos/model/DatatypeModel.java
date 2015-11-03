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

public interface DatatypeModel {

    public static final String NAMESPACE = "http://www.citeck.ru/model/datatype/1.0";
    public static final String PREFIX = "data";
    
    public static final QName DATA_TYPE_NAME = QName.createQName(NAMESPACE, "typename");
    public static final QName DATA_ASPECT_NAME = QName.createQName(NAMESPACE, "aspectname");
    public static final QName DATA_CLASS_NAME = QName.createQName(NAMESPACE, "classname");
    
    public static final QName DATA_PROP_NAME = QName.createQName(NAMESPACE, "propname");
    public static final QName DATA_ASSOC_NAME = QName.createQName(NAMESPACE, "assocname");
    public static final QName DATA_ATTR_NAME = QName.createQName(NAMESPACE, "attrname");
    
}
