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

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.util.HashMap;
import java.util.Map;

public class AssociationIndexPropertyRegistrar {
    
    private AssociationIndexPropertyRegistry registry;
    private NamespaceService namespaceService;
    private Map<String, String> assocsMapping;
    private String assocName, indexName;

    public AssociationIndexPropertyRegistrar(AssociationIndexPropertyRegistry registry) {
        this.registry = registry;
    }
    
    public void init() {
        if (assocsMapping == null) {
            assocsMapping = new HashMap<>();
        }
        if (assocName != null && indexName != null) {
            assocsMapping.put(assocName, indexName);
        }
        for (Map.Entry<String, String> entry : assocsMapping.entrySet()){
            QName assoc = QName.resolveToQName(namespaceService, entry.getKey());
            QName prop = QName.resolveToQName(namespaceService, entry.getValue());
            registry.registerAssociationIndexProperty(assoc, prop);
        }
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setAssocName(String assocName) {
        this.assocName = assocName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public void setAssocsMapping(Map<String, String> assocsMapping) {
        this.assocsMapping = assocsMapping;
    }
}

