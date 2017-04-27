package ru.citeck.ecos.workflow.activiti.cmd;

import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.activiti.engine.impl.cmd.NeedsActiveExecutionCmd;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Pavel Simonov
 */
public class AddParallelExecutionInstanceCmd extends NeedsActiveExecutionCmd<Boolean> {

    private static final String NUMBER_OF_INSTANCES = "nrOfInstances";
    private static final String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";
    private static final String LOOP_COUNTER = "loopCounter";

    private String childExecutionId;
    private List<?> additionalElements;

    public AddParallelExecutionInstanceCmd(String executionId, String childExecutionId, List<?> additionalElements) {
        super(executionId);
        this.childExecutionId = childExecutionId;
        this.additionalElements = additionalElements;
    }

    @Override
    protected Boolean execute(CommandContext commandContext, ExecutionEntity processExecution) {
        return addParallelExecution(processExecution, childExecutionId, additionalElements);
    }

    static boolean addParallelExecution(ExecutionEntity processExecution,
                                        String childExecutionId, List<?> additionalElements) {

        ExecutionEntity execution = processExecution.findExecution(childExecutionId);
        if (execution == null) {
            return false;
        }

        ActivityImpl activity = execution.getActivity();
        if (activity == null) {
            return false;
        }

        int numberOfInstances = (Integer) execution.getVariableLocal(NUMBER_OF_INSTANCES);
        int numberOfActiveInstances = (Integer) execution.getVariableLocal(NUMBER_OF_ACTIVE_INSTANCES);
        execution.setVariableLocal(NUMBER_OF_INSTANCES, numberOfInstances + additionalElements.size());
        execution.setVariableLocal(NUMBER_OF_ACTIVE_INSTANCES, numberOfActiveInstances + additionalElements.size());

        ParallelMultiInstanceBehavior activityBehavior = (ParallelMultiInstanceBehavior) activity.getActivityBehavior();

        List<ActivityExecution> additionalExecutions = new ArrayList<>();
        boolean isExtraScopeNeeded = isExtraScopeNeeded(activityBehavior);
        for (int i = 0; i < additionalElements.size(); i++) {
            additionalExecutions.add(createExecution(execution, isExtraScopeNeeded));
        }

        Collection collection = (Collection) activityBehavior.getCollectionExpression().getValue(processExecution);
        collection.addAll(additionalElements);

        String collectionElementVariable = activityBehavior.getCollectionElementVariable();

        for (int i = 0; i < additionalExecutions.size(); i++) {
            ActivityExecution concurrentExecution = additionalExecutions.get(i);
            if (concurrentExecution.isActive() && !concurrentExecution.isEnded()
                    && concurrentExecution.getParent().isActive()
                    && !concurrentExecution.getParent().isEnded()) {
                concurrentExecution.setVariableLocal(LOOP_COUNTER, numberOfInstances + i);
                concurrentExecution.setVariableLocal(collectionElementVariable, additionalElements.get(i));
                concurrentExecution.executeActivity(activity);
            }
        }

        return true;
    }

    private static ActivityExecution createExecution(ExecutionEntity parent, boolean isExtraScopeNeeded) {
        ActivityExecution concurrentExecution = parent.createExecution();
        concurrentExecution.setActive(true);
        concurrentExecution.setConcurrent(true);
        concurrentExecution.setScope(false);

        // In case of an embedded subprocess, and extra child execution is required
        // Otherwise, all child executions would end up under the same parent,
        // without any differentation to which embedded subprocess they belong
        if (isExtraScopeNeeded) {
            ActivityExecution extraScopedExecution = concurrentExecution.createExecution();
            extraScopedExecution.setActive(true);
            extraScopedExecution.setConcurrent(false);
            extraScopedExecution.setScope(true);
            concurrentExecution = extraScopedExecution;
        }

        return concurrentExecution;
    }

    private static boolean isExtraScopeNeeded(ParallelMultiInstanceBehavior activityBehavior) {
        // special care is needed when the behavior is an embedded subprocess (not very clean, but it works)
        AbstractBpmnActivityBehavior inner = activityBehavior.getInnerActivityBehavior();
        return inner instanceof SubProcessActivityBehavior;
    }
}
