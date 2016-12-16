package ru.citeck.ecos.utils;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class TransactionUtils {

    private static final Logger LOG = Logger.getLogger(TransactionUtils.class);

    private static TransactionService transactionService;

    public static void doBeforeCommit(final Runnable runnable) {
        new DeferBeforeCommit(runnable).run();
    }

    public static void doAfterBehaviours(final Runnable runnable) {
        doBeforeCommit(new DeferBeforeCommit(runnable));
    }

    public static void doAfterCommit(final Runnable job) {

        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

        final Thread thread = new Thread(new Runnable() {

            public void run() {
                AuthenticationUtil.setRunAsUser(currentUser);
                AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
                    public Void doWork() throws Exception {
                        transactionService.getRetryingTransactionHelper().doInTransaction(
                                new RetryingTransactionHelper.RetryingTransactionCallback<String>() {
                                    public String execute() throws Exception {
                                        try {
                                            job.run();
                                        } catch (Exception e) {
                                            List<Class> retryClasses = Arrays.asList(RetryingTransactionHelper.RETRY_EXCEPTIONS);
                                            if (!retryClasses.contains(e.getClass())) {
                                                LOG.error("Exception", e);
                                            }
                                            throw e;
                                        }
                                        return null;
                                    }
                                }, false, true);
                        return null;
                    }
                });
            }
        });

        AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
            @Override
            public void afterCommit() {
                thread.start();
            }
        });
    }


    public static void setServiceRegistry(ServiceRegistry serviceRegistry) {
        transactionService = serviceRegistry.getTransactionService();
    }

}
