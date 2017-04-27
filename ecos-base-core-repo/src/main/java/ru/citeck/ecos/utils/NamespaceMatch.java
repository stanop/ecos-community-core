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
package ru.citeck.ecos.utils;

import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

public class NamespaceMatch implements QNamePattern {
    
    private final String requiredNamespace;
    
    public NamespaceMatch(String requiredNamespace) {
        this.requiredNamespace = requiredNamespace;
    }
    
    @Override
    public boolean isMatch(QName qname) {
        if(qname == null) return false;
        String namespace = qname.getNamespaceURI();
        return requiredNamespace == namespace
            || requiredNamespace != null && requiredNamespace.equals(namespace);
    }
    
}
