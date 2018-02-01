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
public class StartStageByDateWork extends ExecuteActionByDateWork {

    private static final Log logger = LogFactory.getLog(StartStageByDateWork.class);

    private String stageTitle;

    private CaseActivityService caseActivityService;

    @Override
    public void init() {
        ParameterCheck.mandatory("stageTitle", stageTitle);
        super.init();
        this.caseActivityService = EcosCoreServices.getCaseActivityService(serviceRegistry);
    }

    @Override
    public void process(NodeRef entry) {
        NodeRef activity = caseActivityService.getActivityByTitle(entry, stageTitle, true);
        if (activity != null && serviceRegistry.getNodeService().exists(activity)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Start stage <" + stageTitle + "> on case: " + entry);
            }
            caseActivityService.startActivity(activity);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " ActivityTitle: " + stageTitle;
    }

    public void setStageTitle(String stageTitle) {
        this.stageTitle = stageTitle;
    }
}
