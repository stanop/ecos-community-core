package ru.citeck.ecos.jobs;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import ru.citeck.ecos.model.BpmModel;
import ru.citeck.ecos.model.SecurityWorkflowModel;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_FTS_ALFRESCO;

public class UpdateInactivityPeriodExecutor {

    private static final Logger LOGGER = Logger.getLogger(UpdateInactivityPeriodExecutor.class);

    private static final long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);

    private static final String SEARCH_TASK_BY_NOT_STATUS = "TYPE:'samwf:incomePackageTask' AND !bpm:status:%s";
    private static final String COMPLETED_STATUS          = "Completed";
    
    private static final QName BPM_STATUS_PROP              = QName.createQName(BpmModel.BPM_NAMESPASE, "status");
    private static final QName BPM_START_DATE_PROP          = QName.createQName(BpmModel.BPM_NAMESPASE, "startDate");
    private static final QName SAMWF_INACTIVITY_PERIOD_PROP = QName.createQName(SecurityWorkflowModel.SAMWF_NAMESPACE, "inactivityPeriod");

    private NodeService   nodeService;
    private SearchService searchService;
    
    private UpdateInactivityPeriodExecutor() {}
    
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    private static String escapeCharacters(String input) {
        if (input == null) {
            return null;
        }

        final String[] characters = {" "};

        String output = input;

        for (int i = 0; i < characters.length; i++) {
            if (input.contains(characters[i])) {
                output = input.replace(characters[i], "\\" + characters[i]);
                input  = output;
            }
        }

        return output;
    }

    private List<NodeRef> searchNodes(String language, String query) {
        ResultSet searchResults = searchService.query(STORE_REF_WORKSPACE_SPACESSTORE, language, query);

        if (searchResults != null) {
            try {
                return searchResults.getNodeRefs();
            } finally {
                searchResults.close();
            }
        }

        return null;
    }
    
    private List<NodeRef> getInactiveTasks() {
        return searchNodes(LANGUAGE_FTS_ALFRESCO, String.format(SEARCH_TASK_BY_NOT_STATUS, escapeCharacters(COMPLETED_STATUS)));
    }
    
    public void execute() {
        LOGGER.info("Start updateInactivityPeriodExecutor");
        
        List<NodeRef> inactiveTasks = getInactiveTasks();

        if (CollectionUtils.isNotEmpty(inactiveTasks)) {
            LOGGER.debug("Found " + inactiveTasks.size() + " inactive incoming tasks");
            
            for (NodeRef inactiveTask : inactiveTasks) {
                try {
                    if (nodeService.exists(inactiveTask)) {
                        Date startDate = (Date) nodeService.getProperty(inactiveTask, BPM_START_DATE_PROP);
                        
                        if (startDate != null) {
                            Date currentDate = new Date();

                            int diffDays = (int) Math.round((currentDate.getTime() - startDate.getTime()) / (double) MS_PER_DAY);
                            
                            if (diffDays >= 0) {
                                String status = (String) nodeService.getProperty(inactiveTask, BPM_STATUS_PROP);

                                if (!COMPLETED_STATUS.equals(status)) {
                                    nodeService.setProperty(inactiveTask, SAMWF_INACTIVITY_PERIOD_PROP, diffDays);
                                    LOGGER.info("The period of inactivity in the task \"" + inactiveTask + "\" has been updated to the value " + diffDays);
                                }
                            }
                        }
                    }
                } catch (Exception exc) {
                    LOGGER.error("Error while working with the repository", exc);
                }
            }
        } else {
            LOGGER.info("Inactive incoming tasks were not found");
        }
        
        LOGGER.info("Finish updateInactivityPeriodExecutor");
    }
}