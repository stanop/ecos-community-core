package ru.citeck.ecos.icase.activity.service.eproc.listeners;

import ru.citeck.ecos.icase.activity.dto.ActivityRef;

public interface BeforeStartedActivityListener extends OrderedListener {

    void beforeStartedActivity(ActivityRef activityRef);

}
