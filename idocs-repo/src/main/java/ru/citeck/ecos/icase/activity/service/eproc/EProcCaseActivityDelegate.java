package ru.citeck.ecos.icase.activity.service.eproc;

import org.springframework.stereotype.Service;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.CaseActivityDelegate;

import java.util.List;

@Service
public class EProcCaseActivityDelegate implements CaseActivityDelegate {

    @Override
    public void startActivity(ActivityRef activity) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void stopActivity(ActivityRef activity) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void reset(ActivityRef activityRef) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public CaseActivity getActivity(ActivityRef activityRef) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<CaseActivity> getActivities(ActivityRef activityRef) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<CaseActivity> getActivities(ActivityRef activityRef, boolean recurse) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<CaseActivity> getStartedActivities(ActivityRef activityRef) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public CaseActivity getActivityByTitle(ActivityRef activityRef, String title, boolean recurse) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void setParent(ActivityRef activityRef, ActivityRef parentRef) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void setParentInIndex(ActivityRef activityRef, int newIndex) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public boolean hasActiveChildren(ActivityRef activityRef) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
