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
package ru.citeck.ecos.invariants.view;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.alfresco.service.namespace.NamespacePrefixResolver;

import ru.citeck.ecos.invariants.InvariantConstants;
import ru.citeck.ecos.invariants.xml.ViewRoot;
import ru.citeck.ecos.invariants.xml.ViewRoot.Imports.Import;
import ru.citeck.ecos.utils.NamespacePrefixResolverMapImpl;
import ru.citeck.ecos.utils.XMLUtils;

class NodeViewsParser {
    
    public List<NodeViewElement> parse(InputStream inputStream) {
        ViewRoot data = parseXML(inputStream);
        
        NamespacePrefixResolver prefixResolver = 
                new NamespacePrefixResolverMapImpl(
                        getPrefixToUriMap(data.getImports().getImport()));
        
        return NodeViewElement.Builder.buildElements(data.getElements(), prefixResolver);
    }

    private static ViewRoot parseXML(InputStream inputStream) {
        try {
            Unmarshaller jaxbUnmarshaller = XMLUtils.createUnmarshaller(ViewRoot.class, 
                    InvariantConstants.INVARIANTS_SCHEMA_LOCATION, 
                    InvariantConstants.VIEWS_SCHEMA_LOCATION);
            JAXBElement<?> element = (JAXBElement<?>) jaxbUnmarshaller.unmarshal(inputStream);
            return (ViewRoot) element.getValue();
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not parse views file", e);
        }
    }

    private static Map<String, String> getPrefixToUriMap(List<Import> namespaces) {
        Map<String, String> prefixToUriMap = new HashMap<>(namespaces.size() + 1);
        for(Import namespace : namespaces) {
            prefixToUriMap.put(namespace.getPrefix(), namespace.getUri());
        }
        return prefixToUriMap;
    }

    
}
