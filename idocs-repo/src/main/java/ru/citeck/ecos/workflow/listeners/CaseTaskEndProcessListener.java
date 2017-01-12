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
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.action.ActionConstants;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.search.*;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.workflow.utils.ActivitiVariableScopeMap;

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
                NodeRef docRef = ListenerUtils.getDocument(delegateExecution, nodeService);
                if (docRef == null) {
                    return null;
                }
                ActivitiVariableScopeMap activitiVariables = new ActivitiVariableScopeMap(delegateExecution, serviceRegistry);

                Object bpmPackage = activitiVariables.get("bpm_package");
                if(bpmPackage == null) return null;

                NodeRef packageRef = ((ScriptNode)bpmPackage).getNodeRef();
                List<AssociationRef> packageAssocs = nodeService.getSourceAssocs(packageRef, ICaseTaskModel.ASSOC_WORKFLOW_PACKAGE);

                if(packageAssocs != null && packageAssocs.size() > 0) {

                    Map<String, Object> actionConditionVariables =
                                        AlfrescoTransactionSupport.getResource(ActionConstants.ACTION_CONDITION_VARIABLES);

                    if(actionConditionVariables == null) {
                        actionConditionVariables = new HashMap<String, Object>();
                    }

                    actionConditionVariables.put("process", activitiVariables);
                    AlfrescoTransactionSupport.bindResource(ActionConstants.ACTION_CONDITION_VARIABLES, actionConditionVariables);

                    caseActivityService.stopActivity(packageAssocs.get(0).getSourceRef());
                }

                return null;
            }
        });
    }

    @Override
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.caseActivityService = (CaseActivityService) serviceRegistry.getService(CiteckServices.CASE_ACTIVITY_SERVICE);
    }
}
