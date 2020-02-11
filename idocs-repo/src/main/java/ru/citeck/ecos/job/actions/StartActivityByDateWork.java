package ru.citeck.ecos.job.actions;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.service.EcosCoreServices;

@Slf4j
public class StartActivityByDateWork extends ExecuteActionByDateWork {

    private String activityTitle;

    private CaseActivityService caseActivityService;

    @Override
    public void init() {
        ParameterCheck.mandatory("activityTitle", activityTitle);
        super.init();
        this.caseActivityService = EcosCoreServices.getCaseActivityService(serviceRegistry);
    }

    @Override
    public void process(NodeRef entryNodeRef) {

        CaseActivity activity = caseActivityService.getActivityByTitle(entryNodeRef.toString(), activityTitle, true);
        if (activity != null) {
            NodeRef activityNodeRef = new NodeRef(activity.getId());
            if (serviceRegistry.getNodeService().exists(activityNodeRef)) {
                log.debug("Start activity <" + activityTitle + "> on case: " + entryNodeRef);
                caseActivityService.startActivity(activity);
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
