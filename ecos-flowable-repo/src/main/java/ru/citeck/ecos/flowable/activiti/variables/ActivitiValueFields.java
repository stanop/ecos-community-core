package ru.citeck.ecos.flowable.activiti.variables;

import org.activiti.engine.impl.persistence.entity.ByteArrayEntity;
import org.activiti.engine.impl.variable.ValueFields;

public class ActivitiValueFields implements ValueFields {

    private final org.flowable.variable.api.types.ValueFields impl;

    public ActivitiValueFields(org.flowable.variable.api.types.ValueFields impl) {
        this.impl = impl;
    }

    @Override
    public String getName() {
        return impl.getName();
    }

    @Override
    public String getTextValue() {
        return impl.getTextValue();
    }

    @Override
    public void setTextValue(String textValue) {
        impl.setTextValue(textValue);
    }

    @Override
    public String getTextValue2() {
        return impl.getTextValue2();
    }

    @Override
    public void setTextValue2(String textValue2) {
        impl.setTextValue2(textValue2);
    }

    @Override
    public Long getLongValue() {
        return impl.getLongValue();
    }

    @Override
    public void setLongValue(Long longValue) {
        impl.setLongValue(longValue);
    }

    @Override
    public Double getDoubleValue() {
        return impl.getDoubleValue();
    }

    @Override
    public void setDoubleValue(Double doubleValue) {
        impl.setDoubleValue(doubleValue);
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
    public String getByteArrayValueId() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public ByteArrayEntity getByteArrayValue() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setByteArrayValue(byte[] bytes) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object getCachedValue() {
        return impl.getCachedValue();
    }

    @Override
    public void setCachedValue(Object cachedValue) {
        impl.setCachedValue(cachedValue);
    }
}
