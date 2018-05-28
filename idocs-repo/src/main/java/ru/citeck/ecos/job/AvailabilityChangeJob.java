package ru.citeck.ecos.job;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
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

import java.util.List;

public class AvailabilityChangeJob extends AbstractScheduledLockedJob {
    private static final Log logger = LogFactory.getLog(AvailabilityChangeJob.class);
    private NodeService nodeService;
    private SearchService searchService;
    private AvailabilityService availabilityService;
    private TransactionService transactionService;

    @Override
    public void executeJob(final JobExecutionContext jobContext) throws JobExecutionException {
        final JobDataMap data = jobContext.getJobDetail().getJobDataMap();
        AuthenticationUtil.runAsSystem(() -> {
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
            ResultSet eventsToStartResultSet = getEventsToStart();
            ResultSet eventsToStopResultSet = getEventsToStop();
            if (eventsToStartResultSet != null && eventsToStartResultSet.getNodeRefs() != null) {
                makeUsersUnavailable(eventsToStartResultSet.getNodeRefs());
            }
            if (eventsToStopResultSet != null && eventsToStopResultSet.getNodeRefs() != null) {
                makeUsersAvailable(eventsToStopResultSet.getNodeRefs());
            }
            logger.info("AvailabilityChangeJob is finished");
            return null;
        });
    }

    private void makeUsersAvailable(List<NodeRef> eventsToStop) {
        //events to start

        if (eventsToStop == null) {
            return;
        }

        for (NodeRef event : eventsToStop) {
            try {
                processEvent(event, true);
            } catch (Exception e) {
                logger.error("Event " + event + " cant be stopped", e);
            }
        }
    }

    private ResultSet getEventsToStop() {
        String eventsToStopQuery = "TYPE:\"deputy:absenceEvent\"" +
                " AND @deputy\\:endAbsence:[MIN TO NOW]" +
                " AND @deputy\\:eventFinished:false" +
                " AND @deputy\\:eventStarted:true";
        if (logger.isDebugEnabled()) {
            logger.debug("eventsToStopQuery.query = " + eventsToStopQuery);
        }
        return searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO,
                eventsToStopQuery);
    }

    private void makeUsersUnavailable(List<NodeRef> eventsToStart) {
        if (eventsToStart == null) {
            return;
        }
        for (NodeRef event : eventsToStart) {
            try {
                processEvent(event, false);
            } catch (Exception e) {
                logger.error("Event " + event + " cant be started", e);
            }
        }
    }

    private ResultSet getEventsToStart() {
        //events to start
        String eventsToStartQuery = "TYPE:\"deputy:absenceEvent\"" +
                " AND @deputy\\:startAbsence:[MIN TO NOW]" +
                " AND (@deputy\\:endAbsence:<NOW TO MAX> OR ISNULL:\"deputy:endAbsence\")" +
                " AND @deputy\\:eventFinished:false" +
                " AND @deputy\\:eventStarted:false";
        if (logger.isDebugEnabled()) {
            logger.debug("eventsToStartQuery.query = " + eventsToStartQuery);
        }
        return searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO,
                eventsToStartQuery);
    }

    private void processEvent(final NodeRef event, final boolean availability) {
        final NodeRef personToChangeAvailability = nodeService.getTargetAssocs(event, DeputyModel.ASSOC_USER) != null
                && !nodeService.getTargetAssocs(event, DeputyModel.ASSOC_USER).isEmpty()
                ? nodeService.getTargetAssocs(event, DeputyModel.ASSOC_USER).get(0).getTargetRef()
                : null;
        if (personToChangeAvailability == null) {
            throw new IllegalArgumentException("Person to change availability is null. Event nodeRef = " + event);
        }
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            if (availability) {
                nodeService.setProperty(event, DeputyModel.PROP_EVENT_FINISHED, true);
            } else {
                nodeService.setProperty(event, DeputyModel.PROP_EVENT_STARTED, true);
            }
            availabilityService.setUserAvailability(personToChangeAvailability, availability);
            return null;
        });

    }
}
