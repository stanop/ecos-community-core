package ru.citeck.ecos.flowable.temp;

import org.flowable.engine.delegate.DelegateTask;
import org.flowable.engine.delegate.Expression;
import org.flowable.engine.delegate.TaskListener;

/**
 * Variable pull task listener
 */
public class FlowableVariablePull implements TaskListener {

    private String executionVariable;
    private String taskVariable;
    private String variable;
    private boolean ifNotNull = false;

    @Override
    public void notify(DelegateTask task) {
        Object value = task.getExecution().getVariable(executionVariable != null ? executionVariable : variable);
        if(!ifNotNull || value != null) {
            task.setVariableLocal(taskVariable != null ? taskVariable : variable, value);
        }

    }

    public void setExecutionVariable(String executionVariable) {
        this.executionVariable = executionVariable;
    }

    public void setTaskVariable(String taskVariable) {
        this.taskVariable = taskVariable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public void setIfNotNull(boolean ifNotNull) {
        this.ifNotNull = ifNotNull;
    }

    public void setExecutionVariableExpr(Expression executionVariable) {
        this.executionVariable = executionVariable.getExpressionText();
    }

    public void setTaskVariableExpr(Expression taskVariable) {
        this.taskVariable = taskVariable.getExpressionText();
    }

    public void setVariableExpr(Expression variable) {
        this.variable = variable.getExpressionText();
    }

    public void setIfNotNullExpr(Expression ifNotNull) {
        this.ifNotNull = Boolean.valueOf(ifNotNull.getExpressionText());
    }
}
