package ru.citeck.ecos.eapps;

import kotlin.Unit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.module.ModuleDependency;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.app.EcosApp;
import ru.citeck.ecos.apps.app.EcosAppType;
import ru.citeck.ecos.apps.app.provider.ComputedModule;
import ru.citeck.ecos.apps.app.provider.ComputedModulesProvider;
import ru.citeck.ecos.apps.app.provider.EcosAppsProvider;
import ru.citeck.ecos.apps.module.command.getmodules.GetModulesMeta;
import ru.citeck.ecos.apps.module.type.TypeContext;
import ru.citeck.ecos.apps.module.type.provider.ModuleTypesProvider;
import ru.citeck.ecos.commons.io.file.EcosFile;
import ru.citeck.ecos.commons.io.file.mem.EcosMemDir;
import ru.citeck.ecos.commons.io.file.std.EcosStdFile;
import ru.citeck.ecos.utils.ResourceResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class EcosAppsModulesProviderImpl implements EcosAppsProvider, ModuleTypesProvider, ComputedModulesProvider {

    private static final String MIGRATION_APP_ID = "ecos-modules-migration";

    private ResourceResolver resolver;
    private ModuleService moduleService;

    @Getter(lazy = true)
    private final List<EcosApp> registeredEcosApps = findEcosAppsImpl();

    private final Map<String, ModuleMigration> migrations = new ConcurrentHashMap<>();

    private boolean updateRequired = false;

    @Autowired
    public EcosAppsModulesProviderImpl(@Qualifier("resourceResolver") ResourceResolver resolver,
                                       ModuleService moduleService) {
        this.resolver = resolver;
        this.moduleService = moduleService;
    }

    public void update() {
        updateRequired = true;
    }

    @NotNull
    @Override
    public EcosFile getModuleTypes() {
        return getDir("extension/emtypes");
    }

    @NotNull
    @Override
    public List<ComputedModule> getComputedModules(@NotNull String app,
                                                   @NotNull TypeContext typeContext,
                                                   @NotNull GetModulesMeta getModulesMeta) {

        if (!MIGRATION_APP_ID.equals(app) || !migrations.containsKey(typeContext.getId())) {
            return Collections.emptyList();
        }

        List<ComputedModule> result = migrations.get(typeContext.getId())
            .getModulesSince(getModulesMeta.getLastConsumedMs());

        if (result == null) {
            result = Collections.emptyList();
        }

        return result;
    }

    @NotNull
    @Override
    public EcosFile getModulesRoot(@NotNull String appName) {
        return getDir("module/" + appName);
    }

    private EcosFile getDir(String path) {
        try {
            Resource modulesDir = resolver.getResource("classpath:alfresco/" + path);
            if (modulesDir != null && modulesDir.exists()) {
                File file = null;
                try {
                    file = modulesDir.getFile();
                } catch (FileNotFoundException e) {
                    // module is not a directory (e.g. jar module). do nothing
                }
                if (file != null) {
                    return new EcosStdFile(file);
                }
            }
        } catch (Exception e) {
            log.error("Directory resolving error. Path: " + path, e);
        }
        return new EcosMemDir();
    }

    @NotNull
    @Override
    public List<EcosApp> getEcosApps() {
        if (updateRequired) {
            updateRequired = false;
            return Collections.emptyList();
        }
        return getRegisteredEcosApps();
    }

    private List<EcosApp> findEcosAppsImpl() {

        if (moduleService == null) {
            throw new IllegalStateException("Module service is not initialized");
        }

        List<ModuleDetails> modules = moduleService.getAllModules();

        List<EcosApp> result = new ArrayList<>();
        modules.forEach(m -> {
            try {
                result.add(convertToEcosApp(m));
            } catch (Exception e) {
                log.error("Module can't be converted: " + m);
            }
        });

        log.info("Found " + modules.size() + " modules");

        result.add(EcosApp.create(b -> {
            b.setId(MIGRATION_APP_ID);
            b.setName(MIGRATION_APP_ID);
            b.setVersion("1.0.0");
            b.setProvidedTypes(migrations.keySet());
            b.setType(EcosAppType.COMPUTED);
            return Unit.INSTANCE;
        }));

        return result;
    }

    @Autowired(required = false)
    public void setMigrations(List<ModuleMigration> migrations) {
        migrations.forEach(m -> this.migrations.put(m.getModuleType(), m));
    }

    private EcosApp convertToEcosApp(ModuleDetails module) {

        return EcosApp.create(b -> {

            b.setId(module.getId());
            b.setName(module.getTitle());

            String version = String.valueOf(module.getModuleVersionNumber());
            version = version.replaceAll("[^0-9.]", "");
            if (version.isEmpty()) {
                version = "1.0";
            }

            b.setVersion(version);

            List<ModuleDependency> dependencies = module.getDependencies();
            if (dependencies != null) {
                for (ModuleDependency dep : dependencies) {
                    b.setDependency(dep.getDependencyId(), "1.0");
                }
            }

            return Unit.INSTANCE;
        });
    }
}
