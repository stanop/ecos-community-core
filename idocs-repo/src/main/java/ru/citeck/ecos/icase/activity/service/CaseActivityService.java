package ru.citeck.ecos.icase.activity.service;

import lombok.NonNull;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;

import java.util.List;

public interface CaseActivityService {

    void startActivity(CaseActivity activity);

    void stopActivity(CaseActivity activity);

    void restartChildActivity(CaseActivity parentId, CaseActivity childId);

    void reset(String documentId);

    CaseActivity getActivity(String activityId);

    List<CaseActivity> getActivities(String documentId);

    List<CaseActivity> getActivities(String documentId, QNamePattern type);

    List<CaseActivity> getActivities(String documentId, QName assocType, QNamePattern type);

    List<CaseActivity> getActivities(String documentId, QName assocType, QNamePattern type, boolean recurse);

    List<CaseActivity> getStartedActivities(String documentId);

    CaseActivity getActivityByTitle(String documentId, String title, boolean recurse);

    String getDocumentId(String activityId);

    void setParent(String activityId, String parentId);

    /**
     * Set new parent in result list method getActivities()
     *
     * @param activity activity.
     * @param newIndex Index define activities order when "getActivities" method called.
     *                 If this value less than zero, than activity moved to the beginning.
     *                 If this value greater or equal to activities count, than activity moved to the end.
     *                 In other case activity position in result list of "getActivities" equals to this parameter.
     */
    void setParentInIndex(@NonNull CaseActivity activity, int newIndex);

    boolean hasActiveChildren(CaseActivity activity);

}
