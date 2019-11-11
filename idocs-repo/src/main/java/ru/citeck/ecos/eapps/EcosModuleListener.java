package ru.citeck.ecos.eapps;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.EcosAppsApiFactory;
import ru.citeck.ecos.apps.app.module.EcosModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@DependsOn({"moduleStarter"})
public class EcosModuleListener extends AbstractLifecycleBean {

    private Map<Class<?>, EcosModulePublisher> publishers = new HashMap<>();
    private EcosAppsApiFactory apiFactory;

    @Autowired
    public EcosModuleListener(EcosAppsApiFactory apiFactory, List<EcosModulePublisher> publishers) {
        this.apiFactory = apiFactory;
        publishers.forEach(p -> this.publishers.put(p.getModuleType(), p));
    }

    @Override
    protected void onBootstrap(ApplicationEvent event) {

        log.info("Initialize EcosModuleListener");

        try {
            for (Class<?> type : publishers.keySet()) {
                @SuppressWarnings("unchecked")
                Class<EcosModule> moduleType = (Class<EcosModule>) type;
                apiFactory.getModuleApi().onModulePublished(moduleType, this::handleMessage);
            }
        } catch (Exception e) {
            log.error("Modules subscription failed", e);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {
    }

    private void handleMessage(EcosModule module) {

        @SuppressWarnings("unchecked")
        EcosModulePublisher<EcosModule> publisher = publishers.get(module.getClass());
        if (publisher == null) {
            throw new IllegalArgumentException("Publisher is not registered for type " + module.getClass());
        }

        publisher.publish(module);
    }
}
