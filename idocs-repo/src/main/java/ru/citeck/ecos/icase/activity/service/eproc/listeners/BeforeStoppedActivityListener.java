package ru.citeck.ecos.icase.activity.service.eproc.listeners;

import ru.citeck.ecos.icase.activity.dto.ActivityRef;

public interface BeforeStoppedActivityListener extends OrderedListener {

    void beforeStoppedActivity(ActivityRef activityRef);

}
