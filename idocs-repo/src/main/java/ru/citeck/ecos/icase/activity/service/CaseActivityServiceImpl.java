package ru.citeck.ecos.icase.activity.service;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.alfresco.AlfrescoCaseActivityDelegate;
import ru.citeck.ecos.icase.activity.service.eproc.EProcCaseActivityDelegate;

import java.util.List;

@Slf4j
public class CaseActivityServiceImpl implements CaseActivityService {

    private AlfrescoCaseActivityDelegate alfrescoDelegate;
    private EProcCaseActivityDelegate eprocDelegate;

    @Override
    public void startActivity(CaseActivity activity) {
        if (isAlfrescoCase(activity)) {
            alfrescoDelegate.startActivity(activity);
        } else {
            eprocDelegate.startActivity(activity);
        }
    }

    @Override
    public void stopActivity(CaseActivity activity) {
        if (isAlfrescoCase(activity)) {
            alfrescoDelegate.stopActivity(activity);
        } else {
            eprocDelegate.stopActivity(activity);
        }
    }

    @Override
    public void restartChildActivity(CaseActivity parent, CaseActivity child) {
        if (isAlfrescoCase(parent, child)) {
            alfrescoDelegate.restartChildActivity(parent, child);
        } else {
            eprocDelegate.restartChildActivity(parent, child);
        }
    }

    @Override
    public String getDocumentId(String activityId) {
        if (isAlfrescoCase(activityId)) {
            return alfrescoDelegate.getDocumentId(activityId);
        } else {
            return eprocDelegate.getDocumentId(activityId);
        }
    }

    @Override
    public void setParent(String activityId, String receivedParentId) {
        if (isAlfrescoCase(activityId, receivedParentId)) {
            alfrescoDelegate.setParent(activityId, receivedParentId);
        } else {
            eprocDelegate.setParent(activityId, receivedParentId);
        }
    }

    @Override
    public CaseActivity getActivity(String activityId) {
        if (isAlfrescoCase(activityId)) {
            return alfrescoDelegate.getActivity(activityId);
        } else {
            return eprocDelegate.getActivity(activityId);
        }
    }

    @Override
    public List<CaseActivity> getActivities(String documentId) {
        if (isAlfrescoCase(documentId)) {
            return alfrescoDelegate.getActivities(documentId);
        } else {
            return eprocDelegate.getActivities(documentId);
        }
    }

    @Override
    public List<CaseActivity> getActivities(String documentId, boolean recurse) {
        if (isAlfrescoCase(documentId)) {
            return alfrescoDelegate.getActivities(documentId, recurse);
        } else {
            return eprocDelegate.getActivities(documentId, recurse);
        }
    }

    @Override
    public List<CaseActivity> getStartedActivities(String documentId) {
        if (isAlfrescoCase(documentId)) {
            return alfrescoDelegate.getStartedActivities(documentId);
        } else {
            return eprocDelegate.getStartedActivities(documentId);
        }
    }

    @Override
    public CaseActivity getActivityByTitle(String documentId, String title, boolean recurse) {
        if (isAlfrescoCase(documentId)) {
            return alfrescoDelegate.getActivityByTitle(documentId, title, recurse);
        } else {
            return eprocDelegate.getActivityByTitle(documentId, title, recurse);
        }
    }

    @Override
    public void reset(String id) {
        if (isAlfrescoCase(id)) {
            alfrescoDelegate.reset(id);
        } else {
            eprocDelegate.reset(id);
        }
    }

    @Override
    public void setParentInIndex(CaseActivity activity, int newIndex) {
        if (isAlfrescoCase(activity)) {
            alfrescoDelegate.setParentInIndex(activity, newIndex);
        } else {
            eprocDelegate.setParentInIndex(activity, newIndex);
        }
    }

    @Override
    public boolean hasActiveChildren(CaseActivity activity) {
        if (isAlfrescoCase(activity)) {
            return alfrescoDelegate.hasActiveChildren(activity);
        } else {
            return eprocDelegate.hasActiveChildren(activity);
        }
    }

    private boolean isAlfrescoCase(CaseActivity... activities) {
        for (CaseActivity activity : activities) {
            String id = activity.getId();
            if (!isAlfrescoCase(id)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAlfrescoCase(String... activityIds) {
        for (String activityId : activityIds) {
            if (!NodeRef.isNodeRef(activityId)) {
                //TODO: Provide check of situation where NodeRef is document (it can be EProc and Alfresco case)
                return false;
            }
        }
        return true;
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
