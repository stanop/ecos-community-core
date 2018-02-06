package ru.citeck.ecos.job.actions;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.service.EcosCoreServices;

/**
 * @author Roman Makarskiy
 */
public class StartActivityByDateWork extends ExecuteActionByDateWork {

    private static final Log logger = LogFactory.getLog(StartActivityByDateWork.class);

    private String activityTitle;

    private CaseActivityService caseActivityService;

    @Override
    public void init() {
        ParameterCheck.mandatory("activityTitle", activityTitle);
        super.init();
        this.caseActivityService = EcosCoreServices.getCaseActivityService(serviceRegistry);
    }

    @Override
    public void process(NodeRef entry) {
        NodeRef activity = caseActivityService.getActivityByTitle(entry, activityTitle, true);
        if (activity != null && serviceRegistry.getNodeService().exists(activity)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Start activity <" + activityTitle + "> on case: " + entry);
            }
            caseActivityService.startActivity(activity);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " ActivityTitle: " + activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }
}
