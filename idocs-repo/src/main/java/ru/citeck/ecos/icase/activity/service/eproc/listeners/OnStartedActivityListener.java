package ru.citeck.ecos.icase.activity.service.eproc.listeners;

import ru.citeck.ecos.icase.activity.dto.ActivityRef;

public interface OnStartedActivityListener extends OrderedListener {

    void onStartedActivity(ActivityRef activityRef);

}
