package ru.citeck.ecos.migration;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.utils.TransactionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Parent class for module components
 * that process all users (cm:person type)
 * {@code executeOnceOnly} is set to {@code true} by default
 * @see AbstractModuleComponent
 * @since 3.4.0
 *
 */
public abstract class UsersBatchProcessing extends AbstractModuleComponent {

    private static final Log logger = LogFactory.getLog(UsersBatchProcessing.class);

    private static final String PROCESS_NAME = "users-batch-processing";
    private static final int BATCH_SIZE = 10;
    private static final int WORKER_THREADS = 4;
    private static final int LOGGING_INTERVAL = 200;

    @Autowired
    protected PersonService personService;

    @Autowired
    protected ServiceRegistry serviceRegistry;

    @Autowired
    protected RetryingTransactionHelper retryingTransactionHelper;

    @Override
    protected void executeInternal() {
        TransactionUtils.doAfterCommit(() -> {

            logger.info("Start execution...");
            beforeProcessing();

            BatchProcessor<PersonService.PersonInfo> batchProcessor = new BatchProcessor<>(
                    PROCESS_NAME,
                    retryingTransactionHelper,
                    new UsersBatchProcessing.PresonInfoWorkProvider(personService),
                    WORKER_THREADS, BATCH_SIZE,
                    null, logger, LOGGING_INTERVAL
            );

            startProcessing(batchProcessor);
            logger.info("Finished executing");
        });
    }



    protected void beforeProcessing() {

    }

    protected abstract void startProcessing(BatchProcessor<PersonService.PersonInfo> batchProcessor);

    private static class PresonInfoWorkProvider implements BatchProcessWorkProvider<PersonService.PersonInfo> {

        private int skip = 0;
        private int maxItems = 480;
        private boolean hasMore = true;

        private PersonService personService;

        PresonInfoWorkProvider(PersonService personService) {
            this.personService = personService;
        }

        @Override
        public int getTotalEstimatedWorkSize() {
            return personService.countPeople();
        }

        @Override
        public Collection<PersonService.PersonInfo> getNextWork() {
            if (!hasMore) {
                return Collections.emptyList();
            }
            PagingResults<PersonService.PersonInfo> people = personService.getPeople(
                    null,
                    null,
                    null,
                    new PagingRequest(skip, maxItems));
            List<PersonService.PersonInfo> page = people.getPage();
            skip += page.size();
            hasMore = people.hasMoreItems();
            return page;
        }
    }
}
