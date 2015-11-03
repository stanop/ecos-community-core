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
package ru.citeck.ecos.workflow.related;

import flexjson.JSONSerializer;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.workflow.related.WorkflowDTO;
import ru.citeck.ecos.workflow.related.WorkflowDTOBuilder;

import java.util.*;

/**
 *
 */
public class RelatedWorkflowService extends BaseScopableProcessorExtension {

    public static final QName PROP_RELATED_WF = QName.createQName("http://www.citeck.ru/alfresco/prefix/rwf", "relatedWorkflows");

    private JSONSerializer serializer = new JSONSerializer().exclude("class");

    protected WorkflowService workflowService;


    public void deleteRelatedWorkflows(String workflowId) {
        WorkflowInstance workflow = workflowService.getWorkflowById(workflowId);
        List<WorkflowInstance> relatedWorkflows = getRelatedWorkflows(workflow);
        for (WorkflowInstance wf : relatedWorkflows) {
            workflowService.deleteWorkflow(wf.getId());
        }
    }

    public String getRelatedWorkflowsForNode(String nodeRefId) {
        return getRelatedWorkflowsForNodeImpl(
                new NodeRef(nodeRefId)
        );
    }

    public String getRelatedWorkflowsForNode(String storeProtocol, String storeId, String nodeId) {
        return getRelatedWorkflowsForNodeImpl(
                new NodeRef(storeProtocol, storeId, nodeId)
        );
    }



    //

    private String getRelatedWorkflowsForNodeImpl(NodeRef nodeRef) {
        List<WorkflowInstance> workflows = workflowService.getWorkflowsForContent(nodeRef, true);
        List<WorkflowInstance> relatedWorkflows = new ArrayList<WorkflowInstance>();
        for (WorkflowInstance workflow : workflows) {
            relatedWorkflows.addAll(
                    getRelatedWorkflows(workflow)
            );
        }
        List<WorkflowDTO> result = new LinkedList<WorkflowDTO>();
        for (WorkflowInstance workflow : relatedWorkflows) {
            result.add(
                    WorkflowDTOBuilder.getInstance().build(new WorkflowDTO(), workflow)
            );
        }
        return serializer.serialize(result);
    }

    private List<WorkflowInstance> getRelatedWorkflows(WorkflowInstance workflow) {
        WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();
        tasksQuery.setTaskState(null);
        tasksQuery.setActive(null);
        tasksQuery.setProcessId(workflow.getId());

        List<WorkflowTask> tasks = workflowService.queryTasks(tasksQuery);
        List<WorkflowInstance> results = new ArrayList<WorkflowInstance>();
        for (WorkflowTask task : tasks) {
            String sRelatedWorkflows = (String) task.getProperties().get(PROP_RELATED_WF);
            if (null != sRelatedWorkflows) {
                results.addAll(
                        parseRelatedWorkflows(sRelatedWorkflows.split(","))
                );
            }
        }
        return results;
    }

    private List<WorkflowInstance> parseRelatedWorkflows(String[] sRelatedWorkflows) {
        List<WorkflowInstance> results = new ArrayList<WorkflowInstance>();
        for (String id : sRelatedWorkflows ) {
            if (!id.trim().isEmpty()) {
                WorkflowInstance workflow = workflowService.getWorkflowById(id);
                if (null != workflow) {
                    results.add(workflow);
                }
            }
        }
        return results;
    }

    public WorkflowService getWorkflowService() {
        return workflowService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }
}
