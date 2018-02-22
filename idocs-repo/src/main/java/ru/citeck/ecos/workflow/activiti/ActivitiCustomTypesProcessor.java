package ru.citeck.ecos.workflow.activiti;

import org.activiti.engine.impl.variable.VariableType;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.ArrayList;
import java.util.List;

public class ActivitiCustomTypesProcessor implements BeanFactoryPostProcessor {

    private static final String CONFIG_ID = "activitiProcessEngineConfiguration";
    private static final String CUSTOM_PRE_VAR_TYPES = "customPreVariableTypes";
    private static final String CUSTOM_POST_VAR_TYPES = "customPostVariableTypes";

    private List<VariableType> customPreVariableTypes;
    private List<VariableType> customPostVariableTypes;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(CONFIG_ID);
        MutablePropertyValues properties = beanDefinition.getPropertyValues();
        addVariableTypes(CUSTOM_PRE_VAR_TYPES, customPreVariableTypes, properties);
        addVariableTypes(CUSTOM_POST_VAR_TYPES, customPostVariableTypes, properties);
    }

    private void addVariableTypes(String varName, List<VariableType> types, MutablePropertyValues properties) {
        if (types != null) {
            List<VariableType> vars = getProperty(properties, varName);
            vars = vars != null ? new ArrayList<>(vars) : new ArrayList<>();
            vars.addAll(customPreVariableTypes);
            properties.add(varName, vars);
        }
    }

    private <T> T getProperty(MutablePropertyValues properties, String key) {
        PropertyValue value = properties.getPropertyValue(key);
        return value != null ? (T) value.getValue() : null;
    }

    public void setCustomPreVariableTypes(List<VariableType> customPreVariableTypes) {
        this.customPreVariableTypes = customPreVariableTypes;
    }

    public List<VariableType> getCustomPreVariableTypes() {
        return customPreVariableTypes;
    }

    public void setCustomPostVariableTypes(List<VariableType> customPostVariableTypes) {
        this.customPostVariableTypes = customPostVariableTypes;
    }

    public List<VariableType> getCustomPostVariableTypes() {
        return customPostVariableTypes;
    }
}
