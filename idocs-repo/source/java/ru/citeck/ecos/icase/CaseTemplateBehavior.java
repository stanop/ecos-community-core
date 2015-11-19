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

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.service.cmr.repository.ScriptService;

import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.search.CriteriaSearchService;
import ru.citeck.ecos.search.SearchCriteriaFactory;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.FieldType;
import ru.citeck.ecos.search.SearchPredicate;

public class CaseTemplateBehavior implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnAddAspectPolicy {
	private static final Log logger = LogFactory.getLog(CaseTemplateBehavior.class);

	protected PolicyComponent policyComponent;
	protected NodeService nodeService;
	//protected SearchService searchService;
	protected NamespaceService namespaceService;
	protected CaseElementServiceImpl caseElementService;
	protected int order = 45;
	
	protected Map<QName, NodeRef> caseTemplates;
    private ScriptService scriptService;
    private String scriptEngine;
	
    private CriteriaSearchService searchService;
    private SearchCriteriaFactory criteriaFactory;
    private String language;
	
	public void resetCaseTemplates() {
		caseTemplates = new HashMap<QName, NodeRef>();
	}

	public void init() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ICaseModel.ASPECT_CASE, 
				new OrderedBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT, order));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, ICaseModel.ASPECT_CASE, 
		        new OrderedBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT, order));
		resetCaseTemplates();
	}

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        NodeRef caseNode = childAssocRef.getChildRef();
        copyFromTemplate(caseNode);
    }

    @Override
    public void onAddAspect(NodeRef caseNode, QName aspectTypeQName) {
        if(!nodeService.exists(caseNode) ||
                !ICaseModel.ASPECT_CASE.equals(aspectTypeQName)) {
            return;
        }
        copyFromTemplate(caseNode);
    }

    private void copyFromTemplate(NodeRef caseNode) {
        if (!nodeService.exists(caseNode)
                || nodeService.hasAspect(caseNode,
                        ContentModel.ASPECT_COPIEDFROM)
                || nodeService.hasAspect(caseNode,
                        ICaseModel.ASPECT_COPIED_FROM_TEMPLATE)
                || nodeService.hasAspect(caseNode,
                        ICaseModel.ASPECT_CASE_TEMPLATE)) {
            return;
        }

        if (logger.isDebugEnabled())
            logger.debug("Creating node with icase:case aspect. nodeRef="
                    + caseNode);

        List<NodeRef> templates = getCaseTemplateByType(caseNode, nodeService.getType(caseNode));
        if (templates.isEmpty()) {
            return;
        }
		for(NodeRef template : templates)
		{
		    caseElementService.copyTemplateToCase(template, caseNode);
		    
			if (logger.isDebugEnabled())
				logger.debug("Case elements are successfully copied from template. nodeRef="
						+ caseNode + "; template=" + template);
		}
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

	protected List<NodeRef> getCaseTemplateByType(NodeRef caseNode, QName type) {
        SearchCriteria searchCriteria = criteriaFactory.createSearchCriteria()
                .addCriteriaTriplet(FieldType.TYPE, SearchPredicate.TYPE_EQUALS, ICaseModel.TYPE_CASE_TEMPLATE)
                .addCriteriaTriplet(ICaseModel.PROP_CASE_TYPE, SearchPredicate.STRING_EQUALS, type.toString());
        List<NodeRef> nodeRefs = searchService.query(searchCriteria, language).getResults();
		logger.debug("searchCriteria for getting case template "+searchCriteria);
		List<NodeRef> resultTemplates = new ArrayList<NodeRef>();

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("caseNode", caseNode);
		for(NodeRef template : nodeRefs)
		{
			String condition = (String)nodeService.getProperty(template, ICaseModel.PROP_CONDITION);
			if(condition!=null)
			{
				Object result = scriptService.executeScriptString(scriptEngine, condition, model);
				logger.debug("condition "+condition);
				logger.debug("result "+result);
				if(Boolean.TRUE.equals(result))
					resultTemplates.add(template);
			}
			else
			{
				resultTemplates.add(template);
			}
		}
		
		return resultTemplates;
	}

}
