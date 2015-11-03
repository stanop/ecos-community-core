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
package ru.citeck.ecos.webscripts.workflow.tasks;

import java.util.List;

import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import ru.citeck.ecos.webscripts.common.BaseAbstractWebscript;

/**
 * Java-backed Alfresco webscript for user's workflow tasks listing.
 * 
 */
public class UserWorkflowTasksWebscript extends BaseAbstractWebscript {
	
	private WorkflowService workflowService;
	private String shareApp;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void executeInternal(WebScriptRequest aRequest, WebScriptResponse aResponse) throws Exception {	
		
		// get user name to read tasks for
		String username = aRequest.getServiceMatch().getTemplateVars().get("username");
		
		// load active tasks for the given user
		List<WorkflowTask> taskList = this.workflowService.getAssignedTasks(username, WorkflowTaskState.IN_PROGRESS);

		// construct resulting JSON 
		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		result.put("data", array);
		
		// fill array with JSON tasks
		for (WorkflowTask task : taskList) {
			array.put(construct(aRequest,task, username));
		}
		
		aResponse.setContentType("application/json");
		aResponse.setContentEncoding("UTF-8");
		aResponse.addHeader("Cache-Control", "no-cache");
		aResponse.addHeader("Pragma","no-cache");
		// write JSON into response stream
		result.write(aResponse.getWriter());		
	}

	/**
	 * Builds JSON object from workflow task given.
	 * @param aTask workflow task
	 * @param aUsername user name
	 * @return JSON otask object
	 */
	private JSONObject construct(WebScriptRequest aRequest, WorkflowTask aTask, String aUsername) throws JSONException {
		
		JSONObject result = new JSONObject();
		
		result.put("id", aTask.id);
		result.put("title", aTask.title);
		result.put("description", aTask.path.instance.description);
//		result.put("url", "/page/user/" + aUsername + "/task-edit?taskId=" + aTask.id + "&referrer=tasks");
		result.put("url", (new StringBuilder()).append(shareApp).append("/page/user/").append(aUsername).append("/task-edit?taskId=").append(aTask.id).append("&referrer=tasks").toString());
		
		return result;
	}
	
	/**
	 * @param workflowService the workflowService to set
	 */
	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}
	
	public void setShareApp(String shareApp)
    {
        this.shareApp = shareApp;
    }

}
