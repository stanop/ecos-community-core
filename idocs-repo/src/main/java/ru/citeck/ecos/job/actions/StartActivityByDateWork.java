package ru.citeck.ecos.job.actions;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.service.EcosCoreServices;
import ru.citeck.ecos.utils.AlfActivityUtils;

@Slf4j
public class StartActivityByDateWork extends ExecuteActionByDateWork {

    private String activityTitle;

    private CaseActivityService caseActivityService;
    private AlfActivityUtils alfActivityUtils;
    private NodeService nodeService;

    @Override
    public void init() {
        ParameterCheck.mandatory("activityTitle", activityTitle);
        super.init();
        this.caseActivityService = EcosCoreServices.getCaseActivityService(serviceRegistry);
        this.alfActivityUtils = (AlfActivityUtils) serviceRegistry.getService(CiteckServices.ALF_ACTIVITY_UTILS);
        this.nodeService = serviceRegistry.getNodeService();
    }

    @Override
    public void process(NodeRef entryNodeRef) {

        ActivityRef rootActivityRef = alfActivityUtils.composeActivityRef(entryNodeRef);
        CaseActivity activity = caseActivityService.getActivityByTitle(rootActivityRef, activityTitle, true);
        if (activity != null) {
            NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activity.getActivityRef());
            if (nodeService.exists(activityNodeRef)) {
                log.debug("Start activity <" + activityTitle + "> on case: " + entryNodeRef);
                caseActivityService.startActivity(activity.getActivityRef());
            } else {
                log.warn("Cannot start activity. NodeService not found stored CaseActivity with id: " + activityNodeRef);
            }
        } else {
            log.warn("Cannot start activity. CaseActivity is null");
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
