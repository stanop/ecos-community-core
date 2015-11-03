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
package ru.citeck.ecos.invariants;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;

import org.alfresco.service.namespace.NamespacePrefixResolver;

import ru.citeck.ecos.invariants.xml.Invariants;
import ru.citeck.ecos.invariants.xml.Invariants.Imports.Import;
import ru.citeck.ecos.model.AttributeModel;
import ru.citeck.ecos.utils.NamespacePrefixResolverMapImpl;
import ru.citeck.ecos.utils.XMLUtils;

class InvariantsParser {
    
    public List<InvariantDefinition> parse(InputStream inputStream, InvariantPriority priority) {
        Invariants data = parseXML(inputStream);
        
        NamespacePrefixResolver prefixResolver = 
                new NamespacePrefixResolverMapImpl(
                        getPrefixToUriMap(data.getImports().getImport()));
        
        return InvariantDefinition.Builder.buildInvariants(data.getScopes(), priority, prefixResolver);
    }
    
    private Invariants parseXML(InputStream inputStream) {
        try {
            Unmarshaller jaxbUnmarshaller = XMLUtils.createUnmarshaller(Invariants.class, 
                    InvariantConstants.INVARIANTS_SCHEMA_LOCATION);
            return (Invariants) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not parse invariants file", e);
        }
    }
    
    private static Map<String, String> getPrefixToUriMap(List<Import> namespaces) {
        Map<String, String> prefixToUriMap = new HashMap<>(namespaces.size());
        for(Import namespace : namespaces) {
            prefixToUriMap.put(namespace.getPrefix(), namespace.getUri());
        }
        prefixToUriMap.put("", AttributeModel.NAMESPACE); // virtual attributes by default
        return prefixToUriMap;
    }
    
}
