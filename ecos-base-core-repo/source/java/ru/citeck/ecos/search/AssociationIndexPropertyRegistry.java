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
package ru.citeck.ecos.search;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class AssociationIndexPropertyRegistry {

    private static final String INDEX_PROP_SUFFIX = "_added";
    
    private Map<QName, QName> indexProperties = new HashMap<QName, QName>();
    
    public QName getAssociationIndexProperty(QName assocName) {
        QName explicitlyRegistered = indexProperties.get(assocName);
        if(explicitlyRegistered != null) {
            return explicitlyRegistered;
        }
        return QName.createQName(assocName.getNamespaceURI(), assocName.getLocalName() + INDEX_PROP_SUFFIX);
    }
    
    public String getAssociationIndexProperty(String assocName, NamespaceService namespaceService) {
        QName assocQName = QName.createQName(assocName, namespaceService);
        QName indexQName = getAssociationIndexProperty(assocQName);
        return indexQName.toPrefixString(namespaceService);
    }
    
    public void registerAssociationIndexProperty(QName assocName, QName indexName) {
        indexProperties.put(assocName, indexName);
    }

}
