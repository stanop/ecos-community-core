package ru.citeck.ecos.icase;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.apache.log4j.Logger;
import ru.citeck.ecos.cmmn.service.CaseImportService;
import ru.citeck.ecos.utils.AbstractDeployerBean;

import java.io.InputStream;

/**
 * @author Maxim Strizhov (maxim.strizhov@citeck.com)
 */
public class CaseTemplateDeployer extends AbstractDeployerBean {
    private static final Logger logger = Logger.getLogger(CaseTemplateDeployer.class);

    private CaseImportService caseImportService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private boolean enabled;

    public CaseTemplateDeployer() {
        super("caseTemplateDeployer");
    }

    public void setCaseImportService(CaseImportService caseImportService) {
        this.caseImportService = caseImportService;
    }

    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper) {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected void load(final String location, final InputStream inputStream) {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>() {
            public String doWork() throws Exception{
                return retryingTransactionHelper.doInTransaction(
                        new RetryingTransactionHelper.RetryingTransactionCallback<String>() {
                            public String execute() throws Exception {
                                if (enabled) {
                                    logger.info("Deploying case template from file: " + location);
                                    caseImportService.importCase(inputStream);
                                }
                                logger.info("Finish deploying case templates");
                                return null;
                            }
                        },
                        false
                );
            }
        }, AuthenticationUtil.getSystemUserName());
    }
}
