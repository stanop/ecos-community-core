package ru.citeck.ecos.utils;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TransactionUtils {

    private static final Logger LOG = Logger.getLogger(TransactionUtils.class);

    @Autowired
    @Qualifier("transactionService")
    private TransactionService transactionService;

    @Autowired
    private SessionTaskExecutor taskExecutor;

    public void doBeforeCommit(final Runnable runnable) {
        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        final Locale locale = I18NUtil.getLocale();
        AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
            @Override
            public void beforeCommit(boolean readOnly) {
                taskExecutor.getExecutorService().execute(getRunnableTask(runnable, null, currentUser, locale));
            }
        });
    }

    public void doAfterBehaviours(final Runnable runnable) {
        doBeforeCommit(runnable);
    }

    public void doAfterCommit(final Runnable job) {
        doAfterCommit(job, null);
    }

    public void doAfterCommit(final Runnable job, final Runnable errorHandler) {

        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        final Locale locale = I18NUtil.getLocale();

        AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
            @Override
            public void afterCommit() {
                taskExecutor.getExecutorService().execute(getRunnableTask(job, errorHandler, currentUser, locale));
            }
        });
    }

    private Thread prepareJobThread(final Runnable job, final Runnable errorHandler, final String currentUser, final Locale locale) {

        return new Thread(getRunnableTask(job, errorHandler, currentUser, locale));
    }

    private Runnable getRunnableTask(final Runnable job, final Runnable errorHandler, final String currentUser, final Locale locale) {
        return new Runnable() {

            public void run() {

                AuthenticationUtil.setRunAsUser(currentUser);
                I18NUtil.setLocale(locale);

                AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
                    public Void doWork() throws Exception {
                        try {
                            doInTransaction(job);
                        } catch (Exception e) {
                            LOG.error("Exception while job running", e);
                            if (errorHandler != null) {
                                doInTransaction(errorHandler);
                            }
                        }
                        return null;
                    }
                });
            }
        };
    }

    private void doInTransaction(final Runnable job) {
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<String>() {
                    public String execute() throws Exception {
                        job.run();
                        return null;
                    }
                }, false, true);
    }
}
