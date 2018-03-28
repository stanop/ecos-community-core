package ru.citeck.ecos.flowable.bootstrap;

import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.module.ImporterModuleComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.citeck.ecos.config.EcosConfigService;
import ru.citeck.ecos.flowable.services.FlowableModelerService;

/**
 * @author Roman Makarskiy
 */
public class FlowableModelerImportProcessModuleComponent extends AbstractModuleComponent {

    private static final Logger logger = Logger.getLogger(FlowableModelerImportProcessModuleComponent.class);

    private static final String IMPORT_CONFIG_KEY = "flowable-process-import-to-modeler-already-executed";
    private static final String IMPORT_CONFIG_BEAN = "flowable.module-component.process-import-to-modeler-config";

    private RetryingTransactionHelper retryingTransactionHelper;
    private FlowableModelerService flowableModelerService;
    private EcosConfigService ecosConfigService;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    protected void executeInternal() {
        AuthenticationUtil.runAs(() -> retryingTransactionHelper.doInTransaction(() -> {
                    if (importRequired()) {
                        flowableModelerService.importProcessModel();
                    }
                    return null;
                },
                false
        ), AuthenticationUtil.getSystemUserName());
    }

    private boolean importRequired() {
        if (flowableModelerService == null || !flowableModelerService.importIsPossible()) {
            logger.info("Cannot import process model, because flowable integration is not initialized.");
            return false;
        }

        String config = processConfig();
        Boolean alreadyImported = Boolean.valueOf(config);

        return !alreadyImported;
    }

    private String processConfig() {
        String config = (String) ecosConfigService.getParamValue(IMPORT_CONFIG_KEY, null);

        if (config == null) {
            ImporterModuleComponent bean = applicationContext.getBean(
                    IMPORT_CONFIG_BEAN, ImporterModuleComponent.class);
            bean.execute();
            config = (String) ecosConfigService.getParamValue(IMPORT_CONFIG_KEY, null);
        }

        return config;
    }

    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper) {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    public void setFlowableModelerService(FlowableModelerService flowableModelerService) {
        this.flowableModelerService = flowableModelerService;
    }

    public void setEcosConfigService(EcosConfigService ecosConfigService) {
        this.ecosConfigService = ecosConfigService;
    }
}
