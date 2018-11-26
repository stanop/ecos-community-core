package ru.citeck.ecos.flowable.activiti.variables;

import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;

public class ActivitiVariableType implements VariableType {

    private org.flowable.variable.api.types.VariableType impl;

    public ActivitiVariableType(org.flowable.variable.api.types.VariableType impl) {
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
    public boolean isAbleToStore(Object value) {
        return impl.isAbleToStore(value);
    }

    @Override
    public void setValue(Object value, ValueFields valueFields) {
        impl.setValue(value, new FlowableValueFields(valueFields));
    }

    @Override
    public Object getValue(ValueFields valueFields) {
        return impl.getValue(new FlowableValueFields(valueFields));
    }
}

