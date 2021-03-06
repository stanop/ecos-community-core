package ru.citeck.ecos.icase.activity.service;

import lombok.NonNull;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;

import java.util.List;

public interface CaseActivityDelegate {

    void startActivity(ActivityRef activity);

    void stopActivity(ActivityRef activity);

    void reset(ActivityRef activityRef);

    CaseActivity getActivity(ActivityRef activityRef);

    List<CaseActivity> getActivities(ActivityRef activityRef);

    List<CaseActivity> getActivities(ActivityRef activityRef, boolean recurse);

    List<CaseActivity> getStartedActivities(ActivityRef activityRef);

    CaseActivity getActivityByTitle(ActivityRef activityRef, String title, boolean recurse);

    void setParent(ActivityRef activityRef, ActivityRef parentRef);

    void setParentInIndex(@NonNull ActivityRef activityRef, int newIndex);

    boolean hasActiveChildren(ActivityRef activityRef);

}
