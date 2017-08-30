package ru.citeck.ecos.utils;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.I18NUtil;

import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

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
        doAfterCommit(job, null);
    }

    public static void doAfterCommit(final Runnable job, final Runnable errorHandler) {

        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        final Locale locale = I18NUtil.getLocale();

        AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
            @Override
            public void afterCommit() {
                prepareJobThread(job, errorHandler, currentUser, locale).start();
            }
        });
    }

    public static <T> void processBeforeCommit(String transactionKey, T element, Consumer<T> consumer) {
        final Set<T> elements = TransactionalResourceHelper.getSet(transactionKey);
        if (elements.isEmpty()) {
            TransactionUtils.doBeforeCommit(() -> {
                AuthenticationUtil.runAsSystem(() -> {
                    elements.forEach(consumer);
                    return null;
                });
                elements.clear();
            });
        }
        elements.add(element);
    }

    private static Thread prepareJobThread(final Runnable job, final Runnable errorHandler, final String currentUser, final Locale locale) {

        return new Thread(() -> {

            AuthenticationUtil.setRunAsUser(currentUser);
            I18NUtil.setLocale(locale);

            AuthenticationUtil.runAsSystem((AuthenticationUtil.RunAsWork<Void>) () -> {
                try {
                    doInTransaction(job);
                } catch (Exception e) {
                    LOG.error("Exception while job running", e);
                    if (errorHandler != null) {
                        doInTransaction(errorHandler);
                    }
                }
                return null;
            });
        });
    }

    private static void doInTransaction(final Runnable job) {
        transactionService.getRetryingTransactionHelper().doInTransaction(
                (RetryingTransactionHelper.RetryingTransactionCallback<String>) () -> {
                    job.run();
                    return null;
                }, false, true);
    }

    public static void setServiceRegistry(ServiceRegistry serviceRegistry) {
        transactionService = serviceRegistry.getTransactionService();
    }
}
