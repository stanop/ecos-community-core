package ru.citeck.ecos.flowable.listeners.utils;

import org.flowable.engine.FormService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import ru.citeck.ecos.flowable.converters.FlowablePropertyConverter;
import ru.citeck.ecos.flowable.listeners.global.impl.task.create.TaskCreateListener;
import ru.citeck.ecos.providers.ApplicationContextProvider;

/**
 * Flowable set context start listener
 */
public class FlowableSetContextStartListener implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * Application event handling
     * @param contextRefreshedEvent Context refresh event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        /** Set services after context initialization */
        TaskCreateListener taskCreateListener = ApplicationContextProvider.getBean(TaskCreateListener.class);
        if (taskCreateListener != null) {
            taskCreateListener.setFormService(ApplicationContextProvider.getBean(FormService.class));
            taskCreateListener.setPropertyConverter(ApplicationContextProvider.getBean(FlowablePropertyConverter.class));
        }
    }
}
