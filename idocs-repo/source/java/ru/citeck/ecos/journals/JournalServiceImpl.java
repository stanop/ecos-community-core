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
package ru.citeck.ecos.journals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.Unmarshaller;

import org.alfresco.service.namespace.NamespacePrefixResolver;

import ru.citeck.ecos.journals.xml.Journal;
import ru.citeck.ecos.journals.xml.Journals;
import ru.citeck.ecos.journals.xml.Journals.Imports.Import;
import ru.citeck.ecos.model.AttributeModel;
import ru.citeck.ecos.utils.NamespacePrefixResolverMapImpl;
import ru.citeck.ecos.utils.XMLUtils;

class JournalServiceImpl implements JournalService {

    private static final String SCHEMA_LOCATION = "alfresco/module/idocs-repo/schema/journals.xsd";
    
    private Map<String, JournalType> journalTypes = new TreeMap<>();
    
    @Override
    public void deployJournalTypes(InputStream inputStream) {
        Journals data = parseXML(inputStream);
        
        NamespacePrefixResolver prefixResolver = 
                new NamespacePrefixResolverMapImpl(
                        getPrefixToUriMap(data.getImports().getImport()));
        
        for(Journal journal : data.getJournal()) {
            this.journalTypes.put(journal.getId(), new JournalTypeImpl(journal, prefixResolver));
        }
    }

    private Journals parseXML(InputStream inputStream) {
        try {
            Unmarshaller jaxbUnmarshaller = XMLUtils.createUnmarshaller(Journals.class, 
                    SCHEMA_LOCATION);
            return (Journals) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not parse journals file", e);
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
    
    @Override
    public JournalType getJournalType(String id) {
        return journalTypes.get(id);
    }

    @Override
    public Collection<JournalType> getAllJournalTypes() {
        return new ArrayList<>(journalTypes.values());
    }

}
