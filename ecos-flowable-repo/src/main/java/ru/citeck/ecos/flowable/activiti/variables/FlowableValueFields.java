package ru.citeck.ecos.flowable.activiti.variables;

import org.flowable.variable.api.types.ValueFields;

public class FlowableValueFields implements ValueFields {

    private final org.activiti.engine.impl.variable.ValueFields impl;

    public FlowableValueFields(org.activiti.engine.impl.variable.ValueFields impl) {
        this.impl = impl;
    }

    @Override
    public String getName() {
        return impl.getName();
    }

    @Override
    public String getProcessInstanceId() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getExecutionId() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getScopeId() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getSubScopeId() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getScopeType() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getTaskId() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getTextValue() {
        return impl.getTextValue();
    }

    @Override
    public void setTextValue(String s) {
        impl.setTextValue(s);
    }

    @Override
    public String getTextValue2() {
        return impl.getTextValue2();
    }

    @Override
    public void setTextValue2(String s) {
        impl.setTextValue2(s);
    }

    @Override
    public Long getLongValue() {
        return impl.getLongValue();
    }

    @Override
    public void setLongValue(Long aLong) {
        impl.setLongValue(aLong);
    }

    @Override
    public Double getDoubleValue() {
        return impl.getDoubleValue();
    }

    @Override
    public void setDoubleValue(Double aDouble) {
        impl.setDoubleValue(aDouble);
    }

    @Override
    public byte[] getBytes() {
        return impl.getBytes();
    }

    @Override
    public void setBytes(byte[] bytes) {
        impl.setBytes(bytes);
    }

    @Override
    public Object getCachedValue() {
        return impl.getCachedValue();
    }

    @Override
    public void setCachedValue(Object o) {
        impl.setCachedValue(o);
    }
}
