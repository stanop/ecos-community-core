package ru.citeck.ecos.icase.activity.service;

import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.EventRef;

public interface CaseActivityEventDelegate {

    void fireEvent(ActivityRef activityRef, String eventType);

    void fireConcreteEvent(EventRef eventRef);

    boolean checkConditions(EventRef eventRef);

}
