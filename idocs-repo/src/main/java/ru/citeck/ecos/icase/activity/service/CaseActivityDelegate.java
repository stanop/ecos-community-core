package ru.citeck.ecos.icase.activity.service;

import lombok.NonNull;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;

import java.util.List;

public interface CaseActivityDelegate {

    void startActivity(ActivityRef activityRef);

    void stopActivity(ActivityRef activityRef);

    void reset(ActivityRef activityRef);

    CaseActivity getActivity(ActivityRef activityRef);

    List<CaseActivity> getActivities(ActivityRef activityRef);

    List<CaseActivity> getActivities(ActivityRef activityRef, boolean recurse);

    List<CaseActivity> getStartedActivities(ActivityRef activityRef);

    CaseActivity getActivityByName(ActivityRef activityRef, String name, boolean recurse);

    CaseActivity getActivityByTitle(ActivityRef activityRef, String title, boolean recurse);

    void setParent(ActivityRef activityRef, ActivityRef parentRef);

    void setParentInIndex(@NonNull ActivityRef activityRef, int newIndex);

    boolean hasActiveChildren(ActivityRef activityRef);

}
