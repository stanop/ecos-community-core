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

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(FlowableModelerImportProcessModuleComponent.class);

    /**
     * Constants
     */
    private static final String IMPORT_CONFIG_KEY = "flowable-process-import-to-modeler-already-executed";
    private static final String IMPORT_CONFIG_BEAN = "flowable.module-component.process-import-to-modeler-config";

    /**
     * Services
     */
    private RetryingTransactionHelper retryingTransactionHelper;
    private FlowableModelerService flowableModelerService;
    private EcosConfigService ecosConfigService;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Execute internal
     */
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

    /**
     * Check - is import required
     * @return Check result
     */
    private boolean importRequired() {
        if (flowableModelerService == null || !flowableModelerService.importIsPossible()) {
            LOGGER.info("Cannot import process model, because flowable integration is not initialized.");
            return false;
        }
        return !initializeModels();
    }

    /**
     * Initialize models (first time)
     * @return Was initialization called check
     */
    private Boolean initializeModels() {
        String config = (String) ecosConfigService.getParamValue(IMPORT_CONFIG_KEY);

        /** Call initializing */
        if (config == null) {
            ImporterModuleComponent bean = applicationContext.getBean(IMPORT_CONFIG_BEAN,
                    ImporterModuleComponent.class);
            bean.execute();
            return true;
        } else {
            return false;
        }
    }

    /** Setters */

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
