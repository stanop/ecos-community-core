package ru.citeck.ecos.authority;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.utils.TransactionUtils;

import java.util.*;

/**
 * @author Roman Makarskiy
 */
public class AddUsersToAllGroupModuleComponent extends AbstractModuleComponent {

    private static final Log logger = LogFactory.getLog(AddUsersToAllGroupModuleComponent.class);

    private static final String GROUP_ALL = "GROUP_all";

    private static final List<String> skipUsers = Arrays.asList("System", "guest");

    private static final String PROCESS_NAME = "add-users-to-all-group";
    private static final int BATCH_SIZE = 10;
    private static final int WORKER_THREADS = 4;
    private static final int LOGGING_INTERVAL = 200;

    @Autowired
    private PersonService personService;

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private RetryingTransactionHelper retryingTransactionHelper;

    @Override
    protected void executeInternal() {
        TransactionUtils.doAfterCommit(() -> {

            logger.info("Start execution...");

            if (!authorityService.authorityExists(GROUP_ALL)) {
                throw new IllegalStateException("Error, " + GROUP_ALL + " not found");
            }

            AuthenticationUtil.runAsSystem(() -> {
                BatchProcessor<PersonService.PersonInfo> batchProcessor = new BatchProcessor<>(
                        PROCESS_NAME,
                        retryingTransactionHelper,
                        new AddAllUsersToGroupWorkProvider(personService),
                        WORKER_THREADS, BATCH_SIZE,
                        null, logger, LOGGING_INTERVAL
                );

                batchProcessor.process(new AddAllUsersToGroupWorker(authorityService), true);
                return null;
            });

            logger.info("Finished executing");
        });
    }

    private static class AddAllUsersToGroupWorker extends BatchProcessor.BatchProcessWorkerAdaptor<PersonService.PersonInfo> {

        private AuthorityService authorityService;

        AddAllUsersToGroupWorker(AuthorityService authorityService) {
            this.authorityService = authorityService;
        }

        @Override
        public void process(PersonService.PersonInfo personInfo) {
            AuthenticationUtil.runAsSystem(() -> {
                String userName = personInfo.getUserName();
                if (!skipUsers.contains(userName) && !userExistsInGroup(userName)) {
                    authorityService.addAuthority(GROUP_ALL, userName);
                }
                return null;
            });
        }

        private boolean userExistsInGroup(String userName) {
            Set<String> authoritiesForUser = authorityService.getAuthoritiesForUser(userName);
            return authoritiesForUser != null && authoritiesForUser.contains(GROUP_ALL);
        }
    }

    private static class AddAllUsersToGroupWorkProvider implements BatchProcessWorkProvider<PersonService.PersonInfo> {

        private int skip = 0;
        private int maxItems = 500;
        private boolean hasMore = true;

        private PersonService personService;

        AddAllUsersToGroupWorkProvider(PersonService personService) {
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
