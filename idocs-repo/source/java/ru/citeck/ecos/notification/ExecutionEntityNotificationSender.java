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
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.alfresco.service.cmr.repository.TemplateService;

import ru.citeck.ecos.security.NodeOwnerDAO;

/**
 * Notification sender for tasks (ItemType = ExecutionEntity).
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
class ExecutionEntityNotificationSender extends AbstractNotificationSender<ExecutionEntity> {

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
	NodeRef docsInfo;
	protected boolean sendToOwner;
	private NodeOwnerDAO nodeOwnerDAO;
private static final Log logger = LogFactory.getLog(ExecutionEntityNotificationSender.class);
	public static final String ARG_MODIFIER = "modifier";
	List<String> allowDocList;
	Map<String, Map<String,String>> subjectTemplates;
	private TemplateService templateService;
	private String nodeVariable;
	private String templateEngine;

	@Override
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    	super.setServiceRegistry(serviceRegistry);
		this.qNameConverter = new WorkflowQNameConverter(namespaceService);
		this.authenticationService = serviceRegistry.getAuthenticationService();
		this.personService = serviceRegistry.getPersonService();
	}	
	
	/**
	* Recipients provided as parameter taskSubscribers: "task name"-{"doc type1"-"recepient field1", ...}
	* @param task subscribers
	*/
	public void setTaskSubscribers(Map<String, Map<String,List<String>>> taskSubscribers) {
    	this.taskSubscribers = taskSubscribers;
    }

  // get notification template arguments for the task
	protected Map<String, Serializable> getNotificationArgs(ExecutionEntity task) {
		Map<String, Serializable> args = new HashMap<String, Serializable>();
		//args.put(ARG_TASK, getTaskInfo(task));
		args.put(ARG_WORKFLOW, getWorkflowInfo(task));
		String userName = authenticationService.getCurrentUserName();
		NodeRef person = personService.getPerson(userName);
		String last_name = (String)nodeService.getProperty(person,ContentModel.PROP_FIRSTNAME);
		String first_name = (String)nodeService.getProperty(person,ContentModel.PROP_LASTNAME);
		args.put(ARG_MODIFIER, last_name+" "+first_name);
		return args;
	}
	
	/*private Serializable getTaskInfo(DelegateTask task) {
		HashMap<String, Object> taskInfo = new HashMap<String, Object>();
		taskInfo.put(ARG_TASK_ID, task.getId());
		taskInfo.put(ARG_TASK_NAME, task.getName());
		taskInfo.put(ARG_TASK_DESCRIPTION, task.getDescription());
		HashMap<String, Serializable> properties = new HashMap<String, Serializable>();
		taskInfo.put(ARG_TASK_PROPERTIES, properties);
		properties.put(ARG_TASK_PROPERTIES_PRIORITY, task.getPriority());
		properties.put(ARG_TASK_PROPERTIES_DESCRIPTION, task.getDescription());
		properties.put(ARG_TASK_PROPERTIES_DUEDATE, task.getDueDate());
		String userName = authenticationService.getCurrentUserName();
		NodeRef person = personService.getPerson(userName);
		String last_name = (String)nodeService.getProperty(person,ContentModel.PROP_FIRSTNAME);
		String first_name = (String)nodeService.getProperty(person,ContentModel.PROP_LASTNAME);
		taskInfo.put(ARG_TASK_EDITOR,last_name+" "+first_name);
		return taskInfo;
	}*/
	
	private Serializable getWorkflowInfo(ExecutionEntity task) {
		HashMap<String, Object> workflowInfo = new HashMap<String, Object>();
		//workflowInfo.put(ARG_WORKFLOW_ID, "activiti$" +task.getId());
		workflowInfo.put(ARG_WORKFLOW_ID, task.getId());
		HashMap<String, Serializable> properties = new HashMap<String, Serializable>();
		workflowInfo.put(ARG_WORKFLOW_PROPERTIES, properties);
		for(Map.Entry<String, Object> entry : task.getVariables().entrySet()) {
			if(entry.getValue()!=null)
			{
				properties.put(entry.getKey(), entry.getValue().toString());
			}
			else
			{
				properties.put(entry.getKey(), null);
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
	public void sendNotification(ExecutionEntity task)
	{
		String subject = null;
		NodeRef workflowPackage = null;
		Vector<String> recipient = new Vector<String>();
		//ExecutionEntity executionEntity = ((ExecutionEntity)task.getExecution()).getProcessInstance();
		ActivitiScriptNode scriptNode = (ActivitiScriptNode)task.getVariable("bpm_package");
		if (scriptNode != null)
		{
			workflowPackage = scriptNode.getNodeRef();
		}
		if(workflowPackage!=null && nodeService.exists(workflowPackage))
		{
			List<ChildAssociationRef> children = services.getNodeService().getChildAssocs(workflowPackage);
			for(ChildAssociationRef child : children) {
				recipient.clear();
				NodeRef node = child.getChildRef();
				if(node!=null  && nodeService.exists(node))
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
				NodeRef template = getNotificationTemplate(task);
				if(template!=null && nodeService.exists(template))
				{
                    recipient.addAll(getRecipients(task, template, docsInfo));
					String from = null;
					String notificationProviderName = EMailNotificationProvider.NAME;
					if(subjectTemplates!=null)
					{
						String processDef = task.getProcessDefinitionId();
						String wfkey = "activiti$"+processDef.substring(0,processDef.indexOf(":"));
						if(subjectTemplates.containsKey(wfkey))
						{
							Map<String,String> taskSubjectTemplate = subjectTemplates.get(wfkey);
							if(taskSubjectTemplate.containsKey(qNameConverter.mapQNameToName(nodeService.getType(docsInfo))))
							{
								HashMap<String,Object> model = new HashMap<String,Object>(1);
								model.put(nodeVariable, docsInfo);
								subject = templateService.processTemplateString(templateEngine, taskSubjectTemplate.get(qNameConverter.mapQNameToName(nodeService.getType(docsInfo))), model);
							}
						}
						else
						{
							subject = (String) nodeService.getProperty(template, ContentModel.PROP_TITLE);
						}
					}
					else
					{
						subject = (String) nodeService.getProperty(template, ContentModel.PROP_TITLE);
					}
					for(String to : recipient) {
						notificationContext.addTo(to);
						
					}
					notificationContext.setSubject(subject);
					setBodyTemplate(notificationContext, template);
					notificationContext.setTemplateArgs(getNotificationArgs(task));
					notificationContext.setAsyncNotification(true);
					if (null != from) {
						notificationContext.setFrom(from);
					}
					// send
					logger.debug("Send notification");
					services.getNotificationService().sendNotification(notificationProviderName, notificationContext);
				}
            }
		}

	}
	
	public NodeRef getNotificationTemplate(ExecutionEntity task)
	{
		String processDef = task.getProcessDefinitionId();
		String wfkey = "activiti$"+processDef.substring(0,processDef.indexOf(":"));
		String tkey = (String)task.getVariableLocal("taskFormKey");
		logger.debug("template for notification "+getNotificationTemplate(wfkey, tkey));
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
    
	protected void sendToAssignee(ExecutionEntity task, Set<String> authorities)
	{

	}

	protected void sendToInitiator(ExecutionEntity task, Set<String> authorities)
	{
		NodeRef initiator = ((ActivitiScriptNode)task.getVariable("initiator")).getNodeRef();
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

	
	protected void sendToSubscribers(ExecutionEntity task, Set<String> authorities, List<String> taskSubscribers)
	{
		for(String subscriber : taskSubscribers)
		{
			QName sub = qNameConverter.mapNameToQName(subscriber);
			NodeRef workflowPackage = null;
			ActivitiScriptNode scriptNode = (ActivitiScriptNode)task.getVariable("bpm_package");
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
	
	public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}

	public void setTemplateEngine(String templateEngine) {
		this.templateEngine = templateEngine;
	}

    public void setSubjectTemplates(Map<String, Map<String,String>> subjectTemplates) {
        this.subjectTemplates = subjectTemplates;
    }

	public void setNodeVariable(String nodeVariable) {
		this.nodeVariable = nodeVariable;
	}

}
