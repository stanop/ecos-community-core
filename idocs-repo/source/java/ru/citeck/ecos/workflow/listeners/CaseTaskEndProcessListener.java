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
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.task.CaseTaskService;
import ru.citeck.ecos.workflow.utils.ActivitiVariableScopeMap;

/**
 * @author Maxim Strizhov
 */
public class CaseTaskEndProcessListener extends AbstractExecutionListener {
    private CaseTaskService caseTaskService;
    private NodeService nodeService;

    @Override
    protected void notifyImpl(final DelegateExecution delegateExecution) throws Exception {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                NodeRef docRef = ListenerUtils.getDocument(delegateExecution, nodeService);
                if (docRef == null) {
                    return null;
                }
                String definitionId = "activiti$" + Context.getExecutionContext().getProcessDefinition().getKey();

                caseTaskService.onTaskCompleted(docRef, definitionId, delegateExecution.getProcessInstanceId());
                return null;
            }
        });
    }

    @Override
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.caseTaskService = (CaseTaskService) serviceRegistry.getService(CiteckServices.CASE_TASK_SERVICE);
    }
}
