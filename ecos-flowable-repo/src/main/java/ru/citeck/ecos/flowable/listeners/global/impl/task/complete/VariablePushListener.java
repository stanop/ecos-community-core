package ru.citeck.ecos.flowable.listeners.global.impl.task.complete;

import org.flowable.engine.delegate.DelegateTask;
import ru.citeck.ecos.flowable.listeners.global.GlobalCompleteTaskListener;

/**
 * Variable push listener
 */
public class VariablePushListener implements GlobalCompleteTaskListener {

    /**
     * Execution variable
     */
    private String executionVariable;

    /**
     * Task variable
     */
    private String taskVariable;

    /**
     * Variable
     */
    private String variable;

    /**
     * If not null flag
     */
    private boolean ifNotNull = false;

    /**
     * Notify
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        Object value = delegateTask.getVariable(taskVariable != null ? taskVariable : variable);
        if(!ifNotNull || value != null) {
            delegateTask.getExecution().setVariableLocal(executionVariable != null ? executionVariable : variable, value);
        }
    }

    /**
     * Set execution variable
     * @param executionVariable Execution variable
     */
    public void setExecutionVariable(String executionVariable) {
        this.executionVariable = executionVariable;
    }

    /**
     * Set task variable
     * @param taskVariable Task variable
     */
    public void setTaskVariable(String taskVariable) {
        this.taskVariable = taskVariable;
    }

    /**
     * Set variable
     * @param variable Variable
     */
    public void setVariable(String variable) {
        this.variable = variable;
    }

    /**
     * Set if not null flag
     * @param ifNotNull If not null flag
     */
    public void setIfNotNull(boolean ifNotNull) {
        this.ifNotNull = ifNotNull;
    }
}
