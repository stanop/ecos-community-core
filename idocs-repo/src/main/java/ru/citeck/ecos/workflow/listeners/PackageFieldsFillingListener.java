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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Alexander Nemerov
 *         created on 22.04.2015.
 */
public class PackageFieldsFillingListener extends AbstractExecutionListener {

    private NodeService nodeService;
    private NamespacePrefixResolver namespaceService;

    @Override
    protected void notifyImpl(DelegateExecution execution) throws Exception {
        NodeRef packageRef = ListenerUtils.getWorkflowPackage(execution);
        Map<QName, Serializable> props = nodeService.getProperties(packageRef);
        boolean propsChanged = false;
        QName workflowInstanceIdProp = QName.createQName("bpm", "workflowInstanceId", namespaceService);
        if(!props.containsKey(workflowInstanceIdProp)) {
            props.put(workflowInstanceIdProp, "activiti$" + execution.getProcessInstanceId());
            propsChanged = true;
        }
        QName workflowDefinitionNameProp = QName.createQName("bpm", "workflowDefinitionName", namespaceService);
        if(!props.containsKey(workflowDefinitionNameProp)) {
            props.put(workflowDefinitionNameProp, "activiti$" + execution.getProcessDefinitionId().split(":")[0]);
            propsChanged = true;
        }
        if(propsChanged) {
            nodeService.setProperties(packageRef, props);
        }
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespacePrefixResolver namespaceService) {
        this.namespaceService = namespaceService;
    }
}
