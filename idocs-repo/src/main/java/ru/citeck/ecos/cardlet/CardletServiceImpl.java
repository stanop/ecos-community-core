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
package ru.citeck.ecos.cardlet;

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.cardlet.config.CardletsRegistry;
import ru.citeck.ecos.cardlet.xml.Cardlet;
import ru.citeck.ecos.cardlet.xml.ColumnType;
import ru.citeck.ecos.model.CardletModel;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/*default*/ class CardletServiceImpl implements CardletService {

    private static final Log logger = LogFactory.getLog(CardletServiceImpl.class);

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private AuthorityService authorityService;
    private SearchService searchService;
    private ScriptService scriptService;
    private Repository repositoryHelper;
    private CardletsRegistry cardletsRegistry;

    private String scriptEngine;

    @Override
    public List<Cardlet> queryCardlets(NodeRef nodeRef) {
        return queryCardlets(nodeRef, DEFAULT_MODE);
    }

    @Override
    public List<Cardlet> queryCardlets(NodeRef nodeRef, String cardMode) {
        List<QName> types = getAllNodeTypes(nodeRef);
        Collection<String> authorities = getAllUserAuthorities();
        Map<String, Object> conditionModel = buildConditionModel(nodeRef);
        return queryCardletsImpl(types, authorities, cardMode, conditionModel);
    }

    @Override
    public List<NodeRef> queryCardModes(NodeRef nodeRef) {
        List<QName> types = getAllNodeTypes(nodeRef);
        Collection<String> authorities = getAllUserAuthorities();
        Map<String, Object> conditionModel = buildConditionModel(nodeRef);
        return queryCardModesImpl(types, authorities, conditionModel);
    }

    @Override
    public CardletsWithModes queryCardletsWithModes(NodeRef nodeRef) {

        List<QName> types = getAllNodeTypes(nodeRef);
        Collection<String> authorities = getAllUserAuthorities();
        Map<String, Object> conditionModel = buildConditionModel(nodeRef);

        return new CardletsWithModes(
                queryCardletsImpl(types, authorities, ALL_MODES, conditionModel),
                queryCardModesImpl(types, authorities, conditionModel)
        );
    }

    private List<Cardlet> queryCardletsImpl(List<QName> types,
                                            Collection<String> authorities,
                                            String cardMode,
                                            Map<String, Object> conditionModel) {

        if (cardMode == null) {
            cardMode = DEFAULT_MODE;
        } else if (cardMode.equals(ALL_MODES)) {
            cardMode = null;
        }

        List<Cardlet> cardlets = cardletsRegistry.getCardlets(types, authorities, cardMode);

        return cardlets.stream()
                .filter(c ->
                    !ColumnType.DISABLED.equals(c.getPosition().getColumn()) &&
                    conditionAllows(c.getCondition(), conditionModel)
                ).sorted(Comparator.comparing(c -> c.getPosition().getOrder()))
                .collect(Collectors.toList());
    }

    private List<NodeRef> queryCardModesImpl(List<QName> types,
                                             Collection<String> authorities,
                                             Map<String, Object> conditionModel) {

        Comparator<NodeRef> precedenceComparator = new ScopedObjectsPrecedenceComparator(types);

        // get all card modes
        List<NodeRef> cardModes = queryCardModes(types, authorities);

        // group by card mode id
        Map<Object, List<NodeRef>> cardModesById = groupBy(cardModes, CardletModel.PROP_CARD_MODE_ID);

        // get resulting card modes
        List<NodeRef> resultCardModes = new LinkedList<>();
        for (List<NodeRef> regionCardModes : cardModesById.values()) {
            NodeRef mostSuitableCardMode = findMostSuitable(regionCardModes, precedenceComparator,
                    conditionModel);
            if (mostSuitableCardMode != null) {
                resultCardModes.add(mostSuitableCardMode);
            }
        }
        resultCardModes.sort(new PropertyValueComparator(nodeService, CardletModel.PROP_CARD_MODE_ORDER));
        return resultCardModes;
    }

    private List<NodeRef> queryCardModes(List<QName> types, Collection<String> authorities) {
        String typeClause = "TYPE:\"" + CardletModel.TYPE_CARD_MODE + "\"";
        String documentClause = disjunction(CardletModel.PROP_ALLOWED_TYPE, types, false, true);
        authorities.add("");
        String authorityClause = disjunction(CardletModel.PROP_ALLOWED_AUTHORITIES, authorities, false, true);
        String query = typeClause + " AND " + documentClause + " AND " + authorityClause;
        if (logger.isDebugEnabled()) {
            logger.debug("Quering card modes: " + query);
        }

        List<NodeRef> cardModes;
        cardModes = getCardItemsRefs(query);
        if (logger.isDebugEnabled()) {
            logger.debug("Found card modes: " + cardModes.size());
        }
        return cardModes;
    }

    private List<NodeRef> getCardItemsRefs(String query) {
        ResultSet results = null;
        List<NodeRef> cardModes;
        try {
            SearchParameters searchParameters = new SearchParameters();
            searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
            searchParameters.setQuery(query);
            searchParameters.setQueryConsistency(QueryConsistency.TRANSACTIONAL);
            results = searchService.query(searchParameters);
            cardModes = results.getNodeRefs();
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return cardModes;
    }

    private String disjunction(QName property, Collection<?> objects, boolean allowNull, boolean exactSearch) {
        List<String> clauses = new LinkedList<>();
        String exactPrefix = exactSearch ? "=" : "";
        for (Object object : objects) {
            if (object == null) continue;
            clauses.add(exactPrefix + "@" + property + ":\"" + object.toString().replaceAll("[\"]", "\\\"") + "\"");
        }
        if (allowNull) {
            clauses.add("ISNULL:\"" + property + "\"");
        }
        return "(" + StringUtils.join(clauses, " OR ") + ")";
    }

    private List<QName> getAllNodeTypes(NodeRef nodeRef) {
        List<QName> nodeTypes = new ArrayList<>();
        QName baseType = nodeService.getType(nodeRef);
        nodeTypes.add(baseType);
        nodeTypes.addAll(nodeService.getAspects(nodeRef));
        ClassDefinition typeDef = dictionaryService.getClass(baseType);
        if (typeDef != null) {
            typeDef = typeDef.getParentClassDefinition();
        }
        while (typeDef != null) {
            nodeTypes.add(typeDef.getName());
            typeDef = typeDef.getParentClassDefinition();
        }
        return nodeTypes;
    }

    private Collection<String> getAllUserAuthorities() {
        Collection<String> groups = authorityService.getAuthorities();
        Collection<String> result = new ArrayList<>(groups.size() + 1);
        result.addAll(groups);
        result.add(AuthenticationUtil.getFullyAuthenticatedUser());
        return result;
    }

    private Map<String, Object> buildConditionModel(NodeRef document) {
        return scriptService.buildDefaultModel(
                repositoryHelper.getPerson(),
                repositoryHelper.getCompanyHome(),
                repositoryHelper.getUserHome(repositoryHelper.getPerson()),
                null, // script
                document, // document
                nodeService.getPrimaryParent(document).getParentRef() // space
        );
    }

    private boolean conditionAllows(NodeRef nodeRef, Map<String, Object> scriptModel) {
        String condition = (String) nodeService.getProperty(nodeRef, CardletModel.PROP_CONDITION);
        return conditionAllows(condition, scriptModel);
    }

    private boolean conditionAllows(String condition, Map<String, Object> scriptModel) {
        if (condition == null || condition.isEmpty()) {
            return true;
        }
        Object conditionResult = scriptService.executeScriptString(scriptEngine, condition, scriptModel);
        if (conditionResult instanceof Boolean) {
            return (Boolean) conditionResult;
        }
        return false;
    }

    private NodeRef findMostSuitable(List<NodeRef> objects, Comparator<NodeRef> precedenceComparator, Map<String, Object> conditionModel) {
        List<NodeRef> objectsToEvaluate = new LinkedList<>();
        objectsToEvaluate.addAll(objects);
        while (objectsToEvaluate.size() > 0) {
            NodeRef mostSuitable = Collections.min(objectsToEvaluate, precedenceComparator);
            if (conditionAllows(mostSuitable, conditionModel)) {
                return mostSuitable;
            } else {
                objectsToEvaluate.remove(mostSuitable);
            }
        }
        return null;
    }

    private Map<Object, List<NodeRef>> groupBy(List<NodeRef> objects, QName property) {
        Map<Object, List<NodeRef>> result = new HashMap<>();
        for (NodeRef object : objects) {
            Object value = nodeService.getProperty(object, property);
            if (value == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found object without " + property + ": " + object);
                }
                continue;
            }
            List<NodeRef> objectsGroup = result.get(value);
            if (objectsGroup == null) {
                objectsGroup = new ArrayList<>();
                result.put(value, objectsGroup);
            }

            objectsGroup.add(object);
        }
        return result;
    }

    private class ScopedObjectsPrecedenceComparator implements Comparator<NodeRef> {

        private final Map<QName, Integer> typeDepths;
        private final int infiniteTypeDepth;

        ScopedObjectsPrecedenceComparator(List<QName> types) {
            this.typeDepths = calculateDepthMap(types);
            infiniteTypeDepth = types.size() + 1;
        }

        @Override
        public int compare(NodeRef object1, NodeRef object2) {
            // first - by type depth
            int typeDepth1 = getTypeDepth((QName) nodeService.getProperty(object1, CardletModel.PROP_ALLOWED_TYPE));
            int typeDepth2 = getTypeDepth((QName) nodeService.getProperty(object2, CardletModel.PROP_ALLOWED_TYPE));
            if (typeDepth1 > typeDepth2) return -1;
            if (typeDepth1 < typeDepth2) return +1;

            // next - by group
            boolean groups1empty = groupsAreEmpty(nodeService.getProperty(object1, CardletModel.PROP_ALLOWED_AUTHORITIES));
            boolean groups2empty = groupsAreEmpty(nodeService.getProperty(object2, CardletModel.PROP_ALLOWED_AUTHORITIES));
            if (!groups1empty && groups2empty) return -1;
            if (groups1empty && !groups2empty) return +1;

            // finally - by condition
            boolean condition1empty = stringIsEmpty((String) nodeService.getProperty(object1, CardletModel.PROP_CONDITION));
            boolean condition2empty = stringIsEmpty((String) nodeService.getProperty(object2, CardletModel.PROP_CONDITION));
            if (!condition1empty && condition2empty) return -1;
            if (condition1empty && !condition2empty) return +1;

            return 0;
        }

        private boolean stringIsEmpty(String condition1) {
            return condition1 == null || condition1.isEmpty();
        }

        private int getTypeDepth(QName type) {
            return type != null ? typeDepths.get(type) : infiniteTypeDepth;
        }

        private boolean groupsAreEmpty(Object groups1) {
            return groups1 == null || groups1 instanceof Collection && ((Collection<?>) groups1).size() == 0;
        }

        private Map<QName, Integer> calculateDepthMap(List<QName> types) {
            final Map<QName, Integer> typeDepths = new HashMap<>();
            int typeDepth = 0;
            for (QName type : types) {
                typeDepths.put(type, typeDepth);
                typeDepth++;
            }
            return typeDepths;
        }
    }

    private class PropertyValueComparator implements Comparator<NodeRef> {

        private final Map<NodeRef, Serializable> cachedValues = new HashMap<>();
        private final NodeService nodeService;
        private final QName propertyName;
        private final Comparator<Serializable> valueComparator;

        PropertyValueComparator(NodeService nodeService, QName propertyName) {
            this.nodeService = nodeService;
            this.propertyName = propertyName;
            this.valueComparator = null;
        }

        @SuppressWarnings("unused")
        public PropertyValueComparator(NodeService nodeService, QName propertyName, Comparator<Serializable> valueComparator) {
            this.nodeService = nodeService;
            this.propertyName = propertyName;
            this.valueComparator = valueComparator;
        }

        @Override
        public int compare(NodeRef node1, NodeRef node2) {
            Serializable value1 = getValue(node1);
            Serializable value2 = getValue(node2);
            return compare(value1, value2);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private int compare(Serializable value1, Serializable value2) {
            if (valueComparator != null)
                return valueComparator.compare(value1, value2);

            if (value1 == null && value2 != null) {
                return -1;
            }
            if (value1 == null) {
                return 0;
            }
            if (value2 == null) {
                return 1;
            }
            if (value1 instanceof Comparable) {
                return ((Comparable) value1).compareTo(value2);
            }

            throw new IllegalArgumentException("Neither value1, nor value2 are comparable");
        }

        private Serializable getValue(NodeRef nodeRef) {
            if (cachedValues.containsKey(nodeRef)) {
                return cachedValues.get(nodeRef);
            } else {
                Serializable value = nodeService.getProperty(nodeRef, propertyName);
                cachedValues.put(nodeRef, value);
                return value;
            }
        }
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    public void setScriptEngine(String scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    public void setRepositoryHelper(Repository repoHelper) {
        this.repositoryHelper = repoHelper;
    }

    public void setCardletsRegistry(CardletsRegistry cardletsRegistry) {
        this.cardletsRegistry = cardletsRegistry;
    }
}
