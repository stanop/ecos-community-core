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
package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import ru.citeck.ecos.model.CiteckWorkflowModel;

public class SetPriorityWorkflowListener extends AbstractExecutionListener {

	private NodeService nodeService;

	@Override
	protected void notifyImpl(DelegateExecution execution) throws Exception {
		NodeRef docRef = ListenerUtils.getDocument(execution, nodeService);
		if (docRef == null)
			return;

		Object bpmPriorityObj = execution.getVariable("bpm_workflowPriority");
		Integer bpmPriority = null;
		if (bpmPriorityObj instanceof Integer)
			bpmPriority = (Integer)bpmPriorityObj;

		if (bpmPriority == null)
			return;

		Object docPriorityObj = nodeService.getProperty(docRef, CiteckWorkflowModel.PROP_PRIORITY);
		Integer docPriority = null;
		if (docPriorityObj instanceof Integer)
			docPriority = (Integer)docPriorityObj;
		if (!bpmPriority.equals(docPriority))
			nodeService.setProperty(docRef, CiteckWorkflowModel.PROP_PRIORITY, bpmPriority);
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

}
