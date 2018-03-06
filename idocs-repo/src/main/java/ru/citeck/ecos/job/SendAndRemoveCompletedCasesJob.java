package ru.citeck.ecos.job;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import ru.citeck.ecos.cases.RemoteCaseModelService;
import java.util.List;

/**
 * Send and remove completed cases job
 */
public class SendAndRemoveCompletedCasesJob extends AbstractScheduledLockedJob {

    /**
     * Constants
     */
    private static final Integer MAX_ITEMS_COUNT = 500;

    /**
     * Store reference
     */
    private StoreRef storeRef;

    /**
     * Search service
     */
    private SearchService searchService;

    /**
     * Node service
     */
    private RemoteCaseModelService remoteCaseModelService;

    /**
     * Init job service
     * @param jobDataMap Job data map
     */
    private void init(JobDataMap jobDataMap) {
        searchService = (SearchService) jobDataMap.get("searchService");
        remoteCaseModelService = (RemoteCaseModelService) jobDataMap.get("remoteCaseModelService");
        storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
    }

    /**
     * This is the method that should be implemented by any extension of the
     * abstract class. It won't need to worry about any lockings of the job and
     * can focus only on its specific task.
     *
     * @param jobContext context of the execution for retrieving services, etc
     * @throws JobExecutionException if a job fails to execute
     */
    @Override
    public void executeJob(JobExecutionContext jobContext) throws JobExecutionException {
        init(jobContext.getJobDetail().getJobDataMap());
        AuthenticationUtil.runAsSystem(() -> {
            int skipCount = 0;
            boolean hasMore;
            ResultSet resultSet = getDocumentsResultSetByOffset(skipCount);
            /** Start processing */
            do {
                List<NodeRef> documents = resultSet.getNodeRefs();
                hasMore = resultSet.hasMore();
                /** Process each document */
                for (NodeRef documentRef : documents) {
                    remoteCaseModelService.sendAndRemoveCaseModelsByDocument(documentRef);
                }
                skipCount += documents.size();
                resultSet = getDocumentsResultSetByOffset(skipCount);
            } while (hasMore);
            return null;
        });
    }

    /**
     * Get documents by offset
     * @param offset Offset
     * @return Set of documents
     */
    private ResultSet getDocumentsResultSetByOffset(Integer offset) {
        SearchParameters parameters = new SearchParameters();
        parameters.addStore(storeRef);
        parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
        parameters.setQuery("TYPE:\"idocs:doc\" " +
                "AND @idocs\\:caseCompleted:true " +
                "AND -@idocs\\:caseModelsSent:true");
        parameters.addSort("@cm:created", true);
        parameters.setMaxItems(MAX_ITEMS_COUNT);
        parameters.setSkipCount(offset);
        return searchService.query(parameters);
    }

}
