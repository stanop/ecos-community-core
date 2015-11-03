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
package ru.citeck.ecos.webscripts.workflow;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.webscripts.common.BaseAbstractWebscript;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DocumentAssignee extends BaseAbstractWebscript {

	public static final String NODE_REF = "nodeRef";

	private WorkflowService workflowService;

	@Override
	protected void executeInternal(WebScriptRequest aRequest,
	                               WebScriptResponse aResponse) throws Exception {
		NodeRef nodeRef = new NodeRef(aRequest.getParameter(NODE_REF));
		List<String> assigneeList = getDocumentAssignees(nodeRef);
		buildResult(assigneeList, aResponse);
	}

	private List<String> getDocumentAssignees(NodeRef nodeRef) {
		Set<String> authorities = new HashSet<String>();
		List<WorkflowInstance> workflows = workflowService.getWorkflowsForContent(nodeRef, true);
		for (WorkflowInstance workflow : workflows) {
			WorkflowTaskQuery workflowTaskQuery = new WorkflowTaskQuery();
			workflowTaskQuery.setActive(true);
			workflowTaskQuery.setProcessId(workflow.getId());
			List<WorkflowTask> tasks = workflowService.queryTasks(workflowTaskQuery);
			for (WorkflowTask task : tasks) {
				String owner = (String) task.getProperties().get(ContentModel.PROP_OWNER);
				if(owner != null) {
					authorities.add(owner);
				}
			}
		}
		return new ArrayList<String>(authorities);
	}

	private void buildResult(List<String> assigneeList,
	                         WebScriptResponse aResponse) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		result.put("data", array);
		for(String assignee : assigneeList) {
			array.put(assignee);
		}
		aResponse.setContentType("application/json");
		aResponse.setContentEncoding("UTF-8");
		aResponse.addHeader("Cache-Control", "no-cache");
		aResponse.addHeader("Pragma", "no-cache");
		// write JSON into response stream
		result.write(aResponse.getWriter());
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

}
