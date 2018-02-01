package ru.citeck.ecos.job;

import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import ru.citeck.ecos.icase.timer.CaseTimerService;
import ru.citeck.ecos.model.CaseTimerModel;
import ru.citeck.ecos.service.AlfrescoServices;
import ru.citeck.ecos.service.EcosCoreServices;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Pavel Simonov
 */
public class CaseTimerJob extends AbstractScheduledLockedJob implements StatefulJob {

    private static final String PROCESS_NAME = "update-timers-process";

    private static final int BATCH_SIZE = 1;
    private static final int WORKER_THREADS = 1;
    private static final int LOGGING_INTERVAL = 10;

    private static final String SEARCH_QUERY = String.format("TYPE:\"%s\" AND @%s:[MIN TO NOW]",
                                                             CaseTimerModel.TYPE_TIMER,
                                                             CaseTimerModel.PROP_OCCUR_DATE);
    private static final SearchParameters searchParameters;

    static {
        searchParameters = new SearchParameters();
        searchParameters.setLimit(0);
        searchParameters.setLimitBy(LimitBy.UNLIMITED);
        searchParameters.setMaxItems(-1);
        searchParameters.setMaxPermissionChecks(Integer.MAX_VALUE);
        searchParameters.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setQuery(SEARCH_QUERY);
    }

    @Override
    public void executeJob(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap data = context.getJobDetail().getJobDataMap();
        AuthenticationUtil.runAsSystem(() -> {
            final ServiceRegistry serviceRegistry = (ServiceRegistry) data.get("serviceRegistry");
            RepositoryState repositoryState = getService(serviceRegistry, AlfrescoServices.REPOSITORY_STATE);
            if (!repositoryState.isBootstrapping()) {
                updateTimers(serviceRegistry);
            }
            return null;
        });
    }

    private void updateTimers(ServiceRegistry serviceRegistry) {

        SearchService searchService = serviceRegistry.getSearchService();
        CaseTimerService caseTimerService = getService(serviceRegistry, EcosCoreServices.CASE_TIMER_SERVICE);

        List<NodeRef> nodeRefs = findCompletedTimers(searchService);

        if (!nodeRefs.isEmpty()) {
            TransactionService transactionService = serviceRegistry.getTransactionService();
            RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();

            BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>(
                    PROCESS_NAME,
                    retryingTransactionHelper,
                    new TimerWorkProvider(nodeRefs),
                    WORKER_THREADS, BATCH_SIZE,
                    null, null, LOGGING_INTERVAL
            );

            batchProcessor.process(new TimerWorker(caseTimerService), true);
        }
    }

    private List<NodeRef> findCompletedTimers(SearchService searchService) {
        ResultSet results = searchService.query(searchParameters);
        try {
            return results.getNodeRefs();
        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    private static <T> T getService(ServiceRegistry registry, QName name) {
        @SuppressWarnings("unchecked")
        T service = (T) registry.getService(name);
        return service;
    }

    private static class TimerWorker extends BatchProcessor.BatchProcessWorkerAdaptor<NodeRef> {

        private CaseTimerService caseTimerService;

        TimerWorker(CaseTimerService caseTimerService) {
            this.caseTimerService = caseTimerService;
        }

        @Override
        public void process(NodeRef timerRef) {
            caseTimerService.timerOccur(timerRef);
        }
    }

    private static class TimerWorkProvider implements BatchProcessWorkProvider<NodeRef> {

        private Collection<NodeRef> nodeRefs;
        private boolean hasMore = true;

        TimerWorkProvider(Collection<NodeRef> nodeRefs) {
            this.nodeRefs = nodeRefs;
        }

        @Override
        public int getTotalEstimatedWorkSize() {
            return nodeRefs.size();
        }

        @Override
        public Collection<NodeRef> getNextWork() {
            if (hasMore) {
                hasMore = false;
                return nodeRefs;
            } else {
                return Collections.emptyList();
            }
        }
    }
}