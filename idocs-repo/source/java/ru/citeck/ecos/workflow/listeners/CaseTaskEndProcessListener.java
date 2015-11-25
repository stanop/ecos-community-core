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
package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import ru.citeck.ecos.activity.CaseActivityService;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.search.*;
import ru.citeck.ecos.service.CiteckServices;

import java.util.List;

/**
 * @author Maxim Strizhov
 */
public class CaseTaskEndProcessListener extends AbstractExecutionListener {
    private CaseActivityService caseActivityService;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private CriteriaSearchService criteriaSearchService;

    @Override
    protected void notifyImpl(final DelegateExecution delegateExecution) throws Exception {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                NodeRef docRef = ListenerUtils.getDocument(delegateExecution, nodeService);
                if (docRef == null) {
                    return null;
                }
                String processType = "activiti$" + Context.getExecutionContext().getProcessDefinition().getKey();
                String processId = delegateExecution.getProcessInstanceId();

                SearchCriteria searchCriteria = new SearchCriteria(namespaceService)
                        .addCriteriaTriplet(FieldType.TYPE, SearchPredicate.TYPE_EQUALS, ICaseTaskModel.TYPE_TASK)
                        .addCriteriaTriplet(ICaseTaskModel.PROP_WORKFLOW_DEFINITION_NAME, SearchPredicate.STRING_EQUALS, processType);
                CriteriaSearchResults searchResults = criteriaSearchService.query(searchCriteria, SearchService.LANGUAGE_LUCENE);
                List<NodeRef> results = searchResults.getResults();
                for (NodeRef result : results) {
                    String workflowId = (String) nodeService.getProperty(result, ICaseTaskModel.PROP_WORKFLOW_INSTANCE_ID);
                    if (workflowId != null && workflowId.equals("activiti$"+processId)) {
                        caseActivityService.stopActivity(result);
                    }
                }
                return null;
            }
        });
    }

    @Override
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.caseActivityService = (CaseActivityService) serviceRegistry.getService(CiteckServices.CASE_ACTIVITY_SERVICE);
        this.criteriaSearchService = (CriteriaSearchService) serviceRegistry.getService(CiteckServices.CRITERIA_SEARCH_SERVICE);
    }
}
