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

public interface CardletModel {
	
	public static final String NAMESPACE = "http://www.citeck.ru/model/cardlet/1.0";
	
	public static final QName TYPE_CARDLET = QName.createQName(NAMESPACE, "cardlet");
	public static final QName PROP_CARD_MODE = QName.createQName(NAMESPACE, "cardMode");
	public static final QName PROP_REGION_ID = QName.createQName(NAMESPACE, "regionId");
	public static final QName PROP_REGION_COLUMN = QName.createQName(NAMESPACE, "regionColumn");
	public static final QName PROP_REGION_POSITION = QName.createQName(NAMESPACE, "regionPosition");
	
	public static final QName TYPE_CARD_MODE = QName.createQName(NAMESPACE, "cardMode");
	public static final QName PROP_CARD_MODE_ID = QName.createQName(NAMESPACE, "cardModeId");
	public static final QName PROP_CARD_MODE_ORDER = QName.createQName(NAMESPACE, "cardModeOrder");
	
	public static final QName ASPECT_SCOPED = QName.createQName(NAMESPACE, "scoped");
	public static final QName PROP_ALLOWED_TYPE = QName.createQName(NAMESPACE, "allowedType");
	public static final QName PROP_ALLOWED_AUTHORITIES = QName.createQName(NAMESPACE, "allowedAuthorities");
	public static final QName PROP_CONDITION = QName.createQName(NAMESPACE, "condition");

}
