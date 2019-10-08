package ru.citeck.ecos.eapps;

import ru.citeck.ecos.apps.app.module.api.ModulePublishMsg;

public interface EcosModulePublisher {

    void publish(ModulePublishMsg publishMsg);

    String getModuleType();
}
