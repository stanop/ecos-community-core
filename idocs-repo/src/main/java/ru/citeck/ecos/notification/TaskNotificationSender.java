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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.security.NodeOwnerDAO;

import java.io.Serializable;
import java.util.*;

/**
 * Notification sender for tasks (ItemType = WorkflowTask).
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
 *       "properties": {
 *         "property1": value1,
 *         ...
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
 * - notification recipients - assignee or pooled actors, whichever present
 * 
 * @author Sergey Tiunov
 */
class TaskNotificationSender extends AbstractNotificationSender<WorkflowTask> {

	// template argument names:
	public static final String ARG_TASK = "task";
	public static final String ARG_TASK_ID = "id";
	public static final String ARG_TASK_NAME = "name";
	public static final String ARG_TASK_TITLE = "title";
	public static final String ARG_TASK_DESCRIPTION = "description";
	public static final String ARG_TASK_PROPERTIES = "properties";
	public static final String ARG_WORKFLOW = "workflow";
	public static final String ARG_WORKFLOW_ID = "id";
	public static final String ARG_WORKFLOW_PROPERTIES = "properties";
	public static final String ARG_WORKFLOW_DOCUMENTS = "documents";

	protected WorkflowQNameConverter qNameConverter;
	protected boolean sendToInitiator;
	protected boolean sendToOwner;
	protected Map<String,Boolean> docMandatory;
	private static Log logger = LogFactory.getLog(TaskNotificationSender.class);
	List<String> allowDocList;
	private NodeOwnerDAO nodeOwnerDAO;

