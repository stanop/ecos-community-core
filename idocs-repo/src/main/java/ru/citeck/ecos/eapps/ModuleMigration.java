package ru.citeck.ecos.eapps;

import ru.citeck.ecos.apps.app.provider.ComputedModule;

import java.util.List;

public interface ModuleMigration {

    List<ComputedModule> getModulesSince(long time);

    String getModuleType();
}
