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
package ru.citeck.ecos.notification;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;
import java.util.Set;

import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTask;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

import ru.citeck.ecos.security.NodeOwnerDAO;
/**
 * Notification sender for tasks (ItemType = DelegateTask).
 * 
 * The following implementation is used:
 * - subject line: default
 * - template: retrieved by key = process-definition
 * - template args: 
 *   {
 *     "task": {
 *       "id": "task id",
 *       "name": "task name",
 *       "description": "task description",
 *       "priority": "task priority",
 *       "dueDate": "task dueDate",
 *       }
 *     },
 *     "workflow": {
 *       "id": "workflow id",
 *       "documents": [
 *         "nodeRef1",
 *         ...
 *       ]
 *     }
 *   }
 * - notification recipients - provided as parameters
 */
class StartDelegateTaskNotificationSender extends AbstractNotificationSender<DelegateTask> {

	// template argument names:
	public static final String ARG_TASK = "task";
	public static final String ARG_TASK_ID = "id";
	public static final String ARG_TASK_NAME = "name";
	public static final String ARG_TASK_DESCRIPTION = "description";
	public static final String ARG_TASK_EDITOR = "editor";
	public static final String ARG_TASK_PROPERTIES = "properties";
	public static final String ARG_TASK_PROPERTIES_PRIORITY = "bpm_priority";
	public static final String ARG_TASK_PROPERTIES_DESCRIPTION = "bpm_description";
	public static final String ARG_TASK_PROPERTIES_DUEDATE = "bpm_dueDate";
	public static final String ARG_WORKFLOW = "workflow";
	public static final String ARG_WORKFLOW_ID = "id";
	public static final String ARG_WORKFLOW_PROPERTIES = "properties";
	public static final String ARG_WORKFLOW_DOCUMENTS = "documents";
	private Map<String, Map<String,List<String>>> taskSubscribers;
	protected WorkflowQNameConverter qNameConverter;
	protected PersonService personService;
	protected AuthenticationService authenticationService;
	protected AuthorityService authorityService;
	NodeRef docsInfo;
	protected boolean sendToOwner;
	protected boolean mandatoryFieldsFilled = true;
	private NodeOwnerDAO nodeOwnerDAO;
	private Map<String, Map<String,String>> taskProperties;
	private Map<String, Boolean> checkAssignee;
	private Map<String, List<String>> additionRecipients;
	private Map<String, List<String>> supervisors;
	private Map<String, List<String>> mandatoryFields;
	List<String> allowDocList;

