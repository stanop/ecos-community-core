package ru.citeck.ecos.icase.activity.service.eproc.listeners;

import ru.citeck.ecos.icase.activity.dto.ActivityRef;

public interface OnStoppedActivityListener extends OrderedListener {

    void onStoppedActivity(ActivityRef activityRef);

}
