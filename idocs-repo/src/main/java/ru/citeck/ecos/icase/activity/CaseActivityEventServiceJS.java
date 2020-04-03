package ru.citeck.ecos.icase.activity;

import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.EventRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventService;
import ru.citeck.ecos.utils.ActivityUtilsJS;
import ru.citeck.ecos.utils.AlfActivityUtils;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

public class CaseActivityEventServiceJS extends AlfrescoScopableProcessorExtension {

    private CaseActivityEventService caseActivityEventService;
    private AlfActivityUtils alfActivityUtils;

    public void fireEvent(Object node, String eventType) {
        ActivityRef activityRef = ActivityUtilsJS.getActivityRef(node, alfActivityUtils);
        caseActivityEventService.fireEvent(activityRef, eventType);
    }

    public void fireConcreteEvent(Object event) {
        EventRef eventRef = ActivityUtilsJS.getEventRef(event, alfActivityUtils);
        caseActivityEventService.fireConcreteEvent(eventRef);
    }

    public boolean checkConditions(Object event) {
        EventRef eventRef = ActivityUtilsJS.getEventRef(event, alfActivityUtils);
        return caseActivityEventService.checkConditions(eventRef);
    }

    public void setCaseActivityEventService(CaseActivityEventService caseActivityEventService) {
        this.caseActivityEventService = caseActivityEventService;
    }

    public void setAlfActivityUtils(AlfActivityUtils alfActivityUtils) {
        this.alfActivityUtils = alfActivityUtils;
    }

}
