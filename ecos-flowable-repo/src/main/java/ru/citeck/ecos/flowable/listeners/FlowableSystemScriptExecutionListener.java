package ru.citeck.ecos.flowable.listeners;


import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.flowable.engine.common.api.delegate.Expression;
import org.flowable.engine.common.api.variable.VariableContainer;
import org.flowable.engine.delegate.DelegateExecution;

/**
 * @author Roman Makarskiy
 */
public class FlowableSystemScriptExecutionListener extends FlowableScriptExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        super.setRunAs(new FlowableStringExpression(AuthenticationUtil.getAdminUserName()));
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
