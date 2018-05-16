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
package ru.citeck.ecos.icase.element;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.icase.element.config.KeyPropConfig;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.search.CriteriaSearchService;
import ru.citeck.ecos.search.FieldType;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaFactory;
import ru.citeck.ecos.search.SearchPredicate;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class KeyPropertyCaseElementDAOImpl extends AbstractCaseElementDAO<KeyPropConfig> {

    @Override
    public QName getElementConfigType() {
        return ICaseModel.TYPE_KEY_PROP_CONFIG;
    }

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
    public List<NodeRef> get(NodeRef caseNode, KeyPropConfig config)
            throws AlfrescoRuntimeException, IllegalArgumentException {

        if (!nodeService.exists(caseNode)) {
            throw new IllegalArgumentException("Case node does not exist: " + caseNode);
        }
        if (config == null) {
            throw new IllegalArgumentException("Config node does not exist");
        }

        try {
            QName caseKey = config.getCaseKey();
            Serializable caseKeyValue = nodeService.getProperty(caseNode, caseKey);
            if (caseKeyValue == null) {
                return Collections.emptyList();
            }
            QName elementKey = config.getElementKey();
            QName elementType = config.getElementType();
            SearchCriteria searchCriteria = criteriaFactory.createSearchCriteria()
                    .addCriteriaTriplet(FieldType.TYPE, SearchPredicate.TYPE_EQUALS, elementType)
                    .addCriteriaTriplet(elementKey, getPredicate(caseKeyValue), caseKeyValue.toString());
            return searchService.query(searchCriteria, language).getResults();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Throwable e) {
            throw new AlfrescoRuntimeException("Can not get elements from case. caseNode=" + caseNode + "; config=" + config, e);
        }
    }

    @Override
    protected List<NodeRef> getCasesImpl(NodeRef element, KeyPropConfig config)
            throws AlfrescoRuntimeException, IllegalArgumentException {
        try {
            QName caseKey = config.getCaseKey();
            QName elementKey = config.getElementKey();
            Serializable elementKeyValue = nodeService.getProperty(element, elementKey);
            if (elementKeyValue == null) {
                return Collections.emptyList();
            }
            SearchCriteria searchCriteria = criteriaFactory.createSearchCriteria()
                    .addCriteriaTriplet(FieldType.ASPECT, SearchPredicate.ASPECT_EQUALS, ICaseModel.ASPECT_CASE)
                    .addCriteriaTriplet(caseKey, getPredicate(elementKeyValue), elementKeyValue.toString());
            return searchService.query(searchCriteria, language).getResults();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Throwable e) {
            throw new AlfrescoRuntimeException("Can not get elements from case. caseNode=" + element + "; config=" + config, e);
        }
    }

    @Override
    public void add(NodeRef nodeRef, NodeRef caseNode, KeyPropConfig config)
            throws AlfrescoRuntimeException, IllegalArgumentException {

        if (!nodeService.exists(nodeRef) || !nodeService.exists(caseNode) || config == null)
            throw new IllegalArgumentException("Can not add property to the case object " +
                                               "without caseNode or nodeRef or config");

        try {
            QName caseKey = config.getCaseKey();
            QName elementKey = config.getElementKey();
            Serializable value = nodeService.getProperty(nodeRef, elementKey);
            nodeService.setProperty(caseNode, caseKey, value);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Throwable e) {
            throw new AlfrescoRuntimeException("Can not add node reference to case. nodeRef=" + nodeRef +
                                               "; caseNode=" + caseNode + "; config=" + config, e);
        }
    }

    @Override
    public void remove(NodeRef nodeRef, NodeRef caseNode, KeyPropConfig config)
            throws AlfrescoRuntimeException, IllegalArgumentException {

        if (!nodeService.exists(nodeRef) || !nodeService.exists(caseNode) || config == null)
            throw new IllegalArgumentException("Can not remove property from the case " +
                                               "object without caseNode or nodeRef or config");

        QName caseKey = config.getCaseKey();
        try {
            nodeService.removeProperty(caseNode, caseKey);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Throwable e) {
            throw new AlfrescoRuntimeException("Can not remove node reference to case. nodeRef=" + nodeRef + "; " +
                                               "caseNode=" + caseNode + "; config=" + config, e);
        }
    }

    @Override
    public KeyPropConfig createConfig(NodeRef configRef) {
        KeyPropConfig config = new KeyPropConfig(configRef);
        config.setCaseKey((QName) nodeService.getProperty(configRef, ICaseModel.PROP_CASE_KEY));
        config.setElementKey((QName) nodeService.getProperty(configRef, ICaseModel.PROP_ELEMENT_KEY));
        return config;
    }

    private SearchPredicate getPredicate(Serializable value) {
        if (value instanceof String) {
            return SearchPredicate.STRING_EQUALS;
        } else if (value instanceof Long || value instanceof Float) {
            return SearchPredicate.NUMBER_EQUALS;
        } else if (value instanceof Date) {
            return SearchPredicate.DATE_EQUALS;
        } else if (value instanceof Boolean) {
            return (Boolean) value ? SearchPredicate.BOOLEAN_TRUE : SearchPredicate.BOOLEAN_FALSE;
        } else if (value instanceof List) {
            return SearchPredicate.LIST_EQUALS;
        } else if (value instanceof NodeRef) {
            return SearchPredicate.ASSOC_CONTAINS;
        }
        throw new IllegalArgumentException("Unknown case key value type. Unable get predicate string");
    }

    @Override
    public Set<BehaviourDefinition<?>> intializeBehaviours(KeyPropConfig config) {
        // TODO support behaviours in key-property configurations
        return Collections.emptySet();
    }

}
