package ru.citeck.ecos.job.actions;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.service.EcosCoreServices;

/**
 * @author Roman Makarskiy
 */
public class StartStageByDateWork extends ExecuteActionByDateWork {

    private static final Log logger = LogFactory.getLog(StartStageByDateWork.class);

    private CaseActivityService caseActivityService;

    @Override
    public void init() {
        super.init();
        this.caseActivityService = EcosCoreServices.getCaseActivityService(serviceRegistry);
    }

    @Override
    public void process(NodeRef entry) {
        NodeRef activity = caseActivityService.getActivityByTitle(entry, actionKey, true);
        if (activity != null && serviceRegistry.getNodeService().exists(activity)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Start stage <" + actionKey + "> on case: " + entry);
            }
            caseActivityService.startActivity(activity);
        }
    }
}
