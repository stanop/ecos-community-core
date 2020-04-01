package ru.citeck.ecos.icase.activity.service.eproc.listeners;

import ru.citeck.ecos.icase.activity.dto.ActivityRef;

public interface OnResetActivityListener extends OrderedListener {

    void onResetActivity(ActivityRef activityRef);

}
