package ru.citeck.ecos.authority;

import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.migration.UsersBatchProcessing;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Roman Makarskiy
 */
public class AddUsersToAllGroupModuleComponent extends UsersBatchProcessing {

    private static final String GROUP_ALL = "GROUP_all";

    private static final List<String> skipUsers = Arrays.asList("System", "guest");

    @Autowired
    private AuthorityService authorityService;

    @Override
    protected void beforeProcessing(){
        if (!authorityService.authorityExists(GROUP_ALL)) {
            throw new IllegalStateException("Error, " + GROUP_ALL + " not found");
        }
    }

    @Override
    protected void startProcessing(BatchProcessor<PersonService.PersonInfo> batchProcessor) {
        batchProcessor.process(new AddUsersToAllGroupModuleComponent.AddAllUsersToGroupWorker(authorityService), true);
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
}
