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
package ru.citeck.ecos.workflow.confirm;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.context.Context;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import ru.citeck.ecos.confirm.ConfirmService;
import ru.citeck.ecos.service.CiteckServices;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: alexander.nemerov
 * Date: 01.10.13
 */
public class AddConsiderableVersion implements TaskListener {

    private ConfirmService confirmService;
	private NamespaceService namespaceService;
    private NodeService nodeService;

    private void initServices() {
        ServiceRegistry services = (ServiceRegistry) Context.getProcessEngineConfiguration()
                .getBeans().get(ActivitiConstants.SERVICE_REGISTRY_BEAN_KEY);
        confirmService = (ConfirmService) services.getService(CiteckServices.CONFIRM_SERVICE);
        namespaceService = services.getNamespaceService();
        nodeService = services.getNodeService();

    }

    @Override
    public void notify(DelegateTask delegateTask) {
        if(delegateTask.getAssignee() == null) {
        	return;
        }

        initServices();
        WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(namespaceService);
        NodeRef packageRef = ((ScriptNode) delegateTask
                .getVariable(qNameConverter.mapQNameToName(WorkflowModel.ASSOC_PACKAGE))).getNodeRef();
        Set<QName> includeQNames = new HashSet<QName>();
        includeQNames.add(WorkflowModel.ASSOC_PACKAGE_CONTAINS);
        includeQNames.add(ContentModel.ASSOC_CONTAINS);
        List<ChildAssociationRef> documentRefs = nodeService.getChildAssocs(packageRef);
        for (ChildAssociationRef documentRef : documentRefs) {
            if(!includeQNames.contains(documentRef.getTypeQName()) || documentRef.getChildRef() == null) {
                continue;
            }
            try {
                confirmService.addCurrentVersionToConsiderable(delegateTask.getAssignee(), documentRef.getChildRef());
            } catch (JSONException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
