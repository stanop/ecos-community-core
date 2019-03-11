package ru.citeck.ecos.flowable.listeners;


import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.api.variable.VariableContainer;
import org.flowable.engine.delegate.DelegateExecution;

/**
 * @author Roman Makarskiy
 */
public class FlowableSystemScriptExecutionListener extends FlowableScriptExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        super.setRunAs(new FlowableStringExpression(AuthenticationUtil.getSystemUserName()));
        super.notify(execution);
    }
}

class FlowableStringExpression implements Expression {

    private String value;

    public FlowableStringExpression(String value) {
        this.value = value;
    }

    @Override
    public Object getValue(VariableContainer variableContainer) {
        return value;
    }

    @Override
    public void setValue(Object o, VariableContainer variableContainer) {

    }

    @Override
    public String getExpressionText() {
        return value;
    }
}
