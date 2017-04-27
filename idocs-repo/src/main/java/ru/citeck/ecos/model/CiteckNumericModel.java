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

public class CiteckNumericModel {

	public static final String NAMESPACE = "http://www.citeck.ru/model/numeric/1.0";
	
	public static final QName ASPECT_HAS_BIG_NUMBERS = QName.createQName(NAMESPACE, "hasBigNumbers");

    public static final String BIG_NUMBER_SUFFIX = "_BIG";
    public static final String INDEX_SUFFIX = "_INDEX";

    /**
     * Check whether property is big-number property.
     * @param name
     * @return
     */
    public static boolean isBigProp(QName name) {
    	return isBigProp(name.getLocalName());
    }

    /**
     * Check whether property is big-number property.
     * @param name
     * @return
     */
    public static boolean isBigProp(String name) {
		return name.endsWith(BIG_NUMBER_SUFFIX);
	}

    /**
     * Check whether property is indexing property.
     * @param name
     * @return
     */
	public static boolean isIndexProp(QName name) {
		return isIndexProp(name.getLocalName());
	}

    /**
     * Check whether property is indexing property.
     * @param name
     * @return
     */
	public static boolean isIndexProp(String name) {
		return name.endsWith(INDEX_SUFFIX);
	}

    /**
     * Get index property for big-number property.
     * @param name - name of big-number property
     * @return
     */
    public static QName getIndexProp(QName bigProp) {
		return QName.createQName(bigProp.getNamespaceURI(), getIndexProp(bigProp.getLocalName()));
	}

    /**
     * Get index property for big-number property.
     * @param name - name of big-number property
     * @return
     */
	public static String getIndexProp(String bigProp) {
		return bigProp.replaceFirst(BIG_NUMBER_SUFFIX + "$", INDEX_SUFFIX);
	}

}
