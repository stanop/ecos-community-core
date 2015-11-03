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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.search.CriteriaSearchResults;
import ru.citeck.ecos.search.CriteriaSearchService;
import ru.citeck.ecos.search.FieldType;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchPredicate;
import ru.citeck.ecos.utils.DictionaryUtils;
import ru.citeck.ecos.utils.LazyNodeRef;
import ru.citeck.ecos.utils.RepoUtils;

/**
 * Case Element DAO, that allows access to case elements configs of case.
 * Every case element config should be bound to some class (type or aspect).
 * Fetching of case element configs is done based on types and aspects, that case has.
 * Adding case element means adding aspect or specializing type, if it is possible.
 * Removing case element means removing corresponding aspect.
 * 
 * If several configs are bound to the same class or classes, that are bound with parent/child relationships, 
 * then adding one config can lead to adding other ones, bound to the same class or parent class.
 * The same stands for removing configs.
 * It should be considered, when developing case element configs.
 * 
 * @author Sergey Tiunov
 */
public class CaseElementConfigsDAOImpl extends AbstractCaseElementDAO {
    
    private CriteriaSearchService criteriaService;
    private NamespaceService namespaceService;
    private LazyNodeRef caseElementConfigRoot;

    public void setCriteriaSearchService(CriteriaSearchService criteriaService) {
        this.criteriaService = criteriaService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setCaseElementConfigRoot(LazyNodeRef caseElementConfigRoot) {
        this.caseElementConfigRoot = caseElementConfigRoot;
    }

    private List<NodeRef> getAllElementConfigs() {
        NodeRef root = caseElementConfigRoot.getNodeRef();
        Collection<QName> elementConfigTypes = dictionaryService.getSubTypes(ICaseModel.TYPE_ELEMENT_CONFIG, true);
        List<ChildAssociationRef> configAssocs = nodeService.getChildAssocs(root, new HashSet<>(elementConfigTypes));
        return RepoUtils.getChildNodeRefs(configAssocs);
    }
    
    @Override
    public QName getElementConfigType() {
        return ICaseModel.TYPE_CLASS_CONFIG;
    }

    @Override
    public List<NodeRef> get(NodeRef caseNode, NodeRef config) {
        if (!nodeService.exists(caseNode)) {
            throw new IllegalArgumentException("Cannot get nodeRefs without case node");
        }
        
        // get all case classes:
        List<QName> classNames = DictionaryUtils.getAllNodeClassNames(caseNode, nodeService, dictionaryService);
        
        List<NodeRef> allElements = getAllElementConfigs();

        List<NodeRef> matchingElements = new LinkedList<>();
        for(NodeRef element : allElements) {
            QName className = RepoUtils.getProperty(element, ICaseModel.PROP_CASE_CLASS, nodeService);
            if(classNames.contains(className)) {
                matchingElements.add(element);
            }
        }
        
        return matchingElements;
    }

    @Override
    protected List<NodeRef> getCasesImpl(NodeRef element, NodeRef config)
            throws AlfrescoRuntimeException, IllegalArgumentException {
        
        // get class, required by new case element config:
        ClassDefinition classToSearch = needCaseClass(element);
        boolean isAspect = classToSearch.isAspect();
        
        // add all cases, with this type/aspect
        SearchCriteria criteria = new SearchCriteria(namespaceService)
                .addCriteriaTriplet(FieldType.ASPECT, SearchPredicate.ASPECT_EQUALS, ICaseModel.ASPECT_CASE)
                .addCriteriaTriplet(
                        isAspect ? FieldType.ASPECT : FieldType.TYPE, 
                        isAspect ? SearchPredicate.ASPECT_EQUALS : SearchPredicate.TYPE_EQUALS, 
                        classToSearch.getName());
        
        CriteriaSearchResults searchResults = criteriaService.query(criteria, SearchService.LANGUAGE_LUCENE);
        return searchResults.getResults();
    }

    @Override
    public void add(NodeRef nodeRef, NodeRef caseNode, NodeRef config) {
        checkForAddRemove(caseNode, nodeRef);
        
        // get class, required by new case element config:
        ClassDefinition classToAdd = needCaseClass(nodeRef);
        
        if(classToAdd.isAspect()) {
            if(!nodeService.hasAspect(caseNode, classToAdd.getName())) {
                nodeService.addAspect(caseNode, classToAdd.getName(), null);
            }
        } else {
            QName caseType = nodeService.getType(caseNode);
            
            if(!dictionaryService.isSubClass(classToAdd.getName(), caseType)) {
                throw new IllegalArgumentException("Can not specialize case type from " + caseType + " to " + classToAdd.getName());
            }
            
            nodeService.setType(caseNode, classToAdd.getName());
        }
    }

    @Override
    public void remove(NodeRef nodeRef, NodeRef caseNode, NodeRef config) {
        checkForAddRemove(caseNode, nodeRef);
        
        // get class, required by new case element config:
        ClassDefinition classToRemove = needCaseClass(nodeRef);
        
        if(classToRemove.isAspect()) {
            if(nodeService.hasAspect(caseNode, classToRemove.getName())) {
                nodeService.removeAspect(caseNode, classToRemove.getName());
            }
        } else {
            throw new IllegalArgumentException("Can not remove case element config, bound to type");
        }
    }
    
    private void checkForAddRemove(NodeRef caseNode, NodeRef elementNode) {
        if (!nodeService.exists(caseNode)) {
            throw new IllegalArgumentException("Case node does not exist, nodeRef: " + caseNode);
        }
        if (!nodeService.exists(elementNode)) {
            throw new IllegalArgumentException("Element node does not exist, nodeRef: " + elementNode);
        }
        // check, that it is element config:
        QName elementType = nodeService.getType(elementNode);
        if(!dictionaryService.isSubClass(elementType, ICaseModel.TYPE_ELEMENT_CONFIG)) {
            throw new IllegalArgumentException("Specified element is not case element config, nodeRef: " + elementNode + ", type: " + elementType);
        }
    }

    private ClassDefinition needCaseClass(NodeRef elementConfig) {
        QName requiredClassName = (QName) nodeService.getProperty(elementConfig, ICaseModel.PROP_CASE_CLASS);
        if(requiredClassName == null) {
            throw new IllegalArgumentException("Case class name is not specified in case element config, nodeRef: " + elementConfig);
        }
        ClassDefinition requiredClass = dictionaryService.getClass(requiredClassName);
        if(requiredClass == null) {
            throw new IllegalArgumentException("Class definition is not found, class name: " + requiredClass);
        }
        return requiredClass;
    }

    @Override
    public Set<BehaviourDefinition<?>> intializeBehaviours(NodeRef config) {
        // TODO implement OnAddAspect, OnRemoveAspect, OnSetNodeType
        return Collections.emptySet();
    }

}
