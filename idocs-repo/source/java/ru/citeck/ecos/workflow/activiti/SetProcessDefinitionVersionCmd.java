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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public class SetProcessDefinitionVersionCmd implements Command<Void>, Serializable {
    private static final long serialVersionUID = 1L;
    private final String processInstanceId;
    private final Integer processDefinitionVersion;
    private String newProcessDefinitionId = "";

    private static Log logger = LogFactory.getLog(SetProcessDefinitionVersionCmd.class);

    public SetProcessDefinitionVersionCmd(String processInstanceId, Integer processDefinitionVersion) {
        if (processInstanceId != null && processInstanceId.length() >= 1) {
            if (processDefinitionVersion == null) {
                throw new ActivitiIllegalArgumentException("The process definition version is mandatory, but \'null\' has been provided.");
            } else if (processDefinitionVersion.intValue() < 1) {
                throw new ActivitiIllegalArgumentException("The process definition version must be positive, but \'" + processDefinitionVersion + "\' has been provided.");
            } else {
                this.processInstanceId = processInstanceId;
                this.processDefinitionVersion = processDefinitionVersion;
            }
        } else {
            throw new ActivitiIllegalArgumentException("The process instance id is mandatory, but \'" + processInstanceId + "\' has been provided.");
        }
    }

    public SetProcessDefinitionVersionCmd(String processInstanceId, Integer processDefinitionVersion, String processDefinitionId) {
        if (processInstanceId != null && processInstanceId.length() >= 1) {
            if (processDefinitionVersion == null) {
                throw new ActivitiIllegalArgumentException("The process definition version is mandatory, but \'null\' has been provided.");
            } else if (processDefinitionVersion.intValue() < 1) {
                throw new ActivitiIllegalArgumentException("The process definition version must be positive, but \'" + processDefinitionVersion + "\' has been provided.");
            } else if (newProcessDefinitionId != null && newProcessDefinitionId.length() >= 1) {
                throw new ActivitiIllegalArgumentException("The new process definition id is mandatory, but \'null\' has been provided.");
            } else {
                this.processInstanceId = processInstanceId;
                this.processDefinitionVersion = processDefinitionVersion;
                this.newProcessDefinitionId = processDefinitionId;
            }
        } else {
            throw new ActivitiIllegalArgumentException("The process instance id is mandatory, but \'" + processInstanceId + "\' has been provided.");
        }
    }

    public Void execute(CommandContext commandContext) {

        ExecutionEntityManager executionManager = commandContext.getExecutionEntityManager();
        ExecutionEntity processInstance = executionManager.findExecutionById(processInstanceId);

        if (processInstance == null) {

            HistoricProcessInstanceEntityManager historicProcessInstanceManager = commandContext.getHistoricProcessInstanceEntityManager();
            // if (historicProcessInstanceManager.isHistoryEnabled()) {
            HistoricProcessInstanceEntity historicProcessInstance = historicProcessInstanceManager.findHistoricProcessInstance(processInstanceId);
            if (historicProcessInstance != null) {
                if(!"".equals(newProcessDefinitionId)) {

                    historicProcessInstance.setProcessDefinitionId(newProcessDefinitionId);
                    findHistoricTaskInstancesByProcessInstanceId(commandContext, processInstanceId, newProcessDefinitionId);
                }
            } else {
                throw new ActivitiObjectNotFoundException("historicProcessInstance is null", HistoricProcessInstanceEntity.class);
            }
            // }

            // throw new ActivitiObjectNotFoundException("No active process instance found for id = \'" + this.processInstanceId + "\'.", ProcessInstance.class);

        } else if (!processInstance.isProcessInstanceType()) {
            throw new ActivitiIllegalArgumentException("A process instance id is required, but the provided id \'" + this.processInstanceId + "\' " + "points to a child execution of process instance " + "\'" + processInstance.getProcessInstanceId() + "\'. " + "Please invoke the " + this.getClass().getSimpleName() + " with a root execution id.");
        } else {


            DeploymentManager deploymentCache = Context.getProcessEngineConfiguration().getDeploymentManager();

            String key = this.newProcessDefinitionId.split(":")[0]; //"maxxium-sales-contract"
            //find processDefinition by key and version
            ProcessDefinitionEntity newProcessDefinition = deploymentCache.findDeployedProcessDefinitionByKeyAndVersion(key, this.processDefinitionVersion);
            if(newProcessDefinition != null) {
                this.validateAndSwitchVersionOfExecution(commandContext, processInstance, newProcessDefinition);

                commandContext.getHistoryManager().recordProcessDefinitionChange(this.processInstanceId, newProcessDefinition.getId());

                // switch the historic process instance to the new process definition version
                HistoricProcessInstanceEntityManager historicProcessInstanceManager = commandContext.getHistoricProcessInstanceEntityManager();
                HistoryManager historyManager= commandContext.getHistoryManager();
                if (historyManager.isHistoryEnabled()) {
                    HistoricProcessInstanceEntity historicProcessInstance = historicProcessInstanceManager.findHistoricProcessInstance(processInstanceId);
                    historicProcessInstance.setProcessDefinitionId(newProcessDefinition.getId());
                }

                // switch all sub-executions of the process instance to the new process definition version
                List<ExecutionEntity> childExecutions = executionManager
                        .findChildExecutionsByProcessInstanceId(processInstanceId);
                for (ExecutionEntity executionEntity : childExecutions) {
                    validateAndSwitchVersionOfExecution(commandContext, executionEntity, newProcessDefinition);
                }


            }else{
                throw new ActivitiObjectNotFoundException("newProcessDefinition is null", ProcessDefinitionEntity.class);
            }
        }
        return null;
    }

    protected void validateAndSwitchVersionOfExecution(CommandContext commandContext, ExecutionEntity execution, ProcessDefinitionEntity newProcessDefinition) {

            execution.setProcessDefinition(newProcessDefinition);

        // and change possible existing tasks (as the process definition id is stored there too)
        List<TaskEntity> tasks = commandContext.getTaskEntityManager().findTasksByExecutionId(execution.getId());
        for (TaskEntity taskEntity : tasks) {
            taskEntity.setProcessDefinitionId(newProcessDefinition.getId());
        }

        findHistoricTaskInstancesByProcessInstanceId(commandContext, processInstanceId, newProcessDefinition.getId());

    }

    public void findHistoricTaskInstancesByProcessInstanceId(CommandContext commandContext, String processInstanceId, String newProcessDefinitionId) {

        List taskInstanceIds = commandContext.getDbSqlSession().selectList("selectHistoricTaskInstanceIdsByProcessInstanceId", processInstanceId);
        HistoricTaskInstanceEntityManager historicTaskInstanceEntityManager = commandContext.getHistoricTaskInstanceEntityManager();

        Iterator i$ = taskInstanceIds.iterator();

        while(i$.hasNext()) {
            String taskInstanceId = (String)i$.next();
            HistoricTaskInstanceEntity taskEntity = historicTaskInstanceEntityManager.findHistoricTaskInstanceById(taskInstanceId);
            taskEntity.setProcessDefinitionId(newProcessDefinitionId);
        }
    }

}
