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
import java.util.Properties;

/**
 * Send and remove completed cases job
 */
public class SendAndRemoveCompletedCasesJob extends AbstractScheduledLockedJob {

    /**
     * Constants
     */
    private static final Integer MAX_ITEMS_COUNT = 50;
    private static final String MAX_ITEMS_COUNT_PROPERTY = "citeck.remote.case.service.max.items";

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
     * Global properties
     */
    private Properties globalProperties;

    /**
     * Init job service
     * @param jobDataMap Job data map
     */
    private void init(JobDataMap jobDataMap) {
        searchService = (SearchService) jobDataMap.get("searchService");
        remoteCaseModelService = (RemoteCaseModelService) jobDataMap.get("remoteCaseModelService");
        globalProperties = (Properties) jobDataMap.get("global-properties");
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
            ResultSet resultSet = getDocumentsResultSet();
            List<NodeRef> documents = resultSet.getNodeRefs();
            for (NodeRef documentRef : documents) {
                remoteCaseModelService.sendAndRemoveCaseModelsByDocument(documentRef);
            }
            return null;
        });
    }

    /**
     * Get documents by offset
     * @return Set of documents
     */
    private ResultSet getDocumentsResultSet() {
        SearchParameters parameters = new SearchParameters();
        parameters.addStore(storeRef);
        parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
        parameters.setQuery("TYPE:\"idocs:doc\" " +
                "AND @idocs\\:caseCompleted:true " +
                "AND -@idocs\\:caseModelsSent:true");
        parameters.addSort("@cm:created", true);
        parameters.setMaxItems(getMaxItemsCount());
        return searchService.query(parameters);
    }

    /**
     * Get max items count
     * @return Max items count
     */
    private Integer getMaxItemsCount() {
        if (globalProperties == null) {
            return MAX_ITEMS_COUNT;
        }
        String maxItemsRawValue = globalProperties.getProperty(MAX_ITEMS_COUNT_PROPERTY);
        if (maxItemsRawValue == null) {
            return MAX_ITEMS_COUNT;
        } else {
            return Integer.valueOf(maxItemsRawValue);
        }
    }

}
