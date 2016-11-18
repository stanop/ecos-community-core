package ru.citeck.ecos.behavior.status;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.service.AlfrescoServices;
import ru.citeck.ecos.service.CiteckServices;

import java.util.Collections;
import java.util.List;

/**
 * Job to find nodes in specified status and change it if current date more or equals to data stored in specified field
 *
 * @author Pavel Simonov
 */
public class ChangeStatusByDate implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeStatusByDate.class);

    private static final String SEARCH_QUERY = "TYPE:\"%s\" AND @%s:[MIN TO NOW] AND @icase\\:caseStatusAssoc_added:\"%s\"";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap data = context.getJobDetail().getJobDataMap();
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {
                final ServiceRegistry serviceRegistry = (ServiceRegistry) data.get("serviceRegistry");
                RepositoryState repositoryState = (RepositoryState) serviceRegistry.getService(AlfrescoServices.REPOSITORY_STATE);
                if (repositoryState.isBootstrapping()) {
                    return null;
                }
                final TransactionService transactionService = serviceRegistry.getTransactionService();
                final List<String> transitions = (List) data.get("transitions");
                final RetryingTransactionHelper transactionHelper = transactionService.getRetryingTransactionHelper();

                transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                    @Override
                    public Void execute() throws Throwable {
                        for (String transition : transitions) {
                            makeTransition(serviceRegistry, transition);
                        }
                        return null;
                    }
                });
                return null;
            }
        });
    }

    private void makeTransition(ServiceRegistry serviceRegistry, String transitionData) {
        SearchService searchService = serviceRegistry.getSearchService();
        NamespaceService namespaceService = serviceRegistry.getNamespaceService();
        CaseStatusService caseStatusService = (CaseStatusService) serviceRegistry.getService(CiteckServices.CASE_STATUS_SERVICE);

        Transition transition = new Transition(transitionData, namespaceService);
        List<NodeRef> nodeRefs = findNodes(caseStatusService, searchService, transition);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Found " + nodeRefs.size() + " nodes which required transition.");
        }
        for (NodeRef ref : nodeRefs) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Make transition for node '" + ref + "' transition: " + transitionData);
            }
            caseStatusService.setStatus(ref, getStatus(caseStatusService, transition.toStatus));
        }
    }

    private List<NodeRef> findNodes(CaseStatusService caseStatusService, SearchService searchService, Transition transition) {
        NodeRef statusRef = getStatus(caseStatusService, transition.fromStatus);
        String query = String.format(SEARCH_QUERY, transition.className, transition.fieldName, statusRef);
        ResultSet results = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                                                SearchService.LANGUAGE_FTS_ALFRESCO, query);

        return results != null ? results.getNodeRefs() : Collections.<NodeRef>emptyList();
    }

    private NodeRef getStatus(CaseStatusService caseStatusService, String name) {
        NodeRef statusRef = caseStatusService.getStatusByName(name);
        if (statusRef == null) {
            throw new AlfrescoRuntimeException("Status not found: " + name);
        }
        return statusRef;
    }

    private static class Transition {
        final QName className;
        final QName fieldName;
        final String fromStatus;
        final String toStatus;

        Transition(String data, NamespaceService namespaceService) {
            String[] tokens = data.split(",");
            if (tokens.length != 4) {
                throw new IllegalArgumentException("Transition description must contains 4 tokens " +
                                                   "delimited with ','. Actual description: '" + data + "'");
            }
            className = QName.resolveToQName(namespaceService, tokens[0]);
            fieldName = QName.resolveToQName(namespaceService, tokens[1]);
            fromStatus = tokens[2];
            toStatus = tokens[3];
        }
    }
}
