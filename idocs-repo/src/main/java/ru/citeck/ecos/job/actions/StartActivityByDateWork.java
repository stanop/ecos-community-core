package ru.citeck.ecos.job.actions;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.ActivityCommonService;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.service.EcosCoreServices;

@Slf4j
public class StartActivityByDateWork extends ExecuteActionByDateWork {

    private String activityTitle;

    private CaseActivityService caseActivityService;
    private ActivityCommonService activityCommonService;

    @Override
    public void init() {
        ParameterCheck.mandatory("activityTitle", activityTitle);
        super.init();
        this.caseActivityService = EcosCoreServices.getCaseActivityService(serviceRegistry);
        this.activityCommonService = (ActivityCommonService) serviceRegistry
            .getService(CiteckServices.ACTIVITY_COMMON_SERVICE);
    }

    @Override
    public void process(NodeRef caseRef) {
        ActivityRef rootActivityRef = activityCommonService.composeRootActivityRef(caseRef);
        CaseActivity activity = caseActivityService.getActivityByTitle(rootActivityRef, activityTitle, true);
        if (activity != null) {
            log.debug("Start activity <" + activityTitle + "> on case: " + caseRef);
            caseActivityService.startActivity(activity.getActivityRef());
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
