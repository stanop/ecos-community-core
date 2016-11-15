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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.service.CiteckServices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Maxim Strizhov
 */
public class CaseTaskEndProcessListener extends AbstractExecutionListener {

    private static final Log log = LogFactory.getLog(CaseTaskEndProcessListener.class);

    private CaseActivityService caseActivityService;
    private NodeService nodeService;

    @Override
    protected void notifyImpl(final DelegateExecution delegateExecution) throws Exception {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                CaseTaskEndProcessListener.this.doWork(delegateExecution);
                return null;
            }
        });
    }

    private void doWork(DelegateExecution delegateExecution) {
        if (ListenerUtils.getDocument(delegateExecution, nodeService) == null) {
            return;
        }
        stopActivity(delegateExecution);
    }

    private void stopActivity(DelegateExecution delegateExecution) {

        NodeRef bpmPackage = ListenerUtils.getWorkflowPackage(delegateExecution);

        List<AssociationRef> packageAssocs = nodeService.getSourceAssocs(bpmPackage, ICaseTaskModel.ASSOC_WORKFLOW_PACKAGE);

        if(packageAssocs != null && packageAssocs.size() > 0) {
            ActionConditionUtils.getProcessVariables().putAll(delegateExecution.getVariables());
            caseActivityService.stopActivity(packageAssocs.get(0).getSourceRef());
        }
    }

    private static <T> T castOrNull(Object obj, Class<T> clazz) {
        if (obj != null && clazz.isAssignableFrom(obj.getClass())) {
            return clazz.cast(obj);
        }
        return null;
    }

    @Override
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.caseActivityService = (CaseActivityService) serviceRegistry.getService(CiteckServices.CASE_ACTIVITY_SERVICE);
    }
}
