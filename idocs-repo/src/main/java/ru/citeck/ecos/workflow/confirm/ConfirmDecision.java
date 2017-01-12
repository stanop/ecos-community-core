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
package ru.citeck.ecos.workflow.confirm;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.model.ConfirmWorkflowModel;

public class ConfirmDecision {

	private Set<NodeRef> confirmVersions;
	private String confirmTaskId;
	private String confirmerRole;
	private String confirmerUser;
	private String confirmOutcome;
	private String confirmComment;
	private Date confirmDate;
	
	@SuppressWarnings("unchecked")
	public ConfirmDecision(NodeRef confirmDecision, NodeService nodeService,
			WorkflowService workflowService) 
	{
		Map<QName,Serializable> decisionProps = nodeService.getProperties(confirmDecision);
		
		// confirmVersions:
		List<NodeRef> confirmVersionsValue = (List<NodeRef>) decisionProps.get(ConfirmWorkflowModel.PROP_CONFIRM_VERSIONS);
		confirmVersions = new HashSet<NodeRef>(confirmVersionsValue.size());
		confirmVersions.addAll(confirmVersionsValue);
		
		// task id:
		confirmTaskId = (String) decisionProps.get(ConfirmWorkflowModel.PROP_CONFIRM_TASK_ID);
		
		// task properties:
		WorkflowTask confirmTask = workflowService.getTaskById(confirmTaskId);
		Map<QName,Serializable> taskProps = confirmTask.getProperties();
		
		// role:
		/*List<NodeRef> pooledActors = (List<NodeRef>) taskProps.get(WorkflowModel.ASSOC_POOLED_ACTORS);
		if(pooledActors.size() > 0) {
			confirmerRole = pooledActors.get(0).toString();
		}*/
		confirmerRole = (String) decisionProps.get(ConfirmWorkflowModel.PROP_CONFIRM_ROLE);
		
		// user:
		confirmerUser = (String) taskProps.get(ContentModel.PROP_OWNER);
		
		// outcome:
		QName outcomeProperty = (QName) taskProps.get(WorkflowModel.PROP_OUTCOME_PROPERTY_NAME);
		if(outcomeProperty == null) {
			outcomeProperty = WorkflowModel.PROP_OUTCOME;
		}
		confirmOutcome = (String) taskProps.get(outcomeProperty);
		
		// comment:
		confirmComment = (String) taskProps.get(WorkflowModel.PROP_COMMENT);
		
		// date:
		confirmDate = (Date) taskProps.get(WorkflowModel.PROP_COMPLETION_DATE);
		
	}

	public Set<NodeRef> getConfirmVersions() {
		return confirmVersions;
	}

	public String getConfirmTaskId() {
		return confirmTaskId;
	}

	public String getConfirmerRole() {
		return confirmerRole;
	}

	public String getConfirmerUser() {
		return confirmerUser;
	}

	public String getConfirmOutcome() {
		return confirmOutcome;
	}

	public String getConfirmComment() {
		return confirmComment;
	}

	public Date getConfirmDate() {
		return confirmDate;
	}

}
