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

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.model.CiteckWorkflowModel;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.AlfActivityUtils;
import ru.citeck.ecos.utils.RepoUtils;

@Slf4j
public class CaseTaskEndProcessListener extends AbstractExecutionListener {

    private CaseActivityService caseActivityService;
    private AlfActivityUtils alfActivityUtils;
    private NodeService nodeService;
    private WorkflowDocumentResolverRegistry documentResolverRegistry;

    @Override
    protected void notifyImpl(final DelegateExecution delegateExecution) {
        AuthenticationUtil.runAsSystem(() -> {
            CaseTaskEndProcessListener.this.doWork(delegateExecution);
            return null;
        });
    }

    private void doWork(DelegateExecution delegateExecution) {
        if (documentResolverRegistry.getResolver(delegateExecution).getDocument(delegateExecution) == null) {
            return;
        }
        stopActivity(delegateExecution);
    }

    private void stopActivity(DelegateExecution delegateExecution) {
        NodeRef bpmPackage = ListenerUtils.getWorkflowPackage(delegateExecution);
        nodeService.setProperty(bpmPackage, CiteckWorkflowModel.PROP_IS_WORKFLOW_ACTIVE, false);

        NodeRef taskActivityNodeRef = RepoUtils.getFirstSourceAssoc(bpmPackage,
            ICaseTaskModel.ASSOC_WORKFLOW_PACKAGE, nodeService);
        if (taskActivityNodeRef != null) {
            ActionConditionUtils.getProcessVariables().putAll(delegateExecution.getVariables());

            ActivityRef taskActivityRef = alfActivityUtils.composeActivityRef(taskActivityNodeRef);
            caseActivityService.stopActivity(taskActivityRef);
        }
    }

    @Override
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.caseActivityService = (CaseActivityService) serviceRegistry.getService(CiteckServices.CASE_ACTIVITY_SERVICE);
        this.alfActivityUtils = (AlfActivityUtils) serviceRegistry.getService(CiteckServices.ALF_ACTIVITY_UTILS);
        documentResolverRegistry = getBean(WorkflowDocumentResolverRegistry.BEAN_NAME, WorkflowDocumentResolverRegistry.class);
    }
}
