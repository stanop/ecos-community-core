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

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.DmsModel;
import ru.citeck.ecos.security.NodeOwnerDAO;
import ru.citeck.ecos.server.utils.Utils;

import java.io.Serializable;
import java.util.*;

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
 * - notification recipients - assignee or pooled actors, whichever present
 * 
 * @author Elena Zaripova
 */
class DelegateTaskNotificationSender extends AbstractNotificationSender<DelegateTask> {

	// template argument names:
	public static final String ARG_TASK = "task";
	public static final String ARG_TASK_ID = "id";
	public static final String ARG_TASK_NAME = "name";
	public static final String ARG_TASK_DESCRIPTION = "description";
	public static final String ARG_TASK_PROPERTIES = "properties";
	public static final String ARG_TASK_PROPERTIES_PRIORITY = "bpm_priority";
	public static final String ARG_TASK_PROPERTIES_DESCRIPTION = "bpm_description";
	public static final String ARG_TASK_PROPERTIES_DUEDATE = "bpm_dueDate";
	public static final String ARG_WORKFLOW = "workflow";
	public static final String ARG_WORKFLOW_ID = "id";
	public static final String ARG_WORKFLOW_PROPERTIES = "properties";
	public static final String ARG_WORKFLOW_DOCUMENTS = "documents";
	private Map<String, Map<String,String>> taskProperties;
	List<String> allowDocList;
	Map<String, Map<String,String>> subjectTemplates;
	Map<String, String> subjectTemplatesForWorkflow;
	private TemplateService templateService;
	private String nodeVariable;
	private String templateEngine = "freemarker";
	private static final Log logger = LogFactory.getLog(DelegateTaskNotificationSender.class);
	private NodeOwnerDAO nodeOwnerDAO;
	protected Map<String,Boolean> markResending;

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
				if (entry.getValue() instanceof Serializable) {
					properties.put(entry.getKey(), (Serializable) entry.getValue());
				} else {
					properties.put(entry.getKey(), entry.getValue().toString());
				}
			}
			else
			{
				properties.put(entry.getKey(), null);
			}
		}
		return taskInfo;
	}
	
	private Serializable getWorkflowInfo(DelegateTask task) {
		HashMap<String, Object> workflowInfo = new HashMap<String, Object>();
		workflowInfo.put(ARG_WORKFLOW_ID, "activiti$" +task.getProcessInstanceId());
		workflowInfo.put(ARG_WORKFLOW_DOCUMENTS, getWorkflowDocuments(task));
		return workflowInfo;
	}

	public void sendNotification(DelegateTask task)
	{
		NotificationContext notificationContext = new NotificationContext();
		NodeRef template = getNotificationTemplate(task);
		String from = null;
		notificationContext.setTemplateArgs(getNotificationArgs(task));
		String notificationProviderName = EMailNotificationProvider.NAME;
		String subject = null;
		Set<String> authorities = new HashSet<String>();
		if(template!=null && nodeService.exists(template)) {
		    setBodyTemplate(notificationContext, template);
            String taskFormKey = (String) task.getVariableLocal("taskFormKey");
            if (markResending != null
                    && markResending.containsKey(taskFormKey)
                    && markResending.get(taskFormKey)
                    && isResending(task)) {
                subject = (String) nodeService.getProperty(template, DmsModel.PROP_TITLE_FOR_RESENDING);
            } else {
                String subjectTemplate = (String) nodeService.getProperty(template, DmsModel.PROP_SUBJECT_TEMPLATE);
                if (subjectTemplate != null) {
                    subject = (String) nodeService.getProperty(template, ContentModel.PROP_TITLE);

                    Map<String, Serializable> model = notificationContext.getTemplateArgs();
                    subjectTemplate = Utils.restoreFreemarkerVariables(subjectTemplate);
                    String processedSubjectLine = services.getTemplateService().processTemplateString(templateEngine,
                            subjectTemplate, model);

                    if (subject == null) {
                        subject = processedSubjectLine;
                    } else {
                        subject += ": " + processedSubjectLine;
                    }
                } else {
                    subject = (String) nodeService.getProperty(template, ContentModel.PROP_TITLE);
                }
                if (subject == null) {
                    ArrayList<Object> docsInfo = getWorkflowDocuments(task);
                    if (docsInfo.size() > 0 && subjectTemplates != null && subjectTemplates.containsKey(taskFormKey)) {
                        Map<String, String> taskSubjectTemplate = subjectTemplates.get(taskFormKey);
                        if (taskSubjectTemplate.containsKey(qNameConverter.mapQNameToName(nodeService.getType((NodeRef) docsInfo.get(0))))) {
                            HashMap<String, Object> model = new HashMap<>(1);
                            model.put(nodeVariable, docsInfo.get(0));
                            subject = services.getTemplateService().processTemplateString(templateEngine,
                                    taskSubjectTemplate.get(qNameConverter.mapQNameToName(nodeService.getType((NodeRef) docsInfo.get(0)))),
                                    model);
                        }
                    } /*else {
                        String processDef = task.getProcessDefinitionId();
                        String wfkey = processDef.substring(0, processDef.indexOf(":"));
                        if (subjectTemplatesForWorkflow != null && subjectTemplatesForWorkflow.containsKey(wfkey)) {
                            HashMap<String, Object> model = new HashMap<>(1);
                            model.put(nodeVariable, docsInfo.get(0));
                            subject = services.getTemplateService().processTemplateString(
                                    templateEngine, subjectTemplatesForWorkflow.get(wfkey), model);
                        }
                    }*/
                    if (subject == null) {
                        subject = task.getName();
                    }
                }
            }
			authorities.addAll(getRecipients(task, template, null));
		}
		notificationContext.setSubject(subject);
		for(String to : authorities) {
			notificationContext.addTo(to);
		}
		notificationContext.setAsyncNotification(getAsyncNotification());
		if (null != from) {
			notificationContext.setFrom(from);
		}
		// send
		logger.debug("before sent");
		services.getNotificationService().sendNotification(notificationProviderName, notificationContext);
		logger.debug("after sent");
	}
	
	public void setAllowDocList(List<String> allowDocList) {
    	this.allowDocList = allowDocList;
    }
	
    public void setSubjectTemplates(Map<String, Map<String,String>> subjectTemplates) {
        this.subjectTemplates = subjectTemplates;
    }
	
	public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}

	public void setTemplateEngine(String templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void setNodeVariable(String nodeVariable) {
		this.nodeVariable = nodeVariable;
	}
	
    public void setSubjectTemplatesForWorkflow(Map<String, String> subjectTemplatesForWorkflow) {
        this.subjectTemplatesForWorkflow = subjectTemplatesForWorkflow;
    }

	public NodeRef getNotificationTemplate(DelegateTask task)
	{
		String processDef = task.getProcessDefinitionId();
		String wfkey = "activiti$"+processDef.substring(0,processDef.indexOf(":"));
		String tkey = (String)task.getVariableLocal("taskFormKey");
		return getNotificationTemplate(wfkey, tkey);
	}
	
	/* Properties for tasks provided as map: "task name"-{"property1"-"value1", ...}
	* @param task subscribers
	*/
	public void setTaskProperties(Map<String, Map<String,String>> taskProperties) {
    	this.taskProperties = taskProperties;
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
				ExecutionEntity executionEntity = ((ExecutionEntity)task.getExecution()).getProcessInstance();
				NodeRef initiator = ((ActivitiScriptNode)executionEntity.getVariable("initiator")).getNodeRef();
				String initiator_name = (String) nodeService.getProperty(initiator, ContentModel.PROP_USERNAME);
				String sender = (String)task.getVariable("cwf_sender");
				logger.debug("!!!! user "+user);
				logger.debug("!!!! sender "+sender);
				logger.debug("!!!! initiator_name "+initiator_name);
				if (user != null && !user.equals(initiator_name) && !user.equals(sender))
				{
					authorities.add(user);
				}
			}
		}
		else
		{
			authorities.add(task.getAssignee());
		}
		logger.debug("authorities "+authorities);
	}

	protected void sendToInitiator(DelegateTask task, Set<String> authorities)
	{
		ExecutionEntity executionEntity = ((ExecutionEntity)task.getExecution()).getProcessInstance();
		NodeRef initiator = ((ActivitiScriptNode)executionEntity.getVariable("initiator")).getNodeRef();
		String initiator_name = (String) nodeService.getProperty(initiator, ContentModel.PROP_USERNAME);
		authorities.add(initiator_name);
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
	
	protected void sendToOwner(Set<String> authorities, NodeRef node)
	{
		String owner = nodeOwnerDAO.getOwner(node);
		authorities.add(owner);
	}
	
	public void setNodeOwnerDAO(NodeOwnerDAO nodeOwnerDAO) {
		this.nodeOwnerDAO = nodeOwnerDAO;
	}
	
	/**
	* Flag for marking mail as resending
	* @param markResending of values
	*/
	public void setMarkResending(Map<String,Boolean> markResending) {
    	this.markResending = markResending;
    }
	
	public ArrayList<Object> getWorkflowDocuments(DelegateTask task)
	{
		ArrayList<Object> docsInfo = new ArrayList<Object>();
		NodeRef workflowPackage = null;
		ExecutionEntity executionEntity = ((ExecutionEntity)task.getExecution()).getProcessInstance();
		ActivitiScriptNode scriptNode = (ActivitiScriptNode)executionEntity.getVariable("bpm_package");
		if (scriptNode != null)
		{
			workflowPackage = scriptNode.getNodeRef();
		}
        if (workflowPackage != null && nodeService.exists(workflowPackage)) {
            addAssocContains(docsInfo, nodeService.getChildAssocs(workflowPackage,
                    WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL));
            addAssocContains(docsInfo, nodeService.getChildAssocs(workflowPackage,
                    ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL));
        }
        return docsInfo;
	}

    private void addAssocContains(ArrayList<Object> docsInfo, List<ChildAssociationRef> children) {
        for (ChildAssociationRef child : children) {
            if (allowDocList == null) {
                docsInfo.add(child.getChildRef());
            } else {
                if (child!=null && qNameConverter!=null && nodeService!=null && allowDocList.contains(qNameConverter.mapQNameToName(nodeService.getType(child.getChildRef())))) {
                    docsInfo.add(child.getChildRef());
                }
            }
        }
    }

    public boolean isResending(DelegateTask task)
	{
		if(getWorkflowDocuments(task)!=null && getWorkflowDocuments(task).size()>0)
		{
		String query = "TYPE:\"wfgfam:familiarizeTask\" AND @wfm:document:\""+getWorkflowDocuments(task).get(0)+"\"";
		ResultSet nodes = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "fts-alfresco", query);
		return (nodes.length() > 0);
		}
		return false;
	}
	
}