	@Override
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    	super.setServiceRegistry(serviceRegistry);
		this.qNameConverter = new WorkflowQNameConverter(namespaceService);
		this.authenticationService = serviceRegistry.getAuthenticationService();
		this.personService = serviceRegistry.getPersonService();
		this.authorityService = serviceRegistry.getAuthorityService();
	}	
	
	/**
	* Recipients provided as parameter taskSubscribers: "task name"-{"doc type1"-"recepient field1", ...}
	* @param task subscribers
	*/
	public void setTaskSubscribers(Map<String, Map<String,List<String>>> taskSubscribers) {
    	this.taskSubscribers = taskSubscribers;
    }

  // get notification template arguments for the task
	protected Map<String, Serializable> getNotificationArgs(DelegateTask task) {
		Map<String, Serializable> args = new HashMap<String, Serializable>();
		args.put(ARG_TASK, getTaskInfo(task));
		args.put(ARG_WORKFLOW, getWorkflowInfo(task));
		return args;
	}
	
	private Serializable getTaskInfo(DelegateTask task) {
		HashMap<String, Object> taskInfo = new HashMap<String, Object>();
		taskInfo.put(ARG_TASK_ID, task.getId());
		taskInfo.put(ARG_TASK_NAME, task.getName());
		taskInfo.put(ARG_TASK_DESCRIPTION, task.getDescription());
		HashMap<String, Serializable> properties = new HashMap<String, Serializable>();
		taskInfo.put(ARG_TASK_PROPERTIES, properties);
		ExecutionEntity executionEntity = ((ExecutionEntity)task.getExecution()).getProcessInstance();
		for(Map.Entry<String, Object> entry : executionEntity.getVariables().entrySet()) {
			if(entry.getValue()!=null)
			{
				properties.put(entry.getKey(), entry.getValue().toString());
			}
			else
			{
				properties.put(entry.getKey(), null);
			}
		}
		WorkflowTask wfTask = services.getWorkflowService().getTaskById("activiti$"+task.getId());
		for(Map.Entry<QName, Serializable> entry : wfTask.getProperties().entrySet()) {
			properties.put(qNameConverter.mapQNameToName(entry.getKey()), entry.getValue());
		}
		if(mandatoryFields!=null)
		{
			List<String> fields = mandatoryFields.get(task.getName());
			if(fields!=null && fields.size()>0)
			{
				for(String field : fields)
				{
					if(field!=null && properties.get(field)==null || "".equals(properties.get(field).toString().trim()))
					{
						mandatoryFieldsFilled = false;
					}
				}
			}
		}
		String userName = authenticationService.getCurrentUserName();
		NodeRef person = personService.getPerson(userName);
		if(nodeService.exists(person))
		{
			String last_name = (String)nodeService.getProperty(person,ContentModel.PROP_FIRSTNAME);
			String first_name = (String)nodeService.getProperty(person,ContentModel.PROP_LASTNAME);
			taskInfo.put(ARG_TASK_EDITOR,last_name+" "+first_name);
		}
		return taskInfo;
	}
	
	private Serializable getWorkflowInfo(DelegateTask task) {
		HashMap<String, Object> workflowInfo = new HashMap<String, Object>();
		WorkflowTask wfTask = services.getWorkflowService().getTaskById("activiti$"+task.getId());
		if(wfTask!=null) {
			WorkflowInstance workflow = wfTask.getPath().getInstance();
			if(workflow!=null) {
				workflowInfo.put(ARG_WORKFLOW_ID, workflow.getId());
			}
		}
		workflowInfo.put(ARG_WORKFLOW_DOCUMENTS, docsInfo);
		return workflowInfo;
	}

	/**
	* Method send notificatiion about start task to notification recipients.
	* Mail sends to each document to subscriber because task can containls a lot of different documents 
	* and these documents can contains different subscriber.
	* @param Delegate Task
	*/
	@Override
	public void sendNotification(DelegateTask task)
	{
		Set<String> authtorities = authorityService.getAuthorities();
		boolean sendBasedOnUser = true;
		String subject = null;
		if(checkAssignee!=null && checkAssignee.containsKey(task.getName()) && checkAssignee.get(task.getName()))
		{
			sendBasedOnUser = false;
			if(authtorities!=null && authtorities.size()>0)
			{
				if(supervisors!=null && supervisors.size()>0)
				{
					List<String> taskSupervisors = supervisors.get(task.getName());
					if(taskSupervisors!=null && taskSupervisors.size()>0)
					{
						for(String supervisor : taskSupervisors)
						{
							if(authtorities.contains(supervisor))
							{
								sendBasedOnUser = true;
								break;
							}
						}
					}
				}
			}
		}
		NodeRef workflowPackage = null;
		Vector<String> recipient = new Vector<String>();
		ExecutionEntity executionEntity = ((ExecutionEntity)task.getExecution()).getProcessInstance();
		ActivitiScriptNode scriptNode = (ActivitiScriptNode)executionEntity.getVariable("bpm_package");
		if (scriptNode != null)
		{
			workflowPackage = scriptNode.getNodeRef();
		}
		if(workflowPackage!=null && sendBasedOnUser)
		{
			List<ChildAssociationRef> children = services.getNodeService().getChildAssocs(workflowPackage);
			for(ChildAssociationRef child : children) {
				recipient.clear();
				NodeRef node = child.getChildRef();
				if(node!=null && nodeService.exists(node))
				{
					if(allowDocList==null)
					{
						docsInfo = node;
						break;
					}
					else
					{
						if(allowDocList.contains(qNameConverter.mapQNameToName(nodeService.getType(node))))
						{
							docsInfo = node;
							break;
						}
					}
				}
			}
				if(docsInfo!=null && nodeService.exists(docsInfo))
				{
						NotificationContext notificationContext = new NotificationContext();
						if(additionRecipients!=null)
						{
							List<String> addition = additionRecipients.get(task.getName());
							if(addition!=null && addition.size()>0)
							{
								for(String add : addition)
								{
									notificationContext.addTo(add);
								}
							}
						}
						NodeRef template = getNotificationTemplate(task);
						String from = null;
						String notificationProviderName = EMailNotificationProvider.NAME;
						if(template!=null && nodeService.exists(template))
						{
							subject = (String) nodeService.getProperty(template, ContentModel.PROP_TITLE);
							if(subject == null) {
								subject = task.getName();
							}
							recipient.addAll(getRecipients(task, template, docsInfo));
							setBodyTemplate(notificationContext, template);
						}
						notificationContext.setSubject(subject);
						for(String to : recipient) {
							notificationContext.addTo(to);
						}
						notificationContext.setTemplateArgs(getNotificationArgs(task));
						notificationContext.setAsyncNotification(true);
						if (null != from) {
							notificationContext.setFrom(from);
						}
						// send
						if(mandatoryFieldsFilled)
							services.getNotificationService().sendNotification(notificationProviderName, notificationContext);
				}
		}

	}
	
	public NodeRef getNotificationTemplate(DelegateTask task)
	{
		String processDef = task.getProcessDefinitionId();
		String wfkey = "activiti$"+processDef.substring(0,processDef.indexOf(":"));
		String tkey = (String)task.getVariableLocal("taskFormKey");
		return getNotificationTemplate(wfkey, tkey);
	}

	/**
	* Include initiator of process to recipients
	* @param true or false
	*/
	public void setSendToOwner(Boolean sendToOwner) {
    	this.sendToOwner = sendToOwner.booleanValue();
    }
	
	public void setNodeOwnerDAO(NodeOwnerDAO nodeOwnerDAO) {
		this.nodeOwnerDAO = nodeOwnerDAO;
	}
	
	public void setTaskProperties(Map<String, Map<String,String>> taskProperties) {
		this.taskProperties = taskProperties;
	}
	
	public void setAdditionRecipients(Map<String, List<String>> additionRecipients) {
		this.additionRecipients = additionRecipients;
	}
	
	public void setSupervisors(Map<String, List<String>> supervisors) {
		this.supervisors = supervisors;
	}
	
	public void setMandatoryFields(Map<String, List<String>> mandatoryFields) {
		this.mandatoryFields = mandatoryFields;
	}
	
	public void setCheckAssignee(Map<String, Boolean> checkAssignee) {
		this.checkAssignee = checkAssignee;
	}
    
	protected void sendToAssignee(DelegateTask task, Set<String> authorities)
	{
		if (task.getAssignee() == null)
		{
			List<IdentityLinkEntity> identities = ((TaskEntity)task).getIdentityLinks();
			for (IdentityLinkEntity item : identities)
			{
				String group = item.getGroupId();
				if (group != null)
				{
					authorities.add(group);
				}
				String user = item.getUserId();
				if (user != null)
				{
					authorities.add(user);
				}
			}
		}
		else
		{
			authorities.add(task.getAssignee());
		}
	}

	protected void sendToInitiator(DelegateTask task, Set<String> authorities)
	{
		ExecutionEntity executionEntity = ((ExecutionEntity)task.getExecution()).getProcessInstance();
		NodeRef initiator = ((ActivitiScriptNode)executionEntity.getVariable("initiator")).getNodeRef();
		String initiator_name = (String) nodeService.getProperty(initiator, ContentModel.PROP_USERNAME);
		authorities.add(initiator_name);
	}
	
	protected void sendToOwner(Set<String> authorities, NodeRef node)
	{
		String owner = nodeOwnerDAO.getOwner(node);
		authorities.add(owner);
	}
	
	public void setAllowDocList(List<String> allowDocList) {
		this.allowDocList = allowDocList;
	}

	
	protected void sendToSubscribers(DelegateTask task, Set<String> authorities, List<String> taskSubscribers)
	{
		for(String subscriber : taskSubscribers)
		{
			QName sub = qNameConverter.mapNameToQName(subscriber);
			NodeRef workflowPackage = null;
			ExecutionEntity executionEntity = ((ExecutionEntity)task.getExecution()).getProcessInstance();
			ActivitiScriptNode scriptNode = (ActivitiScriptNode)executionEntity.getVariable("bpm_package");
			if (scriptNode != null)
			{
				workflowPackage = scriptNode.getNodeRef();
			}
			if(workflowPackage!=null)
			{
				List<ChildAssociationRef> children = nodeService.getChildAssocs(workflowPackage);
				for(ChildAssociationRef child : children) 
				{
					NodeRef node = child.getChildRef();
					Collection<AssociationRef> assocs = nodeService.getTargetAssocs(node, sub);
					for (AssociationRef assoc : assocs) 
					{
						NodeRef ref = assoc.getTargetRef();
						if(nodeService.exists(ref))
						{
							String sub_name = (String) nodeService.getProperty(ref, ContentModel.PROP_USERNAME);
							authorities.add(sub_name);
						}
					}
				}
			}
		}
	}
}
