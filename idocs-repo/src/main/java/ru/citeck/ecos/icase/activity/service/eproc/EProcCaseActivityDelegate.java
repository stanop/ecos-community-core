package ru.citeck.ecos.icase.activity.service.eproc;

import org.springframework.stereotype.Service;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.CaseActivityDelegate;

import java.util.List;

@Service
public class EProcCaseActivityDelegate implements CaseActivityDelegate {

    @Override
    public void startActivity(CaseActivity activity) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void stopActivity(CaseActivity activity) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void restartChildActivity(CaseActivity parentId, CaseActivity childId) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void reset(String documentId) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public CaseActivity getActivity(String activityId) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<CaseActivity> getActivities(String documentId) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<CaseActivity> getActivities(String documentId, boolean recurse) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<CaseActivity> getStartedActivities(String documentId) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public CaseActivity getActivityByTitle(String documentId, String title, boolean recurse) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public String getDocumentId(String activityId) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void setParent(String activityId, String parentId) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void setParentInIndex(CaseActivity activity, int newIndex) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public boolean hasActiveChildren(CaseActivity activity) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