    @Override
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    	super.setServiceRegistry(serviceRegistry);
    	this.qNameConverter = new WorkflowQNameConverter(namespaceService);
    }
    
	@Override
	protected Collection<String> getNotificationRecipients(WorkflowTask task) {
		Set<String> recipients = new HashSet<String>();
		// add default recipients:
		if(defaultRecipients != null) {
			recipients.addAll(defaultRecipients);
		}
		Map<QName, Serializable> properties = task.getProperties();
		// try with user:
        if(getNotificationTemplate(task)!=null)
        {
            recipients.addAll(getRecipients(task, getNotificationTemplate(task), null));
        }
		// try with pool:
		@SuppressWarnings("unchecked")
		Collection<NodeRef> pool = (Collection<NodeRef>) properties.get(WorkflowModel.ASSOC_POOLED_ACTORS);
		if(recipients.size()==0)
		{
		if(pool != null) {
			for(NodeRef pooledActor : pool) {
				if(nodeService.exists(pooledActor))
				{
					QName type = nodeService.getType(pooledActor);
					if(type.equals(ContentModel.TYPE_PERSON)) {
						String name = (String) nodeService.getProperty(pooledActor, ContentModel.PROP_USERNAME);
						recipients.add(name);
					} else if(type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
						String name = (String) nodeService.getProperty(pooledActor, ContentModel.PROP_AUTHORITY_NAME);
						recipients.add(name);
					}
				}
			}
		}
		}
		return recipients;
	}

	// get notification template for the task
	@Override
	protected NodeRef getNotificationTemplate(WorkflowTask task) {
		// for tasks key is process definition name:
		String wfkey = task.getPath().getInstance().getDefinition().getName();
		String tkey = task.getDefinition().getId();
		return getNotificationTemplate(wfkey, tkey);
	}
	
	// get notification template arguments for the task
	@Override
	protected Map<String, Serializable> getNotificationArgs(WorkflowTask task) {
		Map<String, Serializable> args = new HashMap<String, Serializable>();
		args.put(ARG_TASK, this.getTaskInfo(task));
		args.put(ARG_WORKFLOW, this.getWorkflowInfo(task.getPath().getInstance()));
		return args;
	}
	
	// build task model
	private Serializable getTaskInfo(WorkflowTask task) {
		HashMap<String, Object> taskInfo = new HashMap<String, Object>();
		taskInfo.put(ARG_TASK_ID, task.getId());
		taskInfo.put(ARG_TASK_NAME, task.getName());
		taskInfo.put(ARG_TASK_TITLE, task.getTitle());
		taskInfo.put(ARG_TASK_DESCRIPTION, task.getDescription());
		HashMap<String, Serializable> properties = new HashMap<String, Serializable>();
		taskInfo.put(ARG_TASK_PROPERTIES, properties);
		for(Map.Entry<QName, Serializable> entry : task.getProperties().entrySet()) {
			properties.put(qNameConverter.mapQNameToName(entry.getKey()), entry.getValue());
		}
		return taskInfo;
	}
	
	// build workflow model
	private Serializable getWorkflowInfo(WorkflowInstance workflow) {
		HashMap<String, Object> workflowInfo = new HashMap<String, Object>();
		workflowInfo.put(ARG_WORKFLOW_ID, workflow.getId());

		NodeRef wfPackage = workflow.getWorkflowPackage();
		ArrayList<Object> docsInfo = new ArrayList<Object>();
		workflowInfo.put(ARG_WORKFLOW_DOCUMENTS, docsInfo);
		if (wfPackage == null) {
			return workflowInfo;
		}
		if(nodeService.exists(wfPackage))
		{
			List<ChildAssociationRef> children = services.getNodeService().getChildAssocs(wfPackage);
			for(ChildAssociationRef child : children) {
				if(allowDocList==null)
				{
					docsInfo.add(child.getChildRef());
				}
				else
				{
					if(allowDocList.contains(qNameConverter.mapQNameToName(services.getNodeService().getType(child.getChildRef()))))
					{
						docsInfo.add(child.getChildRef());
					}
				}
			}
		}
		return workflowInfo;
	}
	/**
	* Include initiator of process to recipients
	* @param true or false
	*/
	public void setSendToOwner(Boolean sendToOwner) {
    	this.sendToOwner = sendToOwner.booleanValue();
    }
	
	/**
	* Include initiator of process to recipients
	* @param true or false
	*/
	public void setSendToInitiator(Boolean sendToInitiator) {
    	this.sendToInitiator = sendToInitiator.booleanValue();
    }
	
	/**
	* Flag for document mandatory notifications
	* @param map of values
	*/
	public void setDocMandatory(Map<String,Boolean> docMandatory) {
    	this.docMandatory = docMandatory;
    }
	
	/**
	* List of document's types for notifications
	* @param list of values
	*/
	public void setAllowDocList(List<String> allowDocList) {
    	this.allowDocList = allowDocList;
    }
	
	@Override
	public void sendNotification(WorkflowTask task)
	{
		boolean docNecessary = false;
		boolean contains = false;
		if(docMandatory!=null && docMandatory.get(task.getDefinition().getId())!=null)
		{
			docNecessary = docMandatory.get(task.getDefinition().getId()).booleanValue();
		}
		if(docNecessary)
		{
			NodeRef wfPackage = task.getPath().getInstance().getWorkflowPackage();
			if(nodeService.exists(wfPackage))
			{
				List<ChildAssociationRef> children = services.getNodeService().getChildAssocs(wfPackage);
				for(ChildAssociationRef child : children) {
					if(allowDocList!=null && allowDocList.contains(qNameConverter.mapQNameToName(services.getNodeService().getType(child.getChildRef()))))
					{
						contains = true;
					}
					else
					{
						if(allowDocList==null && WorkflowModel.ASSOC_PACKAGE_CONTAINS.equals(child.getTypeQName()))
						{
							contains = true;
						}
					}
				}
				if(contains)
				{
					super.sendNotification(task);
				}
			}
		}
		else
			super.sendNotification(task);
	}
	protected void sendToAssignee(WorkflowTask task, Set<String> authorities)
	{
		String username = (String) task.getProperties().get(ContentModel.PROP_OWNER);
		if (username != null && !"".equals(username)) {
			authorities.add(username);
		}
	}

	protected void sendToInitiator(WorkflowTask task, Set<String> authorities)
	{
		if(task.getPath()!=null && task.getPath().getInstance()!=null && task.getPath().getInstance().getInitiator()!=null)
		{
			NodeRef initiator = task.getPath().getInstance().getInitiator();
			String initiator_name = (String) nodeService.getProperty(initiator, ContentModel.PROP_USERNAME);
			authorities.add(initiator_name);
		}
	}

	
	protected void sendToSubscribers(WorkflowTask task, Set<String> authorities, List<String> taskSubscribers)
	{
		for(String subscriber : taskSubscribers)
		{
			QName sub = qNameConverter.mapNameToQName(subscriber);
			if(task.getPath()!=null && task.getPath().getInstance()!=null)
			{
				NodeRef workflowPackage = task.getPath().getInstance().getWorkflowPackage();
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
	protected void sendToOwner(Set<String> authorities, NodeRef node)
	{
		String owner = nodeOwnerDAO.getOwner(node);
		if (owner != null && !"".equals(owner)) {
			authorities.add(owner);
		}
	}
	
	public void setNodeOwnerDAO(NodeOwnerDAO nodeOwnerDAO) {
		this.nodeOwnerDAO = nodeOwnerDAO;
	}

}
