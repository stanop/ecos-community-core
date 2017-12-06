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

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public class TransactionUtils {

    private static final Logger LOG = Logger.getLogger(TransactionUtils.class);

    private static final String AFTER_COMMIT_JOBS_KEY = TransactionUtils.class + ".after-commit-jobs";

    private static TransactionService transactionService;

    public static void doBeforeCommit(final Runnable runnable) {
        new DeferBeforeCommit(runnable).run();
    }

    public static void doAfterBehaviours(final Runnable runnable) {
        doBeforeCommit(new DeferBeforeCommit(runnable));
    }

    public static void doAfterCommit(final Runnable job) {
        doAfterCommit(job, (Consumer<Exception>) null);
    }

    public static void doAfterCommit(final Runnable job, final Runnable errorHandler) {
        doAfterCommit(job, e -> errorHandler.run());
    }

    public static void doAfterCommit(final Runnable job, final Consumer<Exception> errorHandler) {

        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        final Locale locale = I18NUtil.getLocale();

        final List<Job> jobs = TransactionalResourceHelper.getList(AFTER_COMMIT_JOBS_KEY);
        if (jobs.size() == 0) {
            AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
                @Override
                public void afterCommit() {
                    prepareJobsThread(jobs, currentUser, locale).start();
                }
            });
        }
        jobs.add(new Job(job, errorHandler));
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

    private static Thread prepareJobsThread(List<Job> jobs, final String currentUser, final Locale locale) {

        return new Thread(() -> {

            AuthenticationUtil.setRunAsUser(currentUser);
            I18NUtil.setLocale(locale);

            AuthenticationUtil.runAsSystem(() -> {

                for (int i = 0; i < jobs.size(); i++) {
                    Job job = jobs.get(i);
                    try {
                        doInTransaction(() -> {
                            AlfrescoTransactionSupport.bindResource(AFTER_COMMIT_JOBS_KEY, jobs);
                            job.runnable.run();
                        });
                    } catch (Exception e) {
                        LOG.error("Exception while job running", e);
                        if (job.errorHandler != null) {
                            doInTransaction(() -> job.errorHandler.accept(e));
                        }
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

    private static class Job {

        Runnable runnable;
        Consumer<Exception> errorHandler;

        Job(Runnable runnable, Consumer<Exception> errorHandler) {
            this.runnable = runnable;
            this.errorHandler = errorHandler;
        }
    }
}
