package ru.citeck.ecos.icase.activity.service;

import lombok.NonNull;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;

import java.util.List;

public interface CaseActivityDelegate {

    void startActivity(CaseActivity activity);

    void stopActivity(CaseActivity activity);

    void restartChildActivity(CaseActivity parentId, CaseActivity childId);

    void reset(String documentId);

    CaseActivity getActivity(String activityId);

    List<CaseActivity> getActivities(String documentId);

    List<CaseActivity> getActivities(String documentId, boolean recurse);

    List<CaseActivity> getStartedActivities(String documentId);

    CaseActivity getActivityByTitle(String documentId, String title, boolean recurse);

    String getDocumentId(String activityId);

    void setParent(String activityId, String parentId);

    void setParentInIndex(@NonNull CaseActivity activity, int newIndex);

    boolean hasActiveChildren(CaseActivity activity);

}
