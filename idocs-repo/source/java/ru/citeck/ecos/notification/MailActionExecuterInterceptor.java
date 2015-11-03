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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.model.ContentModel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Date;

import ru.citeck.ecos.model.WorkflowMirrorModel;
import ru.citeck.ecos.model.NotificationLoggingModel;
import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.node.NodeInfoFactory;
import ru.citeck.ecos.workflow.mirror.WorkflowMirrorService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.springframework.mail.MailSendException;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;

import org.alfresco.service.namespace.RegexQNamePattern;

public class MailActionExecuterInterceptor implements MethodInterceptor {
    
    private static final Log logger = LogFactory.getLog(MailActionExecuterInterceptor.class);
    private NodeService nodeService;
	private NodeInfoFactory nodeInfoFactory;
	private AuthorityService authorityService;
	private WorkflowMirrorService workflowMirrorService;
	private NodeRef notificationLoggingRoot;
	private QName notificationLoggingAssoc;
    private WorkflowService workflowService;

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {

		if("execute".equals(methodInvocation.getMethod().getName()))
		{
			boolean isSent = false;
			Object[] args = methodInvocation.getArguments();
			Action act = (Action)args[0];
			Date currentDate = new Date();
			try {
				Object result = methodInvocation.proceed();
				isSent = true;
				//createLoggingItem(act, currentDate, true);
				logger.debug("COMPLETED!!!");
				return result;

			} catch (MailSendException e) {
					isSent = false;
					//createLoggingItem(act, currentDate, false);
					logger.error("MailSendException COMPLETED!!!");
				throw e;
			}  catch (NullPointerException e) {
					isSent = false;
					//createLoggingItem(act, currentDate, false);
					logger.error("NullPointerException COMPLETED!!!");
				throw e;
			} catch (Exception e) {
					isSent = false;
					//createLoggingItem(act, currentDate, false);
					logger.error("Exception COMPLETED!!!");
				throw e;
			}
			finally
			{
				createLoggingItem(act, currentDate, isSent);
			}
		}
		return methodInvocation.proceed();
	}
	
