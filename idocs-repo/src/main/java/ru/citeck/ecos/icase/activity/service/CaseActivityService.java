package ru.citeck.ecos.icase.activity.service;

import lombok.NonNull;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;

import java.util.List;

public interface CaseActivityService {

    void startActivity(ActivityRef activity);

    void stopActivity(ActivityRef activity);

    void reset(ActivityRef activityRef);

    CaseActivity getActivity(ActivityRef activityRef);

    List<CaseActivity> getActivities(ActivityRef activityRef);

    List<CaseActivity> getActivities(ActivityRef activityRef, boolean recurse);

    List<CaseActivity> getStartedActivities(ActivityRef activityRef);

    CaseActivity getActivityByTitle(ActivityRef activityRef, String title, boolean recurse);

    void setParent(ActivityRef activityRef, ActivityRef parentRef);

    /**
     * Set new parent in result list method getActivities()
     *
     * @param activityRef activityRef.
     * @param newIndex    Index define activities order when "getActivities" method called.
     *                    If this value less than zero, than activity moved to the beginning.
     *                    If this value greater or equal to activities count, than activity moved to the end.
     *                    In other case activity position in result list of "getActivities" equals to this parameter.
     */
    void setParentInIndex(@NonNull ActivityRef activityRef, int newIndex);

    boolean hasActiveChildren(ActivityRef activityRef);

}
