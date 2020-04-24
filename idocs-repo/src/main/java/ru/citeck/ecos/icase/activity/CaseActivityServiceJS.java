package ru.citeck.ecos.icase.activity;

import com.google.common.collect.Iterables;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.cases.RemoteRestoreCaseModelService;
import ru.citeck.ecos.icase.activity.create.dto.ActivityCreateVariant;
import ru.citeck.ecos.icase.activity.create.provider.CreateVariantsProvider;
import ru.citeck.ecos.icase.activity.create.provider.impl.MenuCreateVariantsProvider;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.utils.ActivityUtilsJS;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

import java.util.List;

public class CaseActivityServiceJS extends AlfrescoScopableProcessorExtension {

    private ActivityUtilsJS activityUtilsJS;
    private CaseActivityService caseActivityService;
    private CreateVariantsProvider createVariantsProvider;
    private RemoteRestoreCaseModelService remoteRestoreCaseModelService;

    public void startActivity(Object ref) {
        /* Call restore activity */
        if (ref instanceof String) {
            String stageRefUUID = (String) ref;
            if (((String) ref).startsWith(RemoteRestoreCaseModelService.RESTORE_CASE_MODEL_UUID)) {
                NodeRef documentRef = new NodeRef(stageRefUUID.substring(RemoteRestoreCaseModelService.RESTORE_CASE_MODEL_UUID.length()));
                remoteRestoreCaseModelService.restoreCaseModels(documentRef);
                return;
            }
        }
        /* Call common activity */
        ActivityRef activityRef = activityUtilsJS.getActivityRef(ref);
        caseActivityService.startActivity(activityRef);
    }

    public void stopActivity(Object ref) {
        ActivityRef activityRef = activityUtilsJS.getActivityRef(ref);
        caseActivityService.stopActivity(activityRef);
    }

    public CaseActivity[] getStartedActivities(Object ref) {
        ActivityRef activityRef = activityUtilsJS.getActivityRef(ref);
        List<CaseActivity> activities = caseActivityService.getStartedActivities(activityRef);
        return Iterables.toArray(activities, CaseActivity.class);
    }

    public CaseActivity[] getActivities(Object ref) {
        ActivityRef activityRef = activityUtilsJS.getActivityRef(ref);
        List<CaseActivity> activities = caseActivityService.getActivities(activityRef);
        return Iterables.toArray(activities, CaseActivity.class);
    }

    public CaseActivity getActivityByName(Object ref, String title) {
        return getActivityByName(ref, title, false);
    }

    public CaseActivity getActivityByName(Object ref, String name, boolean recurse) {
        ActivityRef activityRef = activityUtilsJS.getActivityRef(ref);
        return caseActivityService.getActivityByName(activityRef, name, recurse);
    }

    public CaseActivity getActivityByTitle(Object ref, String title) {
        return getActivityByTitle(ref, title, false);
    }

    public CaseActivity getActivityByTitle(Object ref, String title, boolean recurse) {
        ActivityRef activityRef = activityUtilsJS.getActivityRef(ref);
        return caseActivityService.getActivityByTitle(activityRef, title, recurse);
    }

    public void reset(Object ref) {
        ActivityRef activityRef = activityUtilsJS.getActivityRef(ref);
        caseActivityService.reset(activityRef);
    }

    public void setParent(Object childRef, Object parentRef) {
        ActivityRef activityRef = activityUtilsJS.getActivityRef(childRef);
        ActivityRef parentActivityRef = activityUtilsJS.getActivityRef(parentRef);
        caseActivityService.setParent(activityRef, parentActivityRef);
    }

    public void setIndex(Object ref, Object newIndexRaw) {
        ActivityRef activityRef = activityUtilsJS.getActivityRef(ref);
        int newIndex = getInt(newIndexRaw);
        caseActivityService.setParentInIndex(activityRef, newIndex);
    }

    public boolean hasActiveChildren(Object ref) {
        ActivityRef activityRef = activityUtilsJS.getActivityRef(ref);
        return caseActivityService.hasActiveChildren(activityRef);
    }

    public boolean isActive(Object ref) {
        ActivityRef activityRef = activityUtilsJS.getActivityRef(ref);
        CaseActivity activity = caseActivityService.getActivity(activityRef);
        return activity.isActive();
    }

    public List<ActivityCreateVariant> getCreateVariants() {
        return createVariantsProvider.getCreateVariants();
    }

    private int getInt(Object newIndex) {
        int index;
        if (newIndex instanceof Integer) {
            index = (Integer) newIndex;
        } else if (newIndex instanceof String) {
            index = Integer.parseInt((String) newIndex);
        } else if (newIndex instanceof Double) {
            index = ((Double) newIndex).intValue();
        } else {
            throw new IllegalArgumentException("Can not convert from " + newIndex.getClass() + " to Integer");
        }
        return index;
    }

    @Autowired
    public void setActivityUtilsJS(ActivityUtilsJS activityUtilsJS) {
        this.activityUtilsJS = activityUtilsJS;
    }

    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }

    public void setRemoteRestoreCaseModelService(RemoteRestoreCaseModelService remoteRestoreCaseModelService) {
        this.remoteRestoreCaseModelService = remoteRestoreCaseModelService;
    }

    public void setCreateVariantsProvider(MenuCreateVariantsProvider createVariantsProvider) {
        this.createVariantsProvider = createVariantsProvider;
    }


}