	private void createLoggingItem(Action act, Date currentDate, boolean isSent)
	{
		logger.debug("!!!!createLoggingItem");
		NodeInfo nodeInfo = nodeInfoFactory.createNodeInfo();
		Map<String, Serializable> parameterValues = act.getParameterValues();
		logger.debug("act.getParameterValue "+act.getParameterValues());
		Map<String, Serializable> template_modelParameterValue = (Map<String, Serializable>)act.getParameterValue("template_model");
		if(template_modelParameterValue!=null)
		{
			Map<String, Serializable> argsParameterValues = (Map<String, Serializable>)template_modelParameterValue.get("args");
			if(argsParameterValues!=null)
			{
				String autoSent = (String)argsParameterValues.get("autoSent");
				if("true".equals(autoSent))
				{
					nodeInfo.setProperty(NotificationLoggingModel.PROP_EVENT_TYPE, NotificationLoggingModel.EventType.Other);
				}
				else
				{
					nodeInfo.setProperty(NotificationLoggingModel.PROP_EVENT_TYPE, NotificationLoggingModel.EventType.NotificationForTask);
				}
				Map<String, Serializable> taskParameterValues = (Map<String, Serializable>)argsParameterValues.get("task");
				Map<String, Serializable> workflowParameterValues = (Map<String, Serializable>)argsParameterValues.get("workflow");
				Map<String, Serializable> sentOnCancelWFValues = (Map<String, Serializable>)argsParameterValues.get("sentOnCancelWF");
				if(taskParameterValues!=null)
				{
					NodeRef task = workflowMirrorService.getTaskMirror("activiti$"+(String)taskParameterValues.get("id"));
					if(task==null)
					{
						task = workflowMirrorService.getTaskMirror((String)taskParameterValues.get("id"));
					}
					if(task!=null && nodeService.exists(task))
					{
						nodeInfo.setProperty(NotificationLoggingModel.PROP_NOTIFICATION_TASK, task);
						nodeInfo.setProperty(NotificationLoggingModel.PROP_NOTIFICATION_DOCUMENT, nodeService.getProperty(task, WorkflowMirrorModel.PROP_DOCUMENT));
					}
				}
				else if (workflowParameterValues!=null)
				{
					WorkflowInstance wf = workflowService.getWorkflowById("activiti$"+workflowParameterValues.get("id"));
					logger.debug("wf "+wf);
					if(wf!=null)
					{
						WorkflowDefinition definition = wf.getDefinition();
						logger.debug("definition "+definition);
						if(definition!=null)
						{
							logger.debug("definition.getTitle() "+definition.getTitle());
							nodeInfo.setProperty(NotificationLoggingModel.PROP_NOTIFICATION_WOKFLOW_ID,  definition.getTitle());
						}
					}

					nodeInfo.setProperty(NotificationLoggingModel.PROP_NOTIFICATION_DOCUMENT, workflowParameterValues.get("documents"));
				}
			}
		}
		else
		{
			if(act.getParameterValue("document")!=null)
			{
				nodeInfo.setProperty(NotificationLoggingModel.PROP_NOTIFICATION_DOCUMENT, act.getParameterValue("document"));
				nodeInfo.setProperty(NotificationLoggingModel.PROP_EVENT_TYPE, NotificationLoggingModel.EventType.NotificationForTask);
			}
		}
		
		nodeInfo.setProperty(NotificationLoggingModel.PROP_NOTIFICATION_DATE, currentDate);
		logger.debug("currentDate "+currentDate);
		String mailTo = (String)act.getParameterValue("to");
		List mailToMany = (List)act.getParameterValue("to_many");
		logger.debug("mailTo "+mailTo);
		logger.debug("mailToMany "+mailToMany);
		if(mailTo!=null)
		{
			NodeRef recipient = authorityService.getAuthorityNodeRef(mailTo);
			nodeInfo.setProperty(NotificationLoggingModel.PROP_NOTIFICATION_RECIPIENT, recipient);
			logger.debug("mailTo "+mailTo+" nodeService.getType(recipient) "+nodeService.getType(recipient));
			if(nodeService.getType(recipient).equals(ContentModel.TYPE_PERSON))
			{
				nodeInfo.setProperty(NotificationLoggingModel.PROP_NOTIFICATION_EMAIL, nodeService.getProperty(recipient, ContentModel.PROP_EMAIL));
			}
			else if(nodeService.getType(recipient).equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
			{
				nodeInfo.setProperty(NotificationLoggingModel.PROP_NOTIFICATION_EMAIL, getEmailsForGroupMembers(recipient).toString());
			}
		}
		else if(mailToMany!=null)
		{
			LinkedList<NodeRef> recipients = new LinkedList<NodeRef>();
			ArrayList<String> emails = new ArrayList<String>();
			for(int i=0; i<mailToMany.size(); i++)
			{
				NodeRef recipient = authorityService.getAuthorityNodeRef((String)mailToMany.get(i));
				logger.debug("mailToMany "+mailToMany+" nodeService.getType(recipient) "+nodeService.getType(recipient));
				logger.debug("recipient "+recipient);
				if(recipient!=null && nodeService.exists(recipient))
				{
					recipients.add(recipient);
					if(nodeService.getType(recipient).equals(ContentModel.TYPE_PERSON))
					{
						emails.add((String)nodeService.getProperty(recipient, ContentModel.PROP_EMAIL));
					}
					else if(nodeService.getType(recipient).equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
					{
						emails.addAll(getEmailsForGroupMembers(recipient));
					}
				}
			}
			nodeInfo.setProperty(NotificationLoggingModel.PROP_NOTIFICATION_RECIPIENT, recipients);
			nodeInfo.setProperty(NotificationLoggingModel.PROP_NOTIFICATION_EMAIL, emails.toString());
			logger.debug("recipients "+recipients);
		}
		nodeInfo.setProperty(NotificationLoggingModel.PROP_NOTIFICATION_SUBJECT, (String)act.getParameterValue("subject"));
		nodeInfo.setProperty(NotificationLoggingModel.PROP_IS_NOTIFICATION_SENT, isSent);

		if(mailToMany!=null || mailTo!=null)
		{
			QName assocQName = QName.createQName(notificationLoggingAssoc.getNamespaceURI(), act.getId());
			ChildAssociationRef notificationLogItemRef = nodeService.createNode(notificationLoggingRoot, notificationLoggingAssoc, assocQName, NotificationLoggingModel.TYPE_NOTIFICATION_LOG_ITEM);
			nodeInfoFactory.persist(notificationLogItemRef.getChildRef(), nodeInfo, true);
			logger.debug("notificationLogItemRef.getChildRef()! "+notificationLogItemRef.getChildRef());
			logger.debug("nodeInfo! "+nodeInfo);
		}

	}
	
	public ArrayList<String> getEmailsForGroupMembers(NodeRef recipient)
	{
		ArrayList<String> emails = new ArrayList<String>();
		List<ChildAssociationRef> memberAssocs = nodeService.getChildAssocs(recipient, ContentModel.ASSOC_MEMBER, RegexQNamePattern.MATCH_ALL);
		for (ChildAssociationRef memberAssoc : memberAssocs)
		{
			NodeRef memberNode = memberAssoc.getChildRef();
			if(nodeService.getType(memberNode).equals(ContentModel.TYPE_PERSON))
			{
				emails.add((String)nodeService.getProperty(memberNode, ContentModel.PROP_EMAIL));
			}
			else if(nodeService.getType(memberNode).equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
			{
				emails.addAll(getEmailsForGroupMembers(memberNode));
			}
		}
		return emails;
	}
	
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
	
    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }
	

	public void setNodeInfoFactory(NodeInfoFactory nodeInfoFactory) {
		this.nodeInfoFactory = nodeInfoFactory;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setWorkflowMirrorService(WorkflowMirrorService workflowMirrorService) {
		this.workflowMirrorService = workflowMirrorService;
	}

	public void setNotificationLoggingRoot(NodeRef notificationLoggingRoot) {
		this.notificationLoggingRoot = notificationLoggingRoot;
	}

	public void setNotificationLoggingAssoc(QName notificationLoggingAssoc) {
		this.notificationLoggingAssoc = notificationLoggingAssoc;
	}
}
