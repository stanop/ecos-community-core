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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.invariants.Feature;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.journals.invariants.CriterionInvariantsProvider;
import ru.citeck.ecos.journals.records.JournalRecordsDAO;
import ru.citeck.ecos.journals.xml.Journal;
import ru.citeck.ecos.journals.xml.Journals;
import ru.citeck.ecos.journals.xml.Journals.Imports.Import;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.processor.TemplateExpressionEvaluator;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.utils.MandatoryParam;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaSettingsRegistry;
import ru.citeck.ecos.utils.*;

import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class JournalServiceImpl implements JournalService {

    protected static final String JOURNALS_SCHEMA_LOCATION = "alfresco/module/journals-repo/schema/journals.xsd";
    protected static final String INVARIANTS_SCHEMA_LOCATION = "alfresco/module/ecos-forms-repo/schema/invariants.xsd";

    private NodeService nodeService;
    private ServiceRegistry serviceRegistry;
    private SearchCriteriaSettingsRegistry searchCriteriaSettingsRegistry;
    private JournalRecordsDAO recordsDAO;
    private NamespaceService namespaceService;
    private NodeUtils nodeUtils;

    private LazyNodeRef journalsRoot;
    private Map<String, JournalType> journalTypes = new ConcurrentHashMap<>();

    @Autowired
    private TemplateExpressionEvaluator expressionEvaluator;

    private List<CriterionInvariantsProvider> criterionInvariantsProviders;

    private ObjectMapper objectMapper = new ObjectMapper();

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
    public Long getRecordsCount(String journal) {

        NodeRef journalRef;
        String journalId;

        if (NodeRef.isNodeRef(journal)) {
            journalRef = new NodeRef(journal);
            if (!nodeService.exists(journalRef)) {
                return 0L;
            }
            journalId = (String) nodeService.getProperty(journalRef, JournalsModel.PROP_JOURNAL_TYPE);
        } else {
            journalId = journal;
            journalRef = getJournalRef(journal);
            if (journalRef == null) {
                return 0L;
            }
        }

        JGqlPageInfoInput page = new JGqlPageInfoInput(null, 1, Collections.emptyList(), 0);

        String query = buildJournalQuery(journalRef);
        RecordsQueryResult<RecordRef> result = getRecords(journalId, query, null, page);
        return result.getTotalCount();
    }

    private String buildJournalQuery(NodeRef journalRef) {
        List<NodeRef> criteriaRefs = RepoUtils.getChildrenByAssoc(
                journalRef, JournalsModel.ASSOC_SEARCH_CRITERIA, nodeService);
        if (CollectionUtils.isEmpty(criteriaRefs)) {
            return "{}";
        }
        SearchCriteria searchCriteria = new SearchCriteria(namespaceService);
        criteriaRefs.forEach(nodeRef -> {
            Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
            QName fieldQName = (QName) props.get(JournalsModel.PROP_FIELD_QNAME);
            String predicate = (String) props.get(JournalsModel.PROP_PREDICATE);
            String criterionValue = (String) props.get(JournalsModel.PROP_CRITERION_VALUE);
            if (fieldQName == null || predicate == null || criterionValue == null) {
                return;
            }
            String field = fieldQName.toPrefixString(namespaceService);
            String criterion = "";
            if (StringUtils.isNotEmpty(criterionValue)) {
                Map<String, Object> model = new HashMap<>();
                /* this replacement is used to fix template strings like this one:
                 * <#list (people.getContainerGroups(person)![]) as group>#{group.nodeRef},</#list>#{person.nodeRef} */
                criterionValue = criterionValue.replace("#{", "${");
                criterion = (String) expressionEvaluator.evaluate(criterionValue, model);
            }
            searchCriteria.addCriteriaTriplet(field, predicate, criterion);
        });
        String criteria;
        try {
            criteria = objectMapper.writeValueAsString(searchCriteria);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json processing error.", e);
        }
        return criteria;
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
    public List<InvariantDefinition> getCriterionInvariants(String journalId, String attribute) {

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
        if (id == null) {
            return null;
        }
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
                meta -> {
                    ObjectNode attributes = meta.getAttributes();
                    attributes.put("id", meta.getId().toString());
                    return attributes;
                }
        );
    }

    @Override
    public String getJournalGqlSchema(String journalId) {
        return recordsDAO.getJournalGqlSchema(needJournalType(journalId));
    }

    @Override
    public JournalType needJournalType(String journalIdOrNodeRef) {
        MandatoryParam.check("journalId", journalIdOrNodeRef);
        String journalId;
        if (NodeRef.isNodeRef(journalIdOrNodeRef)) {
            journalId = nodeUtils.getProperty(new NodeRef(journalIdOrNodeRef), JournalsModel.PROP_JOURNAL_TYPE);
        } else {
            journalId = journalIdOrNodeRef;
        }
        JournalType journalType = getJournalType(journalId);
        if (journalType == null) {
            throw new IllegalArgumentException("Journal with id " + journalIdOrNodeRef + " is not found");
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

    @Autowired
    public void setNodeUtils(NodeUtils nodeUtils) {
        this.nodeUtils = nodeUtils;
    }
}
