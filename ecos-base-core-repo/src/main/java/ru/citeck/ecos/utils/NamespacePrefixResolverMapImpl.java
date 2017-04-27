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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.Auditable;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;

public class NamespacePrefixResolverMapImpl implements NamespacePrefixResolver {

    protected Map<String, String> prefixToUriMap;

    public NamespacePrefixResolverMapImpl(Map<String, String> prefixToUriMap) {
        this.prefixToUriMap = new HashMap<>(prefixToUriMap);
    }

    @Override
    @Auditable(parameters = "prefix")
    public String getNamespaceURI(String prefix) throws NamespaceException {
        return prefixToUriMap.get(prefix);
    }

    @Override
    @Auditable(parameters = "namespaceURI")
    public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException {
        List<String> prefixes = new LinkedList<>();
        for(String prefix : prefixToUriMap.keySet()) {
            if(prefixToUriMap.get(prefix).equals(namespaceURI)) {
                prefixes.add(prefix);
            }
        }
        return prefixes;
    }

    @Override
    @Auditable
    public Collection<String> getPrefixes() {
        return prefixToUriMap.keySet();
    }

    @Override
    @Auditable
    public Collection<String> getURIs() {
        return prefixToUriMap.values();
    }

}