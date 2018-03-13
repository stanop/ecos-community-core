package ru.citeck.ecos.flowable.example;

import org.flowable.engine.common.api.delegate.Expression;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * @author Roman Makarskiy
 */
public class FlowableVariablePush implements TaskListener {

    private String executionVariable;
    private String taskVariable;
    private String variable;
    private boolean ifNotNull = false;

    @Override
    public void notify(DelegateTask task) {

        Object value = task.getVariable(taskVariable != null ? taskVariable : variable);
        if(!ifNotNull || value != null) {
            task.setVariableLocal(executionVariable != null ? executionVariable : variable, value);
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
