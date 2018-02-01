package ru.citeck.ecos.job.actions;

import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import ru.citeck.ecos.service.AlfrescoServices;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Job to find nodes and process some actions.
 *
 * @author Pavel Simonov
 * @author Roman Makarskiy
 */
public class ExecuteActionJob extends AbstractScheduledLockedJob implements StatefulJob {

    private static final int WORKER_THREADS = 1;
    private static final int BATCH_SIZE = 10;
    private static final int LOGGING_INTERVAL = 50;

    private static final Log logger = LogFactory.getLog(ExecuteActionJob.class);

    @Override
    public void executeJob(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap data = context.getJobDetail().getJobDataMap();
        AuthenticationUtil.runAsSystem(() -> {
            final ServiceRegistry serviceRegistry = (ServiceRegistry) data.get("serviceRegistry");
            RepositoryState repositoryState = (RepositoryState) serviceRegistry.
                    getService(AlfrescoServices.REPOSITORY_STATE);
            if (repositoryState.isBootstrapping()) {
                return null;
            }
            ExecuteActionJobRegistry registry = (ExecuteActionJobRegistry) data.get("executeActionJobRegistry");
            List<ExecuteActionByDateWork> works = registry.getWorks();
            if (!works.isEmpty()) {
                logger.info("Start executing actions. Number of works: " + works.size());
                for (int i = 0; i < works.size(); i++) {
                    try {
                        executeAction(serviceRegistry, works.get(i), i);
                    } catch (Exception e) {
                        logger.error("Work #" + i + " failed", e);
                    }
                }
            }
            return null;
        });
    }

    private void executeAction(ServiceRegistry serviceRegistry, final ExecuteActionByDateWork executeActionData,
                               int index) {

        if (!executeActionData.isEnabled()) {
            return;
        }

        String workName = "Execute action work #" + index;
        logger.info(workName + ": " + executeActionData);

        TransactionService transactionService = serviceRegistry.getTransactionService();
        RetryingTransactionHelper transactionHelper = transactionService.getRetryingTransactionHelper();

        List<NodeRef> nodeRefs = transactionHelper.doInTransaction(executeActionData::queryNodes);

        logger.info(workName + ": nodes to process: " + nodeRefs.size());
        if (!nodeRefs.isEmpty()) {
            BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>(
                    workName + " execute action",
                    transactionHelper,
                    new ChangeStatusWorkProvider(nodeRefs),
                    WORKER_THREADS, BATCH_SIZE,
                    null, logger, LOGGING_INTERVAL
            );

            batchProcessor.process(new ExecuteActionWorker(executeActionData), true);
        }
    }

    private static class ExecuteActionWorker extends BatchProcessor.BatchProcessWorkerAdaptor<NodeRef> {

        private ExecuteActionByDateWork executeActionByDateWork;

        ExecuteActionWorker(ExecuteActionByDateWork executeActionByDateWork) {
            this.executeActionByDateWork = executeActionByDateWork;
            if (this.executeActionByDateWork == null) {
                throw new IllegalArgumentException("ExecuteActionByDateWork can not be null");
            }
        }

        @Override
        public void process(NodeRef entry) {
            try {
                executeActionByDateWork.process(entry);
            } catch (Exception e) {
                throw new RuntimeException("Exception on execute action. nodeRef: " + entry, e);
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
