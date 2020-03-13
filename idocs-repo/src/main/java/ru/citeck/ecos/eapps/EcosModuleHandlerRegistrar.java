package ru.citeck.ecos.eapps;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.module.handler.EcosModuleHandler;
import ru.citeck.ecos.apps.module.handler.ModuleHandlersService;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class EcosModuleHandlerRegistrar {

    private List<EcosModuleHandler<?>> handlers = null;

    @Autowired
    private ModuleHandlersService handlersService;

    @PostConstruct
    public void registerAll() {
        if (handlers != null) {
            handlers.forEach(this::register);
        }
    }

    private void register(EcosModuleHandler<?> handler) {

        log.info("Found and registered '" + handler.getModuleType() + "' handler "
            + "with name: " + handler.getClass().getSimpleName());

        handlersService.register(handler);
    }

    @Autowired(required = false)
    public void setHandlers(List<EcosModuleHandler<?>> handlers) {
        this.handlers = handlers;
    }
}
