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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.invariants.Feature;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.journals.invariants.CriterionInvariantsProvider;
import ru.citeck.ecos.journals.records.JournalRecordsDAO;
import ru.citeck.ecos.journals.xml.Journal;
import ru.citeck.ecos.journals.xml.Journals;
import ru.citeck.ecos.journals.xml.Journals.Imports.Import;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.request.result.RecordsResult;
import ru.citeck.ecos.search.SearchCriteriaSettingsRegistry;
import ru.citeck.ecos.utils.LazyNodeRef;
import ru.citeck.ecos.utils.NamespacePrefixResolverMapImpl;
import ru.citeck.ecos.utils.XMLUtils;

import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class JournalServiceImpl implements JournalService {

    private static final String JOURNAL_OPTION_TYPE = "type";

    protected static final String JOURNALS_SCHEMA_LOCATION = "alfresco/module/journals-repo/schema/journals.xsd";
    protected static final String INVARIANTS_SCHEMA_LOCATION = "alfresco/module/ecos-forms-repo/schema/invariants.xsd";

    private NodeService nodeService;
    private ServiceRegistry serviceRegistry;
    private SearchCriteriaSettingsRegistry searchCriteriaSettingsRegistry;
    private JournalRecordsDAO recordsDAO;
    private NamespaceService namespaceService;

    private LazyNodeRef journalsRoot;
    private Map<String, JournalType> journalTypes = new ConcurrentHashMap<>();

    private List<CriterionInvariantsProvider> criterionInvariantsProviders;

    public JournalServiceImpl() {
        criterionInvariantsProviders = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void deployJournalTypes(InputStream inputStream) {
        Journals data = parseXML(inputStream);
        
        NamespacePrefixResolver prefixResolver = 
                new NamespacePrefixResolverMapImpl(
                        getPrefixToUriMap(data.getImports().getImport()));
        
        for(Journal journal : data.getJournal()) {
            searchCriteriaSettingsRegistry.cleanFieldNameCache(journal.getId());
            this.journalTypes.put(journal.getId(), new JournalTypeImpl(journal, prefixResolver, serviceRegistry,
                    searchCriteriaSettingsRegistry));
            Map<String, String> options = journalTypes.get(journal.getId()).getOptions();
            String nodeType = options.get("type");
            if (nodeType != null) {
                searchCriteriaSettingsRegistry.registerJournalNodeType(journal.getId(), nodeType);
            }
            String staticQuery = options.get("staticQuery");
            if (staticQuery != null) {
                searchCriteriaSettingsRegistry.registerJournalStaticQuery(journal.getId(), staticQuery);
            }
        }
    }

    @Override
    public Optional<JournalType> getJournalForType(QName typeName) {

        String typeShortName = typeName.toPrefixString(namespaceService);

        Collection<JournalType> types = getAllJournalTypes();
        for (JournalType type : types) {
            if (typeShortName.equals(type.getOptions().get("type"))) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<InvariantDefinition> getCriterionInvariants(String journalId, QName attribute) {

        JournalType journalType = journalTypes.get(journalId);

        Set<Feature> features = new HashSet<>();
        List<InvariantDefinition> invariants = new ArrayList<>();

        for (CriterionInvariantsProvider provider : criterionInvariantsProviders) {
            List<InvariantDefinition> provInvariants = provider.getInvariants(journalType, attribute);
            for (InvariantDefinition inv : provInvariants) {
                if (features.add(inv.getFeature())) {
                    invariants.add(inv);
                }
            }
        }

        return invariants;
    }

    protected Journals parseXML(InputStream inputStream) {
        try {
            Unmarshaller jaxbUnmarshaller = XMLUtils.createUnmarshaller(Journals.class,
                                                                        INVARIANTS_SCHEMA_LOCATION,
                                                                        JOURNALS_SCHEMA_LOCATION);
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
    public void clearCache() {
        for (CriterionInvariantsProvider provider : criterionInvariantsProviders) {
            provider.clearCache();
        }
        recordsDAO.clearCache();
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

    @Override
    public void registerCriterionInvariantsProvider(CriterionInvariantsProvider provider) {
        criterionInvariantsProviders.add(provider);
        criterionInvariantsProviders.sort(null);
    }

    @Override
    public RecordsQueryResult<RecordRef> getRecords(String journalId,
                                                    String query,
                                                    String language,
                                                    JGqlPageInfoInput pageInfo,
                                                    boolean debug) {
        if (pageInfo == null) {
            pageInfo = JGqlPageInfoInput.DEFAULT;
        }
        JournalType journalType = needJournalType(journalId);
        return recordsDAO.getRecords(journalType, query, language, pageInfo, debug);
    }

    @Override
    public RecordsQueryResult<ObjectNode> getRecordsWithData(String journalId,
                                                             String query,
                                                             String language,
                                                             JGqlPageInfoInput pageInfo,
                                                             boolean debug) {
        if (pageInfo == null) {
            pageInfo = JGqlPageInfoInput.DEFAULT;
        }
        JournalType journalType = needJournalType(journalId);
        return new RecordsQueryResult<>(
                recordsDAO.getRecordsWithData(journalType, query, language, pageInfo, debug),
                RecordMeta::getAttributes
        );
    }

    @Override
    public JournalType needJournalType(String journalId) {
        JournalType journalType = getJournalType(journalId);
        if (journalType == null) {
            throw new IllegalArgumentException("Journal with id " + journalId + " not found");
        }
        return journalType;
    }

    public void setJournalsRoot(LazyNodeRef journalsRoot) {
        this.journalsRoot = journalsRoot;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public void setRecordsDAO(JournalRecordsDAO recordsDAO) {
        this.recordsDAO = recordsDAO;
    }

    public void setSearchCriteriaSettingsRegistry(SearchCriteriaSettingsRegistry searchCriteriaSettingsRegistry) {
        this.searchCriteriaSettingsRegistry = searchCriteriaSettingsRegistry;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public SearchCriteriaSettingsRegistry getSearchCriteriaSettingsRegistry() {
        return searchCriteriaSettingsRegistry;
    }
}
