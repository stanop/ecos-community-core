package ru.citeck.ecos.eapps;

import ru.citeck.ecos.apps.app.module.EcosModule;

public interface EcosModulePublisher<T extends EcosModule> {

    void publish(T module);

    Class<T> getModuleType();
}
