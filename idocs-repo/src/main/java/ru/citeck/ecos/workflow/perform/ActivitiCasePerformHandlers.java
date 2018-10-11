package ru.citeck.ecos.workflow.perform;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

public class ActivitiCasePerformHandlers implements Serializable {

    private static final long serialVersionUID = -2309572351324327537L;

    private CasePerformWorkflowHandler impl;

    public void onWorkflowStart(ExecutionEntity execution) {
        impl.onWorkflowStart(new ActivitiPerformExecution(execution));
    }

    public void onRepeatIterationGatewayStarted(ExecutionEntity execution) {
        impl.onRepeatIterationGatewayStarted(new ActivitiPerformExecution(execution));
    }

    public void onBeforePerformingFlowTake(ExecutionEntity execution) {
        impl.onBeforePerformingFlowTake(new ActivitiPerformExecution(execution));
    }

    public void onSkipPerformingGatewayStarted(ExecutionEntity execution) {
        impl.onSkipPerformingGatewayStarted(new ActivitiPerformExecution(execution));
    }

    /* skip way */

    public void onSkipPerformingFlowTake(ExecutionEntity execution) {
        impl.onSkipPerformingFlowTake(new ActivitiPerformExecution(execution));
    }

    /* skip way */

    /* perform way */

    public void onPerformingFlowTake(ExecutionEntity execution) {
        impl.onPerformingFlowTake(new ActivitiPerformExecution(execution));

    }

    public void onBeforePerformTaskCreated(ExecutionEntity execution) {
        impl.onBeforePerformTaskCreated(new ActivitiPerformExecution(execution));
    }

    public void onPerformTaskCreated(ExecutionEntity execution, TaskEntity task) {
        impl.onPerformTaskCreated(new ActivitiPerformExecution(execution),
                                  new ActivitiPerformTask(task));
    }

    public void onPerformTaskAssigned(ExecutionEntity execution, TaskEntity task) {
        impl.onPerformTaskAssigned(new ActivitiPerformExecution(execution),
                                   new ActivitiPerformTask(task));
    }

    public void onPerformTaskCompleted(ExecutionEntity execution, TaskEntity task) {
        impl.onPerformTaskCompleted(new ActivitiPerformExecution(execution),
                                    new ActivitiPerformTask(task));
    }

    public void onAfterPerformingFlowTake(ExecutionEntity execution) {
        impl.onAfterPerformingFlowTake(new ActivitiPerformExecution(execution));
    }

    public void onRepeatPerformingGatewayStarted(ExecutionEntity execution) {
        impl.onRepeatPerformingGatewayStarted(new ActivitiPerformExecution(execution));
    }

    /* perform way */

    public void onWorkflowEnd(ExecutionEntity execution) {
        impl.onWorkflowEnd(new ActivitiPerformExecution(execution));
    }

    @Autowired
    public void setImpl(CasePerformWorkflowHandler impl) {
        this.impl = impl;
    }
}
