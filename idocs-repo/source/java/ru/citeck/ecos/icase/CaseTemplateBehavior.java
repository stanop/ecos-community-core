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

import java.util.*;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.service.cmr.repository.ScriptService;

import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.search.*;

public class CaseTemplateBehavior implements NodeServicePolicies.OnCreateNodePolicy {
    private static final String KEY_FILLED_CASE_NODES = "filled-case-nodes";

    private static final Log logger = LogFactory.getLog(CaseTemplateBehavior.class);

    protected PolicyComponent policyComponent;
    protected NodeService nodeService;
    protected NamespaceService namespaceService;
    protected CaseElementServiceImpl caseElementService;

    private RepositoryState repositoryState;

    protected int order = 40;

    protected Map<QName, NodeRef> caseTemplates;
    private ScriptService scriptService;
    private String scriptEngine;

    private CriteriaSearchService searchService;
    private SearchCriteriaFactory criteriaFactory;
    private String language;

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ContentModel.TYPE_CMOBJECT,
                new OrderedBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT, order));
        resetCaseTemplates();
    }

    public void resetCaseTemplates() {
        caseTemplates = new HashMap<>();
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        if (!repositoryState.isBootstrapping()) {
            NodeRef caseNode = childAssocRef.getChildRef();
            copyFromTemplate(caseNode);
        }
    }

    private void copyFromTemplate(NodeRef caseNode) {
        if (!isAllowedCaseNode(caseNode) || !getFilledCaseNodes().add(caseNode)) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Applying template to node. nodeRef=" + caseNode);
        }

        Set<NodeRef> templates = getCaseTemplates(caseNode);

        for (NodeRef template : templates) {

            caseElementService.copyTemplateToCase(template, caseNode);

            if (logger.isDebugEnabled()) {
                logger.debug("Case elements are successfully copied from template. nodeRef="
                                                        + caseNode + "; template=" + template);
            }
        }
    }

    private Set<NodeRef> getCaseTemplates(NodeRef caseNode) {

        Set<NodeRef> resultTemplates = new HashSet<>();

        NodeRef caseType = (NodeRef)nodeService.getProperty(caseNode, ClassificationModel.PROP_DOCUMENT_TYPE);
        NodeRef caseKind = (NodeRef)nodeService.getProperty(caseNode, ClassificationModel.PROP_DOCUMENT_KIND);

        List<NodeRef> templates = getCaseTemplatesByEcosTypeKind(caseType, caseKind);
        if (templates.size() == 0) {
            templates = getCaseTemplatesByType(caseNode);
        }

        for (NodeRef ref : templates) {
            if (isApplicableTemplate(caseNode, ref)) {
                resultTemplates.add(ref);
            }
        }

        return resultTemplates;
    }

    private boolean isApplicableTemplate(NodeRef node, NodeRef template) {

        String condition = (String)nodeService.getProperty(template, ICaseModel.PROP_CONDITION);

        if (condition == null || condition.trim().isEmpty()) {
            return true;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("caseNode", node);

        Object result = scriptService.executeScriptString(scriptEngine, condition, model);

        logger.debug("condition " + condition);
        logger.debug("result " + result);

        return Boolean.TRUE.equals(result);
    }

    private List<NodeRef> getCaseTemplatesByType(NodeRef caseNode) {

        QName type = nodeService.getType(caseNode);

        SearchCriteria searchCriteria = criteriaFactory.createSearchCriteria()
                .addCriteriaTriplet(FieldType.TYPE, SearchPredicate.TYPE_EQUALS, ICaseModel.TYPE_CASE_TEMPLATE)
                .addCriteriaTriplet(ICaseModel.PROP_CASE_TYPE, SearchPredicate.STRING_EQUALS, type.toString());

        logger.debug("searchCriteria for getting case template " + searchCriteria);

        return searchService.query(searchCriteria, language).getResults();
    }

    private List<NodeRef> getCaseTemplatesByEcosTypeKind(NodeRef type, NodeRef kind) {

        if (type == null) {
            return Collections.emptyList();
        }

        SearchCriteria searchCriteria = criteriaFactory.createSearchCriteria()
                .addCriteriaTriplet(FieldType.TYPE, SearchPredicate.TYPE_EQUALS, ICaseModel.TYPE_CASE_TEMPLATE)
                .addCriteriaTriplet(ICaseModel.PROP_CASE_ECOS_TYPE, SearchPredicate.STRING_EQUALS, type.toString());

        if (kind != null) {
            String kindStr = kind.toString();
            searchCriteria.addCriteriaTriplet(ICaseModel.PROP_CASE_ECOS_KIND, SearchPredicate.STRING_EQUALS, kindStr);
        } else {
            searchCriteria.addCriteriaTriplet(ICaseModel.PROP_CASE_ECOS_KIND, SearchPredicate.NODEREF_EMPTY, "");
        }

        List<NodeRef> results = searchService.query(searchCriteria, language).getResults();
        if (results.size() == 0 && kind != null) {
            results = getCaseTemplatesByEcosTypeKind(type, null);
        }
        return results;
    }

    private boolean isAllowedCaseNode(NodeRef caseNode) {
        return caseNode != null && nodeService.exists(caseNode)
                && !nodeService.hasAspect(caseNode, ContentModel.ASPECT_COPIEDFROM)
                && !nodeService.hasAspect(caseNode, ICaseModel.ASPECT_COPIED_FROM_TEMPLATE)
                && !nodeService.hasAspect(caseNode, ICaseModel.ASPECT_CASE_TEMPLATE);
    }

    private Set<NodeRef> getFilledCaseNodes() {
        Set<NodeRef> filledCaseNodes = AlfrescoTransactionSupport.getResource(KEY_FILLED_CASE_NODES);
        if (filledCaseNodes == null) {
            AlfrescoTransactionSupport.bindResource(KEY_FILLED_CASE_NODES, filledCaseNodes = new HashSet<>());
        }
        return filledCaseNodes;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(CriteriaSearchService searchService) {
        this.searchService = searchService;
    }

    public void setCriteriaFactory(SearchCriteriaFactory criteriaFactory) {
        this.criteriaFactory = criteriaFactory;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setCaseElementService(CaseElementServiceImpl caseElementService) {
        this.caseElementService = caseElementService;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    public void setScriptEngine(String engine) {
        this.scriptEngine = engine;
    }

    public void setRepositoryState(RepositoryState repositoryState) {
        this.repositoryState = repositoryState;
    }
}
