/*
 * Copyright (C) 2008-2019 Citeck LLC.
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

public interface PersonalDocumentsModel {

    public static final String NAMESPACE = "http://www.citeck.ru/model/personal/documents/1.0";

    public static final QName TYPE_PERSONAL_DOCUMENTS = QName.createQName(NAMESPACE, "personalDocuments");

    public static final QName ASSOC_DOCUMENT_LINK = QName.createQName(NAMESPACE, "documentLink");
}
