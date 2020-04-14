package ru.citeck.ecos.icase.activity;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.EventRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventService;
import ru.citeck.ecos.utils.ActivityUtilsJS;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

public class CaseActivityEventServiceJS extends AlfrescoScopableProcessorExtension {

    private ActivityUtilsJS activityUtilsJS;
    private CaseActivityEventService caseActivityEventService;

    public void fireEvent(Object node, String eventType) {
        ActivityRef activityRef = activityUtilsJS.getActivityRef(node);
        caseActivityEventService.fireEvent(activityRef, eventType);
    }

    public void fireConcreteEvent(Object event) {
        EventRef eventRef = activityUtilsJS.getEventRef(event);
        caseActivityEventService.fireConcreteEvent(eventRef);
    }

    public boolean checkConditions(Object event) {
        EventRef eventRef = activityUtilsJS.getEventRef(event);
        return caseActivityEventService.checkConditions(eventRef);
    }

    @Autowired
    public void setActivityUtilsJS(ActivityUtilsJS activityUtilsJS) {
        this.activityUtilsJS = activityUtilsJS;
    }

    public void setCaseActivityEventService(CaseActivityEventService caseActivityEventService) {
        this.caseActivityEventService = caseActivityEventService;
    }

}
