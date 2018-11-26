package ru.citeck.ecos.workflow.perform;

import org.activiti.engine.delegate.VariableScope;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ActivitiVariableScopeDelegate implements VariableScope {

    private final VariableScope impl;

    public ActivitiVariableScopeDelegate(VariableScope impl) {
        this.impl = impl;
    }

    @Override
    public Map<String, Object> getVariables() {
        return impl.getVariables();
    }

    @Override
    public Map<String, Object> getVariables(Collection<String> variableNames) {
        return impl.getVariables(variableNames);
    }

    @Override
    public Map<String, Object> getVariables(Collection<String> variableNames, boolean fetchAllVariables) {
        return impl.getVariables(variableNames, fetchAllVariables);
    }

    @Override
    public Map<String, Object> getVariablesLocal() {
        return impl.getVariablesLocal();
    }

    @Override
    public Map<String, Object> getVariablesLocal(Collection<String> variableNames) {
        return impl.getVariablesLocal(variableNames);
    }

    @Override
    public Map<String, Object> getVariablesLocal(Collection<String> variableNames, boolean fetchAllVariables) {
        return impl.getVariablesLocal(variableNames, fetchAllVariables);
    }

    @Override
    public Object getVariable(String variableName) {
        return impl.getVariable(variableName);
    }

    @Override
    public Object getVariable(String variableName, boolean fetchAllVariables) {
        return impl.getVariable(variableName, fetchAllVariables);
    }

    @Override
    public Object getVariableLocal(String variableName) {
        return impl.getVariableLocal(variableName);
    }

    @Override
    public Object getVariableLocal(String variableName, boolean fetchAllVariables) {
        return impl.getVariableLocal(variableName, fetchAllVariables);
    }

    @Override
    public <T> T getVariable(String variableName, Class<T> variableClass) {
        return impl.getVariable(variableName, variableClass);
    }

    @Override
    public <T> T getVariableLocal(String variableName, Class<T> variableClass) {
        return impl.getVariableLocal(variableName, variableClass);
    }

    @Override
    public Set<String> getVariableNames() {
        return impl.getVariableNames();
    }

    @Override
    public Set<String> getVariableNamesLocal() {
        return impl.getVariableNamesLocal();
    }

    @Override
    public void setVariable(String variableName, Object value) {
        impl.setVariable(variableName, value);
    }

    @Override
    public void setVariable(String variableName, Object value, boolean fetchAllVariables) {
        impl.setVariable(variableName, value, fetchAllVariables);
    }

    @Override
    public Object setVariableLocal(String variableName, Object value) {
        return impl.setVariableLocal(variableName, value);
    }

    @Override
    public Object setVariableLocal(String variableName, Object value, boolean fetchAllVariables) {
        return impl.setVariableLocal(variableName, value, fetchAllVariables);
    }

    @Override
    public void setVariables(Map<String, ?> variables) {
        impl.setVariables(variables);
    }

    @Override
    public void setVariablesLocal(Map<String, ?> variables) {
        impl.setVariablesLocal(variables);
    }

    @Override
    public boolean hasVariables() {
        return impl.hasVariables();
    }

    @Override
    public boolean hasVariablesLocal() {
        return impl.hasVariablesLocal();
    }

    @Override
    public boolean hasVariable(String variableName) {
        return impl.hasVariable(variableName);
    }

    @Override
    public boolean hasVariableLocal(String variableName) {
        return impl.hasVariableLocal(variableName);
    }

    @Override
    public void createVariableLocal(String variableName, Object value) {
        impl.createVariableLocal(variableName, value);
    }

    @Override
    public void removeVariable(String variableName) {
        impl.removeVariable(variableName);
    }

    @Override
    public void removeVariableLocal(String variableName) {
        impl.removeVariableLocal(variableName);
    }

    @Override
    public void removeVariables(Collection<String> variableNames) {
        impl.removeVariables(variableNames);
    }

    @Override
    public void removeVariablesLocal(Collection<String> variableNames) {
        impl.removeVariablesLocal(variableNames);
    }

    @Override
    public void removeVariables() {
        impl.removeVariables();
    }

    @Override
    public void removeVariablesLocal() {
        impl.removeVariablesLocal();
    }
}
