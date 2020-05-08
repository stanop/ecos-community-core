package ru.citeck.ecos.icase.activity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.dto.CaseServiceType;
import ru.citeck.ecos.icase.activity.service.alfresco.AlfrescoCaseActivityDelegate;
import ru.citeck.ecos.icase.activity.service.eproc.EProcCaseActivityDelegate;

import java.util.List;

@Slf4j
public class CaseActivityServiceImpl implements CaseActivityService {

    private AlfrescoCaseActivityDelegate alfrescoDelegate;
    private EProcCaseActivityDelegate eprocDelegate;

    @Override
    public void startActivity(ActivityRef activityRef) {
        if (isAlfrescoRef(activityRef)) {
            alfrescoDelegate.startActivity(activityRef);
        } else {
            eprocDelegate.startActivity(activityRef);
        }
    }

    @Override
    public void stopActivity(ActivityRef activityRef) {
        if (isAlfrescoRef(activityRef)) {
            alfrescoDelegate.stopActivity(activityRef);
        } else {
            eprocDelegate.stopActivity(activityRef);
        }
    }

    @Override
    public void reset(ActivityRef activityRef) {
        if (isAlfrescoRef(activityRef)) {
            alfrescoDelegate.reset(activityRef);
        } else {
            eprocDelegate.reset(activityRef);
        }
    }

    @Override
    public CaseActivity getActivity(ActivityRef activityRef) {
        if (isAlfrescoRef(activityRef)) {
            return alfrescoDelegate.getActivity(activityRef);
        } else {
            return eprocDelegate.getActivity(activityRef);
        }
    }

    @Override
    public CaseActivity getParentActivity(ActivityRef childActivityRef) {
        if (isAlfrescoRef(childActivityRef)) {
            return alfrescoDelegate.getParentActivity(childActivityRef);
        } else {
            return eprocDelegate.getParentActivity(childActivityRef);
        }
    }

    @Override
    public List<CaseActivity> getActivities(ActivityRef activityRef) {
        if (isAlfrescoRef(activityRef)) {
            return alfrescoDelegate.getActivities(activityRef);
        } else {
            return eprocDelegate.getActivities(activityRef);
        }
    }

    @Override
    public List<CaseActivity> getActivities(ActivityRef activityRef, boolean recurse) {
        if (isAlfrescoRef(activityRef)) {
            return alfrescoDelegate.getActivities(activityRef, recurse);
        } else {
            return eprocDelegate.getActivities(activityRef, recurse);
        }
    }

    @Override
    public List<CaseActivity> getStartedActivities(ActivityRef activityRef) {
        if (isAlfrescoRef(activityRef)) {
            return alfrescoDelegate.getStartedActivities(activityRef);
        } else {
            return eprocDelegate.getStartedActivities(activityRef);
        }
    }

    @Override
    public CaseActivity getActivityByName(ActivityRef activityRef, String name, boolean recurse) {
        if (isAlfrescoRef(activityRef)) {
            return alfrescoDelegate.getActivityByName(activityRef, name, recurse);
        } else {
            return eprocDelegate.getActivityByName(activityRef, name, recurse);
        }
    }

    @Override
    public CaseActivity getActivityByTitle(ActivityRef activityRef, String title, boolean recurse) {
        if (isAlfrescoRef(activityRef)) {
            return alfrescoDelegate.getActivityByTitle(activityRef, title, recurse);
        } else {
            return eprocDelegate.getActivityByTitle(activityRef, title, recurse);
        }
    }

    @Override
    public void setParent(ActivityRef activityRef, ActivityRef parentRef) {
        if (isAlfrescoRef(activityRef) && isAlfrescoRef(parentRef)) {
            alfrescoDelegate.setParent(activityRef, parentRef);
        } else {
            eprocDelegate.setParent(activityRef, parentRef);
        }
    }

    @Override
    public void setParentInIndex(ActivityRef activityRef, int newIndex) {
        if (isAlfrescoRef(activityRef)) {
            alfrescoDelegate.setParentInIndex(activityRef, newIndex);
        } else {
            eprocDelegate.setParentInIndex(activityRef, newIndex);
        }
    }

    @Override
    public boolean hasActiveChildren(ActivityRef activityRef) {
        if (isAlfrescoRef(activityRef)) {
            return alfrescoDelegate.hasActiveChildren(activityRef);
        } else {
            return eprocDelegate.hasActiveChildren(activityRef);
        }
    }

    private boolean isAlfrescoRef(ActivityRef activityRef) {
        return activityRef.getCaseServiceType() == CaseServiceType.ALFRESCO;
    }

    @Autowired
    public void setAlfrescoDelegate(AlfrescoCaseActivityDelegate alfrescoDelegate) {
        this.alfrescoDelegate = alfrescoDelegate;
    }

    @Autowired
    public void setEprocDelegate(EProcCaseActivityDelegate eprocDelegate) {
        this.eprocDelegate = eprocDelegate;
    }
}
