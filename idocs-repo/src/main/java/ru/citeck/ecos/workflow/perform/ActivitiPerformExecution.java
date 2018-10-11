package ru.citeck.ecos.workflow.perform;

import org.activiti.engine.delegate.VariableScope;

public class ActivitiPerformExecution extends ActivitiVariableScopeDelegate implements PerformExecution {

    public ActivitiPerformExecution(VariableScope impl) {
        super(impl);
    }
}
