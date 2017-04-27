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

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.task.Task;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.dictionary.types.period.Days;
import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import org.jbpm.calendar.Day;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import ru.citeck.ecos.model.DmsModel;

public class OverDueTaskGeneration extends AbstractWebScript {

	private ServiceRegistry serviceRegistry;
	private SysAdminParams sysAdminParams;


	private String engineId = ActivitiConstants.ENGINE_ID + "$";

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setSysAdminParams(SysAdminParams sysAdminParams) {
		this.sysAdminParams = sysAdminParams;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		// TODO Auto-generated method stub

		// construct resulting JSON
		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();

		ProcessEngine processEngine = ProcessEngines.getProcessEngines().get(
				ProcessEngines.NAME_DEFAULT);
		RuntimeService runtimeService = processEngine.getRuntimeService();

		List<Task> tasks = processEngine.getTaskService().createTaskQuery()
				.dueBefore(new Date()).processDefinitionKey("contract-approval-process").list();

		for (Task task : tasks) {

			// Get the workflow package node
			NodeRef workflowPackage = null;
			ActivitiScriptNode scriptNode = (ActivitiScriptNode) runtimeService
					.getVariable(task.getExecutionId(),
							WorkflowNotificationUtils.PROP_PACKAGE);
			if (scriptNode != null) {
				workflowPackage = scriptNode.getNodeRef();
				try {
					array.put(construct(req, task, workflowPackage));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		res.setContentType("application/json");
		res.setContentEncoding("UTF-8");
		res.addHeader("Cache-Control", "no-cache");
		res.addHeader("Pragma","no-cache");

		try {
			result.put("data", array);
			result.write(res.getWriter());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private JSONObject construct(WebScriptRequest aRequest, Task task,
			NodeRef workflowPackage) throws JSONException {
		
		WorkflowTask taskById = serviceRegistry.getWorkflowService()
				.getTaskById(engineId + task.getId());		
		
	
		JSONObject result = new JSONObject();

		List<ChildAssociationRef> agreementChilds = serviceRegistry
				.getNodeService().getChildAssocs(workflowPackage);

		if (agreementChilds.size() > 0) {
			Serializable agreementNumber = serviceRegistry.getNodeService()
					.getProperty(agreementChilds.get(0).getChildRef(),
							DmsModel.PROP_AGREEMENT_NUMBER);

			Date date = (Date) serviceRegistry.getNodeService().getProperty(
					agreementChilds.get(0).getChildRef(),
					ContentModel.PROP_CREATED);

			NodeRef agreemenRef = agreementChilds.get(0).getChildRef();

			List<AssociationRef> targetAssocs = serviceRegistry
					.getNodeService().getTargetAssocs(agreemenRef,
							DmsModel.AGREEMENT_TO_CONTRACTOR);

			Serializable contractor = "-";
			if (targetAssocs.size()>0) {
				contractor=serviceRegistry.getNodeService()
						.getProperty(targetAssocs.get(0).getTargetRef(),
								ContentModel.PROP_NAME);
			}

			result.put("contractId", agreementNumber);
			result.put("contractCreateDate",
					DateFormatUtils.format(date, "dd.MM.yyyy"));
			result.put("contractorName", contractor);
		}



		String assignee = task.getAssignee();
		if (assignee != null) {
			NodeRef person = serviceRegistry.getPersonService().getPerson(
					assignee);
			String firstName = (String) serviceRegistry.getNodeService()
					.getProperty(person, ContentModel.PROP_FIRSTNAME);
			String lastName = (String) serviceRegistry.getNodeService()
					.getProperty(person, ContentModel.PROP_LASTNAME);
			assignee = firstName + " " + lastName;
		} else {

			// Retrieve Task Properties
			
			Map<QName, Serializable> properties = taskById.getProperties();
			List<?> groupIdRef = (List<?>) properties.get(QName.createQName(
					"http://www.alfresco.org/model/bpm/1.0", "pooledActors"));

			NodeRef object = (NodeRef) groupIdRef.get(0);
			String property = (String) serviceRegistry.getNodeService()
					.getProperty(object, ContentModel.PROP_AUTHORITY_NAME);
			String authorityDisplayName = serviceRegistry.getAuthorityService().getAuthorityDisplayName(property);
			assignee = authorityDisplayName;
		}
		

		//int overDueDays = new Date().getDate() - task.getDueDate().getDate();
		
		
		result.put("overDueDays", Math.abs(subtractDays(new  Date(), task.getDueDate())));
		result.put("assignee", assignee);		
		result.put("taskName", task.getDescription()+" ("+taskById.getTitle()+")");
		
		result.put("taskCreateTime",
				DateFormatUtils.format(task.getCreateTime(), "dd.MM.yyyy"));
		result.put("taskId",engineId+task.getId());

		return result;
	}
	 
	  private int subtractDays(Date date1, Date date2) 
	  { 
		  /*
	    GregorianCalendar gc1 = new GregorianCalendar();  gc1.setTime(date1); 
	    GregorianCalendar gc2 = new GregorianCalendar();  gc2.setTime(date2); 

	    int days1 = 0; 
	    int days2 = 0; 
	    int maxYear = Math.max(gc1.get(Calendar.YEAR), gc2.get(Calendar.YEAR)); 

	    GregorianCalendar gctmp = (GregorianCalendar) gc1.clone(); 
	    for (int f = gctmp.get(Calendar.YEAR);  f < maxYear;  f++) 
	      {days1 += gctmp.getActualMaximum(Calendar.DAY_OF_YEAR);  gctmp.add(Calendar.YEAR, 1);} 

	    gctmp = (GregorianCalendar) gc2.clone(); 
	    for (int f = gctmp.get(Calendar.YEAR);  f < maxYear;  f++) 
	      {days2 += gctmp.getActualMaximum(Calendar.DAY_OF_YEAR);  gctmp.add(Calendar.YEAR, 1);} 

	    days1 += gc1.get(Calendar.DAY_OF_YEAR) - 1; 
	    days2 += gc2.get(Calendar.DAY_OF_YEAR) - 1; 
	    
	    return (days1 - days2); 
	    */
		  
	    long diff = date1.getTime() - date2.getTime();
	    int days = (int) (diff / (1000 * 60 * 60 * 24));
	    return days+1;

	  } 
}
