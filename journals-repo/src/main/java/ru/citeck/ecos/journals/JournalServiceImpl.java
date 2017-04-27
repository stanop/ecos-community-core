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
import java.util.*;

import javax.xml.bind.Unmarshaller;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;

import org.alfresco.service.namespace.RegexQNamePattern;
import ru.citeck.ecos.journals.xml.Journal;
import ru.citeck.ecos.journals.xml.Journals;
import ru.citeck.ecos.journals.xml.Journals.Imports.Import;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.utils.LazyNodeRef;
import ru.citeck.ecos.utils.NamespacePrefixResolverMapImpl;
import ru.citeck.ecos.utils.XMLUtils;

class JournalServiceImpl implements JournalService {

    protected static final String SCHEMA_LOCATION = "alfresco/module/journals-repo/schema/journals.xsd";

    private NodeService nodeService;
    private ServiceRegistry serviceRegistry;

    private LazyNodeRef journalsRoot;
    private Map<String, JournalType> journalTypes = new TreeMap<>();
    
    @Override
    public void deployJournalTypes(InputStream inputStream) {
        Journals data = parseXML(inputStream);
        
        NamespacePrefixResolver prefixResolver = 
                new NamespacePrefixResolverMapImpl(
                        getPrefixToUriMap(data.getImports().getImport()));
        
        for(Journal journal : data.getJournal()) {
            this.journalTypes.put(journal.getId(), new JournalTypeImpl(journal, prefixResolver, serviceRegistry));
        }
    }

    protected Journals parseXML(InputStream inputStream) {
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

    @Override
    public NodeRef getJournalRef(String id) {
        List<ChildAssociationRef> associationRefs = nodeService.getChildAssocs(journalsRoot.getNodeRef(),
                                                                               ContentModel.ASSOC_CONTAINS,
                                                                               RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef associationRef : associationRefs) {
            NodeRef journalRef = associationRef.getChildRef();
            String journalID = (String) nodeService.getProperty(journalRef, JournalsModel.PROP_JOURNAL_TYPE);
            if (Objects.equals(journalID, id)) {
                return journalRef;
            }
        }
        return null;
    }

    public void setJournalsRoot(LazyNodeRef journalsRoot) {
        this.journalsRoot = journalsRoot;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
    }
}
