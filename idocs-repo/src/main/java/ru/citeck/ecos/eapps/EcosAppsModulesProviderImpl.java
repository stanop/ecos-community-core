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
import ru.citeck.ecos.apps.app.provider.EcosAppsProvider;
import ru.citeck.ecos.apps.module.type.provider.ModuleTypesProvider;
import ru.citeck.ecos.commons.io.file.EcosFile;
import ru.citeck.ecos.commons.io.file.mem.EcosMemDir;
import ru.citeck.ecos.commons.io.file.std.EcosStdFile;
import ru.citeck.ecos.utils.ResourceResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class EcosAppsModulesProviderImpl implements EcosAppsProvider, ModuleTypesProvider {

    private ResourceResolver resolver;
    private ModuleService moduleService;

    @Getter(lazy = true)
    private final List<EcosApp> registeredEcosApps = findEcosAppsImpl();

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
    public EcosFile getModulesRoot(@NotNull String appName) {
        return getDir("module/" + appName);
    }

    private EcosFile getDir(String path) {
        try {
            Resource modulesDir = resolver.getResource("classpath:alfresco/" + path);
            if (modulesDir != null && modulesDir.exists()) {
                return new EcosStdFile(modulesDir.getFile());
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

        return result;
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
