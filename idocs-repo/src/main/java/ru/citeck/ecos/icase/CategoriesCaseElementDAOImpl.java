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
package ru.citeck.ecos.icase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;

import ru.citeck.ecos.behavior.ParameterizedJavaBehaviour;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.search.CriteriaSearchResults;
import ru.citeck.ecos.search.CriteriaSearchService;
import ru.citeck.ecos.search.FieldType;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaFactory;
import ru.citeck.ecos.search.SearchPredicate;
import ru.citeck.ecos.utils.RepoUtils;

public class CategoriesCaseElementDAOImpl extends AbstractCaseElementDAO {
    
    private CriteriaSearchService searchService;
    private String language;
    private SearchCriteriaFactory criteriaFactory;

    public void setSearchService(CriteriaSearchService searchService) {
        this.searchService = searchService;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCriteriaFactory(SearchCriteriaFactory criteriaFactory) {
        this.criteriaFactory = criteriaFactory;
    }

    @Override
    public QName getElementConfigType() {
        return ICaseModel.TYPE_CATEGORY_CONFIG;
    }

    @Override
    public List<NodeRef> get(NodeRef caseNode, NodeRef config)
            throws AlfrescoRuntimeException, IllegalArgumentException {
        QName categoryProperty = needCategoryProperty(config);
        return getCategories(caseNode, categoryProperty);
    }

    @Override
    protected List<NodeRef> getCasesImpl(NodeRef element, NodeRef config)
            throws AlfrescoRuntimeException, IllegalArgumentException {
        QName categoryProperty = needCategoryProperty(config);
        SearchCriteria criteria = criteriaFactory.createSearchCriteria()
                .addCriteriaTriplet(FieldType.ASPECT, SearchPredicate.ASPECT_EQUALS, ICaseModel.ASPECT_CASE)
                .addCriteriaTriplet(categoryProperty, SearchPredicate.NODEREF_CONTAINS, element.toString());
        CriteriaSearchResults results = searchService.query(criteria, language);
        return results.getResults();
    }

    @Override
    public void add(NodeRef nodeRef, NodeRef caseNode, NodeRef config)
            throws AlfrescoRuntimeException, IllegalArgumentException {
        if(nodeRef == null) return;
        
        QName elementType = super.needElementType(config);
        if(!RepoUtils.isSubType(nodeRef, elementType, nodeService, dictionaryService)) {
            throw new IllegalArgumentException("Specified element is not of required type. element=" + nodeRef + ", type=" + elementType);
        }
        
        QName categoryProperty = needCategoryProperty(config);
        List<NodeRef> categories = getCategories(caseNode, categoryProperty);
        if(categories.contains(nodeRef)) return;
        
        ArrayList<NodeRef> newCategories = new ArrayList<>(categories.size() + 1);
        newCategories.addAll(categories);
        newCategories.add(nodeRef);
        nodeService.setProperty(caseNode, categoryProperty, newCategories);
    }
    
    @Override
    public void addAll(Collection<NodeRef> newElements, NodeRef caseNode, NodeRef config) {
        QName categoryProperty = needCategoryProperty(config);
        List<NodeRef> oldElements = getCategories(caseNode, categoryProperty);
        @SuppressWarnings("unchecked")
        Collection<NodeRef> elementsToAdd = CollectionUtils.subtract(newElements, oldElements);
        if(elementsToAdd.isEmpty()) return;
        ArrayList<NodeRef> newCategories = new ArrayList<>(oldElements.size() + elementsToAdd.size());
        newCategories.addAll(oldElements);
        newCategories.addAll(elementsToAdd);
        nodeService.setProperty(caseNode, categoryProperty, newCategories);
    }

    @Override
    public void remove(NodeRef nodeRef, NodeRef caseNode, NodeRef config)
            throws AlfrescoRuntimeException, IllegalArgumentException {
        if(nodeRef == null) return;
        QName categoryProperty = needCategoryProperty(config);
        List<NodeRef> categories = getCategories(caseNode, categoryProperty);
        if(!categories.contains(nodeRef)) return;
        
        ArrayList<NodeRef> newCategories = new ArrayList<>(categories.size() - 1);
        for(NodeRef category : categories) {
            if(!nodeRef.equals(category)) {
                newCategories.add(category);
            }
        }
        nodeService.setProperty(caseNode, categoryProperty, newCategories);
    }

    @Override
    public Set<BehaviourDefinition<?>> intializeBehaviours(NodeRef config) {
        BehaviourDefinition<?> behaviour = policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ICaseModel.ASPECT_CASE, 
                ParameterizedJavaBehaviour.newInstance(this, "onUpdateProperties", config));
        return Collections.<BehaviourDefinition<?>>singleton(behaviour);
    }
    
    public void onUpdateProperties(NodeRef caseNode, Map<QName, Serializable> before, Map<QName, Serializable> after, NodeRef config) {
        QName categoryProperty = needCategoryProperty(config);
        if(!before.containsKey(categoryProperty) || !after.containsKey(categoryProperty)) {
            return;
        }
        
        List<NodeRef> oldCategories = RepoUtils.anyToNodeRefs(before.get(categoryProperty));
        List<NodeRef> newCategories = RepoUtils.anyToNodeRefs(after.get(categoryProperty));
        
        @SuppressWarnings("unchecked")
        Collection<NodeRef> addedCategories = CollectionUtils.subtract(newCategories, oldCategories),
                          removedCategories = CollectionUtils.subtract(oldCategories, newCategories);
        
        for(NodeRef element : addedCategories) {
            caseElementService.invokeOnCaseElementAdd(caseNode, element, config);
        }
        
        for(NodeRef element : removedCategories) {
            caseElementService.invokeOnCaseElementRemove(caseNode, element, config);
        }
    }

    private QName needCategoryProperty(NodeRef config) {
        QName categoryProperty = getCategoryProperty(config);
        if(categoryProperty == null) 
            throw new IllegalArgumentException("Category property name is not specified in element config: " + config);
        return categoryProperty;
    }

    private QName getCategoryProperty(NodeRef config) {
        return RepoUtils.getProperty(config, ICaseModel.PROP_CATEGORY_PROPERTY, nodeService);
    }

    private List<NodeRef> getCategories(NodeRef caseNode, QName categoryProperty) {
        Serializable categories = nodeService.getProperty(caseNode, categoryProperty);
        return RepoUtils.anyToNodeRefs(categories);
    }
}
