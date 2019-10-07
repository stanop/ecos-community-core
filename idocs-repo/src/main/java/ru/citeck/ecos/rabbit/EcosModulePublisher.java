package ru.citeck.ecos.rabbit;

import ru.citeck.ecos.apps.queue.ModulePublishMsg;

public interface EcosModulePublisher {

    void publish(ModulePublishMsg publishMsg);

    String getModuleType();
}
