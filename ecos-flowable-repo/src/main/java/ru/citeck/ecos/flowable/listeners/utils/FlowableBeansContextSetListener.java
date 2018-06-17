package ru.citeck.ecos.flowable.listeners.utils;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.flowable.engine.ProcessEngine;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import ru.citeck.ecos.flowable.listeners.global.GlobalExecutionListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalTaskListener;
import ru.citeck.ecos.providers.ApplicationContextProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Flowable beans context set listener
 */
public class FlowableBeansContextSetListener implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * Exclude js services
     */
    private static final List<String> EXCLUDE_JS_SERVICES = Collections.singletonList("flowableModelerServiceJS");

    /**
     * Application event handling
     * @param contextRefreshedEvent Context refresh event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        /** Refresh engine */
        ProcessEngine processEngine = (ProcessEngine) ApplicationContextProvider.getBean("flowableEngine");
        if (processEngine != null) {
            refreshEngineBeans(processEngine);
        }
    }

    /**
     * Refresh engine beans
     * @param processEngine Process engine
     */
    private void refreshEngineBeans(ProcessEngine processEngine) {
        Map<Object, Object> beans = processEngine.getProcessEngineConfiguration().getBeans();
        /** Global execution listeners */
        Map<String, GlobalExecutionListener> executionListenerMap = ApplicationContextProvider.getApplicationContext().
                getBeansOfType(GlobalExecutionListener.class);
        for (String key : executionListenerMap.keySet()) {
            beans.put(key, executionListenerMap.get(key));
        }
        /** Global task listeners */
        Map<String, GlobalTaskListener> taskListenerMap = ApplicationContextProvider.getApplicationContext().
                getBeansOfType(GlobalTaskListener.class);
        for (String key : taskListenerMap.keySet()) {
            beans.put(key, taskListenerMap.get(key));
        }
        /** Script services */
        Map<String, BaseScopableProcessorExtension> servicesMap = ApplicationContextProvider.getApplicationContext().
                getBeansOfType(BaseScopableProcessorExtension.class);
        for (BaseScopableProcessorExtension extension : servicesMap.values()) {
            if (!EXCLUDE_JS_SERVICES.contains(extension.getExtensionName())) {
                beans.put(extension.getExtensionName(), extension);
            }
        }
    }
}
