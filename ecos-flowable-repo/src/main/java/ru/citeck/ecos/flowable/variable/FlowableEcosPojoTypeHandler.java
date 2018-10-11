package ru.citeck.ecos.flowable.variable;

import org.flowable.variable.api.types.ValueFields;
import org.flowable.variable.api.types.VariableType;
import ru.citeck.ecos.flowable.activiti.variables.ActivitiValueFields;
import ru.citeck.ecos.workflow.variable.handler.EcosPojoTypeHandler;

public class FlowableEcosPojoTypeHandler implements VariableType {

    private final EcosPojoTypeHandler<?> impl;

    public FlowableEcosPojoTypeHandler(EcosPojoTypeHandler<?> impl) {
        this.impl = impl;
    }

    @Override
    public String getTypeName() {
        return impl.getTypeName();
    }

    @Override
    public boolean isCachable() {
        return impl.isCachable();
    }

    @Override
    public boolean isAbleToStore(Object o) {
        return impl.isAbleToStore(o);
    }

    @Override
    public void setValue(Object o, ValueFields valueFields) {
        impl.setValue(o, new ActivitiValueFields(valueFields));
    }

    @Override
    public Object getValue(ValueFields valueFields) {
        return impl.getValue(new ActivitiValueFields(valueFields));
    }
}
