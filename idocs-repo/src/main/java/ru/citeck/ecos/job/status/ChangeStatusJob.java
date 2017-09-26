package ru.citeck.ecos.job.status;

import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.apache.commons.logging.Log;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.service.AlfrescoServices;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.service.EcosCoreServices;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Job to find nodes and change status
 *
 * @author Pavel Simonov
 */
public class ChangeStatusJob extends AbstractScheduledLockedJob implements StatefulJob {

    private static final int WORKER_THREADS = 1;
    private static final int BATCH_SIZE = 10;
    private static final int LOGGING_INTERVAL = 50;

    private static final Log logger = LogFactory.getLog(ChangeStatusJob.class);

    @Override
    public void executeJob(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap data = context.getJobDetail().getJobDataMap();
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {
                final ServiceRegistry serviceRegistry = (ServiceRegistry) data.get("serviceRegistry");
                RepositoryState repositoryState = (RepositoryState) serviceRegistry.getService(AlfrescoServices.REPOSITORY_STATE);
                if (repositoryState.isBootstrapping()) {
                    return null;
                }
                ChangeStatusJobRegistry registry = (ChangeStatusJobRegistry) data.get("changeStatusJobRegistry");
                List<ChangeStatusByDateWork> works = registry.getWorks();
                if (!works.isEmpty()) {
                    logger.info("Start status updating. Number of works: " + works.size());
                    for (int i = 0; i < works.size(); i++) {
                        try {
                            makeTransitions(serviceRegistry, works.get(i), i);
                        } catch (Exception e) {
                            logger.error("Work #" + i + " failed", e);
                        }
                    }
                }
                return null;
            }
        });
    }

    private void makeTransitions(ServiceRegistry serviceRegistry, final ChangeStatusByDateWork changeStatusData, int index) {

        if (!changeStatusData.isEnabled()) {
            return;
        }

        String workName = "Change status work #" + index;
        logger.info(workName + ": " + changeStatusData);

        CaseStatusService caseStatusService = EcosCoreServices.getCaseStatusService(serviceRegistry);
        TransactionService transactionService = serviceRegistry.getTransactionService();
        RetryingTransactionHelper transactionHelper = transactionService.getRetryingTransactionHelper();

        List<NodeRef> nodeRefs = transactionHelper.doInTransaction(new RetryingTransactionCallback<List<NodeRef>>() {
            @Override
            public List<NodeRef> execute() throws Throwable {
                return changeStatusData.queryNodes();
            }
        });

        logger.info(workName + ": nodes to change: " + nodeRefs.size());
        if (!nodeRefs.isEmpty()) {

            BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>(
                    workName + " status changing",
                    transactionHelper,
                    new ChangeStatusWorkProvider(nodeRefs),
                    WORKER_THREADS, BATCH_SIZE,
                    null, logger, LOGGING_INTERVAL
            );

            String targetStatus = changeStatusData.getTargetStatus();
            batchProcessor.process(new ChangeStatusWorker(caseStatusService, targetStatus), true);
        }
    }

    private static class ChangeStatusWorker extends BatchProcessor.BatchProcessWorkerAdaptor<NodeRef> {

        private CaseStatusService caseStatusService;
        private NodeRef toStatusRef;
        private String toStatus;

        ChangeStatusWorker(CaseStatusService caseStatusService, String toStatus) {
            this.caseStatusService = caseStatusService;
            this.toStatus = toStatus;
            this.toStatusRef = caseStatusService.getStatusByName(toStatus);
            if (this.toStatusRef == null) {
                throw new IllegalArgumentException("Status not found. Status: " + toStatus);
            }
        }

        @Override
        public void process(NodeRef entry) throws Throwable {
            try {
                caseStatusService.setStatus(entry, toStatusRef);
            } catch (Exception e) {
                throw new RuntimeException("Exception on change status. nodeRef: " + entry + " status: " + toStatus, e);
            }
        }
    }

    private static class ChangeStatusWorkProvider implements BatchProcessWorkProvider<NodeRef> {

        private Collection<NodeRef> nodeRefs;
        private boolean hasMore = true;

        ChangeStatusWorkProvider(Collection<NodeRef> nodeRefs) {
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
