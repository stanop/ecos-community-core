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
package ru.citeck.ecos.workflow.activiti;


import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.alfresco.service.cmr.workflow.WorkflowService;

import java.util.List;

//import org.activiti.engine.impl.cmd.SetProcessDefinitionVersionCmd;

//http://docs.camunda.org/latest/guides/user-guide/#process-engine-process-versioning

// example

//      workflowId - activiti$<processInstanceId>, e.g., activiti$3233  -> processInstanceId == "3233",
//      workflowDefinitionId - activiti$<processDefinitionId>,
//      e.g., "activiti$maxxium-disagreement:24:6293" -> newProcessDefinitionId == "maxxium-disagreement:24:6293"
//      or newProcessDefinitionId == "activiti$maxxium-disagreement:24:6293" (там есть replace, убирающий activiti$)
//      processDefinitionVersion.migrateVersionForWorkflow("3233", 24, "maxxium-disagreement:24:6293");

public class ProcessDefinitionMigrateVersion extends BaseProcessorExtension {

    private WorkflowService workflowService;

    public void migrateVersionForWorkflow(String processInstanceId, int newVersion, String newProcessDefinitionId) {

        SetProcessDefinitionVersionCmd command =
                new SetProcessDefinitionVersionCmd(processInstanceId, newVersion, newProcessDefinitionId);
        ((ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine())
                .getProcessEngineConfiguration()
                .getCommandExecutor().execute(command);
    }

    public List<WorkflowInstance> getWorkflows(String workflowDefinitionId) {

        WorkflowInstanceQuery workflowInstanceQuery = new WorkflowInstanceQuery(workflowDefinitionId);
        List<WorkflowInstance> workflows = workflowService.getWorkflows(workflowInstanceQuery);

        return workflows;
    }

    public void changeVersionForAllInstances(String oldProcessDefinitionId, int newVersion, String newProcessDefinitionId) {

        if(newProcessDefinitionId != null){
            newProcessDefinitionId = newProcessDefinitionId.replace("activiti$", "");
        }

        if (oldProcessDefinitionId != null && !"".equals(oldProcessDefinitionId)) {
            //find all active and inactive workflowInstances by oldProcessDefinitionId
            List<WorkflowInstance> workflows = getWorkflows(oldProcessDefinitionId);
            if (workflows != null && workflows.size() > 0) {
                for (int i = 0; i < workflows.size(); i++) {
                    WorkflowInstance workflowInstance = workflows.get(i);
                    String workflowId = workflowInstance.getId().replace("activiti$", "");
                    migrateVersionForWorkflow(workflowId, newVersion, newProcessDefinitionId);
                }
            }
        }
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

}
