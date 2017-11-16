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
package ru.citeck.ecos.invariants.view;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.invariants.utils.WorkflowServiceUtils;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

public class NodeViewServiceJS extends AlfrescoScopableProcessorExtension {

    private static final QName WORKFLOW_DEFINITION_NAME = QName.createQName(
            "http://www.citeck.ru/model/icaseTask/1.0", "workflowDefinitionName"
    );
    private static final String DEFAULT_TASK_VIEW = "wfcf:defaultTask";

    private NodeViewService impl;

    private WorkflowServiceUtils workflowServiceUtils;

    private NodeService nodeService;

    public boolean hasNodeView(String type, String id) {
        /** Check workflow definition existing */
        WorkflowDefinition workflowDefinition = workflowServiceUtils.getWorkflowDefinition(type);
        if (workflowDefinition != null) {
            /** Workflow view */
            type = workflowDefinition.getStartTaskDefinition().getId();
            boolean result = impl.hasNodeView(new NodeView.Builder(serviceRegistry.getNamespaceService())
                    .className(type)
                    .id(id)
                    .build());
            if (result) {
                return true;
            } else {
                return impl.hasNodeView(new NodeView.Builder(serviceRegistry.getNamespaceService())
                        .className(DEFAULT_TASK_VIEW)
                        .id(id)
                        .build());
            }
        } else {
            /** Common view */
            return impl.hasNodeView(new NodeView.Builder(serviceRegistry.getNamespaceService())
                    .className(type)
                    .id(id)
                    .build());
        }

    }
    
    public boolean hasNodeView(ScriptNode node, String id) {
        boolean result = impl.hasNodeView(new NodeView.Builder(serviceRegistry.getNamespaceService())
            .className(node.getTypeShort())
            .id(id)
            .build());
        if (result) {
            return true;
        }
        /** Load workflow */
        if (node != null && node.getNodeRef() != null) {
            String workflowDefinitionName = (String) nodeService.getProperty(node.getNodeRef(), WORKFLOW_DEFINITION_NAME);
            WorkflowDefinition workflowDefinition = workflowServiceUtils.getWorkflowDefinition(workflowDefinitionName);
            if (workflowDefinition != null) {
                String type = workflowDefinition.getStartTaskDefinition().getId();
                result = impl.hasNodeView(new NodeView.Builder(serviceRegistry.getNamespaceService())
                        .className(type)
                        .id(id)
                        .build());
                if (result) {
                    return true;
                } else {
                    return impl.hasNodeView(new NodeView.Builder(serviceRegistry.getNamespaceService())
                            .className(DEFAULT_TASK_VIEW)
                            .id(id)
                            .build());
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public NodeView getNodeView(String className, String id) {
        return impl.getNodeView(new NodeView.Builder(serviceRegistry.getNamespaceService())
            .className(className)
            .id(id)
            .build());
    }

    public NodeView getNodeView(ScriptNode node, String id) {
        return impl.getNodeView(new NodeView.Builder(serviceRegistry.getNamespaceService())
            .className(node.getTypeShort())
            .id(id)
            .build());
    }
    
    public void setImpl(NodeViewService impl) {
        this.impl = impl;
    }

    public void setWorkflowServiceUtils(WorkflowServiceUtils workflowServiceUtils) {
        this.workflowServiceUtils = workflowServiceUtils;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
