package ru.citeck.ecos.flowable.example;

import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.flowable.activiti.delegates.FlowablePerformExecution;
import ru.citeck.ecos.flowable.activiti.delegates.FlowablePerformTask;
import ru.citeck.ecos.workflow.perform.CasePerformWorkflowHandler;

import java.io.Serializable;

public class FlowableCasePerformWorkflowHandler implements Serializable {

    private static final long serialVersionUID = -2309572351324327537L;

    private CasePerformWorkflowHandler impl;

    public void onWorkflowStart(ExecutionEntity execution) {
        impl.onWorkflowStart(new FlowablePerformExecution(execution));
    }

    public void onRepeatIterationGatewayStarted(ExecutionEntity execution) {
        impl.onRepeatIterationGatewayStarted(new FlowablePerformExecution(execution));
    }

    public void onBeforePerformingFlowTake(ExecutionEntity execution) {
        impl.onBeforePerformingFlowTake(new FlowablePerformExecution(execution));
    }

    public void onSkipPerformingGatewayStarted(ExecutionEntity execution) {
        impl.onSkipPerformingGatewayStarted(new FlowablePerformExecution(execution));
    }

    /* skip way */

    public void onSkipPerformingFlowTake(ExecutionEntity execution) {
        impl.onSkipPerformingFlowTake(new FlowablePerformExecution(execution));
    }

    /* skip way */

    /* perform way */

    public void onPerformingFlowTake(ExecutionEntity execution) {
        impl.onPerformingFlowTake(new FlowablePerformExecution(execution));
    }

    public void onBeforePerformTaskCreated(ExecutionEntity execution) {
        impl.onBeforePerformTaskCreated(new FlowablePerformExecution(execution));
    }

    public void onPerformTaskCreated(ExecutionEntity execution, TaskEntity task) {
        impl.onPerformTaskCreated(new FlowablePerformExecution(execution),
                                  new FlowablePerformTask(task));
    }

    public void onPerformTaskAssigned(ExecutionEntity execution, TaskEntity task) {
        impl.onPerformTaskAssigned(new FlowablePerformExecution(execution),
                                   new FlowablePerformTask(task));
    }

    public void onPerformTaskCompleted(ExecutionEntity execution, TaskEntity task) {
        impl.onPerformTaskCompleted(new FlowablePerformExecution(execution),
                                    new FlowablePerformTask(task));
    }

    public void onAfterPerformingFlowTake(ExecutionEntity execution) {
        impl.onAfterPerformingFlowTake(new FlowablePerformExecution(execution));
    }

    public void onRepeatPerformingGatewayStarted(ExecutionEntity execution) {
        impl.onRepeatPerformingGatewayStarted(new FlowablePerformExecution(execution));
    }

    /* perform way */

    public void onWorkflowEnd(ExecutionEntity execution) {
        impl.onWorkflowEnd(new FlowablePerformExecution(execution));
    }

    @Autowired
    public void setImpl(CasePerformWorkflowHandler impl) {
        this.impl = impl;
    }
}
