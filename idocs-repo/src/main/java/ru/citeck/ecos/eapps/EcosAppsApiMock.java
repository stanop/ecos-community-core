package ru.citeck.ecos.eapps;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.citeck.ecos.apps.EcosAppsApiFactory;
import ru.citeck.ecos.apps.app.EcosAppApi;
import ru.citeck.ecos.apps.app.api.EcosAppDeployMsg;
import ru.citeck.ecos.apps.app.module.api.EcosModuleApi;
import ru.citeck.ecos.apps.app.module.api.ModulePublishMsg;
import ru.citeck.ecos.apps.app.module.api.ModulePublishResultMsg;
import ru.citeck.ecos.apps.app.patch.api.EcosPatchApi;
import ru.citeck.ecos.apps.app.patch.api.PatchApplyMsg;
import ru.citeck.ecos.apps.app.patch.api.PatchResultMsg;
import ru.citeck.ecos.apps.utils.UnsafeConsumer;
import ru.citeck.ecos.apps.utils.UnsafeFunction;

@Slf4j
@Getter
public class EcosAppsApiMock implements EcosAppsApiFactory {

    private EcosAppApi appApi = new EcosAppsApiImpl();
    private EcosPatchApi patchApi = new EcosPatchApiImpl();
    private EcosModuleApi moduleApi = new EcosModuleApiImpl();

    private static class EcosModuleApiImpl implements EcosModuleApi {

        @Override
        public void publishModule(ModulePublishMsg msg) {
            log.warn("publishModule is not allowed");
        }

        @Override
        public void onModulePublished(String type, UnsafeConsumer<ModulePublishMsg> consumer) {
            log.warn("onModulePublished is not allowed");
        }

        @Override
        public void onModulePublishResult(UnsafeConsumer<ModulePublishResultMsg> consumer) {
            log.warn("onModulePublishResult is not allowed");
        }
    }

    private static class EcosPatchApiImpl implements EcosPatchApi {

        @Override
        public void applyPatch(String target, PatchApplyMsg patch) {
            log.warn("applyPatch is not allowed");
        }

        @Override
        public void onPatchApply(String target, UnsafeFunction<PatchApplyMsg, Object> consumer) {
            log.warn("onPatchApply is not allowed");
        }

        @Override
        public void onPatchResult(UnsafeConsumer<PatchResultMsg> consumer) {
            log.warn("onPatchResult is not allowed");
        }
    }

    private static class EcosAppsApiImpl implements EcosAppApi {

        @Override
        public void deployApp(EcosAppDeployMsg deployMsg) {
            log.warn("deployApp is not allowed");
        }

        @Override
        public void onAppDeploy(UnsafeConsumer<EcosAppDeployMsg> consumer) {
            log.warn("onAppDeploy is not allowed");
        }
    }
}
