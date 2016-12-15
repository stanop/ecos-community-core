package ru.citeck.ecos.job;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import ru.citeck.ecos.deputy.AvailabilityService;
import ru.citeck.ecos.model.DeputyModel;

public class AvailabilityChangeJob extends AbstractScheduledLockedJob {
    private static final Log logger = LogFactory.getLog(AvailabilityChangeJob.class);
    private NodeService nodeService;
    private SearchService searchService;
    private AvailabilityService availabilityService;
    private TransactionService transactionService;

    @Override
    public void executeJob(final JobExecutionContext jobContext) throws JobExecutionException {
        final JobDataMap data = jobContext.getJobDetail().getJobDataMap();
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {
                logger.info("AvailabilityChangeJob is started");
                if (searchService == null) {
                    searchService = (SearchService) data.get("searchService");
                }
                if (nodeService == null) {
                    nodeService = (NodeService) data.get("nodeService");
                }
                if (availabilityService == null) {
                    availabilityService = (AvailabilityService) data.get("availabilityService");
                }
                if (transactionService == null) {
                    final ServiceRegistry serviceRegistry = (ServiceRegistry) data.get("serviceRegistry");
                    transactionService = serviceRegistry.getTransactionService();
                }

                makeUsersUnavailable();
                makeUsersAvailable();
                logger.info("AvailabilityChangeJob is finished");
                return null;
            }
        });
    }

    private void makeUsersAvailable() {
        //events to start
        String eventsToStopQuery = "TYPE:\"deputy:absenceEvent\"" +
                " AND @deputy\\:endAbsence:[MIN TO NOW]" +
                " AND @deputy\\:eventFinished:false"+
                " AND @deputy\\:eventStarted:true";
        if (logger.isDebugEnabled()) {
            logger.debug("eventsToStopQuery.query = " + eventsToStopQuery);
        }
        ResultSet eventsToStopResultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, eventsToStopQuery);

        for (NodeRef event : eventsToStopResultSet.getNodeRefs()) {
            try {
                processEvent(event, true);
            } catch (Exception e) {
                logger.error("Event " + event + " cant be stopped", e);
            }
        }
    }

    private void makeUsersUnavailable() {
        //events to start
        String eventsToStartQuery = "TYPE:\"deputy:absenceEvent\"" +
                " AND @deputy\\:startAbsence:[MIN TO NOW]" +
                " AND (@deputy\\:endAbsence:{NOW TO MAX} OR ISNULL:\"deputy:endAbsence\")" +
                " AND @deputy\\:eventFinished:false" +
                " AND @deputy\\:eventStarted:false";
        if (logger.isDebugEnabled()) {
            logger.debug("eventsToStartQuery.query = " + eventsToStartQuery);
        }
        ResultSet eventsToStartResultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, eventsToStartQuery);

        for (NodeRef event : eventsToStartResultSet.getNodeRefs()) {
            try {
                processEvent(event, false);
            } catch (Exception e) {
                logger.error("Event " + event + " cant be started", e);
            }
        }
    }

    private void processEvent(final NodeRef event, final boolean availability) {
        final NodeRef personToChangeAvailability = nodeService.getTargetAssocs(event, DeputyModel.ASSOC_USER) != null
                && !nodeService.getTargetAssocs(event, DeputyModel.ASSOC_USER).isEmpty()
                ? nodeService.getTargetAssocs(event, DeputyModel.ASSOC_USER).get(0).getTargetRef()
                : null;
        if (personToChangeAvailability == null) {
            throw new IllegalArgumentException("Person to change availability is null. Event nodeRef = " + event);
        }
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable {
                if (availability) {
                    nodeService.setProperty(event, DeputyModel.PROP_EVENT_FINISHED, true);
                } else {
                    nodeService.setProperty(event, DeputyModel.PROP_EVENT_STARTED, true);
                }
                availabilityService.setUserAvailability(personToChangeAvailability, availability);
                return null;
            }
        });

    }
}
