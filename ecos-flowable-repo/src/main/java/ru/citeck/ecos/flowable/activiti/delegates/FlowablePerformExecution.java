package ru.citeck.ecos.flowable.activiti.delegates;

import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import ru.citeck.ecos.workflow.perform.PerformExecution;

public class FlowablePerformExecution extends FlowableVariableScopeDelegate implements PerformExecution {

    public FlowablePerformExecution(ExecutionEntity impl) {
        super(impl);
    }
}
