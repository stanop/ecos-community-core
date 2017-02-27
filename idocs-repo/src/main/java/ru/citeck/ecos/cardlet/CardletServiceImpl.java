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
import ru.citeck.ecos.model.CardletModel;
import ru.citeck.ecos.utils.DictionaryUtils;

import java.io.Serializable;
import java.util.*;

/*default*/ class CardletServiceImpl implements CardletService {
    private static final Log logger = LogFactory.getLog(CardletServiceImpl.class);

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private AuthorityService authorityService;
    private SearchService searchService;
    private ScriptService scriptService;
    private Repository repositoryHelper;

    private String scriptEngine;

    @Override
    public List<NodeRef> queryCardlets(NodeRef nodeRef) {
        return queryCardlets(nodeRef, DEFAULT_MODE);
    }

    @Override
    public List<NodeRef> queryCardlets(NodeRef nodeRef, String cardMode) {
        List<QName> types = getAllNodeTypes(nodeRef);
        Collection<String> authorities = getAllUserAuthorities();
        Comparator<NodeRef> precedenceComparator = new ScopedObjectsPrecedenceComparator(types);
        Map<String, Object> conditionModel = buildConditionModel(nodeRef);

        if (cardMode == null) cardMode = DEFAULT_MODE;

        // query
        List<NodeRef> cardlets = queryCardlets(cardMode, types, authorities);

        // group by regionId
        Map<Object, List<NodeRef>> cardletsByRegion = groupBy(cardlets, CardletModel.PROP_REGION_ID);

        // get resulting cardlets for each region
        List<NodeRef> resultCardlets = new LinkedList<>();
        for (List<NodeRef> regionCardlets : cardletsByRegion.values()) {
            NodeRef mostSuitableCardlet = findMostSuitable(regionCardlets, precedenceComparator,
                    conditionModel);
            if (mostSuitableCardlet != null) {
                resultCardlets.add(mostSuitableCardlet);
            }
        }
        Collections.sort(resultCardlets, new PropertyValueComparator(nodeService, CardletModel.PROP_REGION_POSITION));
        return resultCardlets;
    }

    @Override
    public List<NodeRef> queryCardModes(NodeRef nodeRef) {
        List<QName> types = getAllNodeTypes(nodeRef);
        Collection<String> authorities = getAllUserAuthorities();
        Comparator<NodeRef> precedenceComparator = new ScopedObjectsPrecedenceComparator(types);
        Map<String, Object> conditionModel = buildConditionModel(nodeRef);

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
        Collections.sort(resultCardModes, new PropertyValueComparator(nodeService, CardletModel.PROP_CARD_MODE_ORDER));
        return resultCardModes;
    }

    private List<NodeRef> queryCardlets(String cardMode, List<QName> types, Collection<String> authorities) {

        String modeClause;
        if (ALL_MODES.equals(cardMode)) {
            modeClause = "TRUE";
        } else {
            Collection<String> modes = new ArrayList<>(2);
            modes.add(cardMode);
            modes.add(CardletService.ALL_MODES);
            modeClause = disjunction(CardletModel.PROP_CARD_MODE, modes, false, true);
        }

        authorities.add("");
        String typeClause = "TYPE:\"" + CardletModel.TYPE_CARDLET + "\"";
        String documentClause = disjunction(CardletModel.PROP_ALLOWED_TYPE, types, false, true);
        String authorityClause = disjunction(CardletModel.PROP_ALLOWED_AUTHORITIES, authorities, false, true);
        String query = typeClause + " AND " + modeClause + " AND " + documentClause + " AND " + authorityClause;
        if (logger.isDebugEnabled()) {
            logger.debug("Quering cardlets: " + query);
        }

        List<NodeRef> cardlets;
        cardlets = getCardItemsRefs(query);
        if (logger.isDebugEnabled()) {
            logger.debug("Found cardlets: " + cardlets.size());
        }

        return filterCardletsByAllowedType(cardlets, types);
    }

    /**
     * This method fix wrong search results by property cardlet:allowedType in Lucene and Solr searches
     */
    private List<NodeRef> filterCardletsByAllowedType(List<NodeRef> cardlets, List<QName> types) {
        List<NodeRef> filteredCardlets = new ArrayList<>(cardlets.size());
        for (NodeRef cardletRef : cardlets) {
            QName allowedType = (QName) nodeService.getProperty(cardletRef, CardletModel.PROP_ALLOWED_TYPE);
            if (types.contains(allowedType)) {
                filteredCardlets.add(cardletRef);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Search returns wrong cardlet with allowedType = " + allowedType.toString());
                }
            }
        }
        return filteredCardlets;
    }

    private List<NodeRef> queryCardModes(List<QName> types, Collection<String> authorities) {
        String typeClause = "TYPE:\"" + CardletModel.TYPE_CARD_MODE + "\"";
        String documentClause = disjunction(CardletModel.PROP_ALLOWED_TYPE, types, false, true);
        String authorityClause = disjunction(CardletModel.PROP_ALLOWED_AUTHORITIES, authorities, true, true);
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
        return DictionaryUtils.getAllNodeClassNames(nodeRef, nodeService, dictionaryService);
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
        if (condition == null || condition.isEmpty()) {
            return true;
        }
        Object conditionResult = scriptService.executeScriptString(scriptEngine, condition, scriptModel);
        if (conditionResult instanceof Boolean) {
            return (Boolean) conditionResult;
        } else {
            return false;
        }
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

}
