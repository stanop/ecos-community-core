package ru.citeck.ecos.icase.activity;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.cases.RemoteRestoreCaseModelService;
import ru.citeck.ecos.icase.activity.create.dto.ActivityCreateVariant;
import ru.citeck.ecos.icase.activity.create.provider.CreateVariantsProvider;
import ru.citeck.ecos.icase.activity.create.provider.impl.MenuCreateVariantsProvider;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.utils.ActivityUtilsJS;
import ru.citeck.ecos.utils.AlfActivityUtils;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CaseActivityServiceJS extends AlfrescoScopableProcessorExtension {

    private AlfActivityUtils alfActivityUtils;
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
        ActivityRef activityRef = ActivityUtilsJS.getActivityRef(ref, alfActivityUtils);
        caseActivityService.startActivity(activityRef);
    }

    public void stopActivity(Object ref) {
        ActivityRef activityRef = ActivityUtilsJS.getActivityRef(ref, alfActivityUtils);
        caseActivityService.stopActivity(activityRef);
    }

    public ScriptNode[] getStartedActivities(Object ref) {
        ActivityRef activityRef = ActivityUtilsJS.getActivityRef(ref, alfActivityUtils);
        List<CaseActivity> activities = caseActivityService.getStartedActivities(activityRef);
        List<NodeRef> nodeRefs = activities.stream()
            .map(activity -> alfActivityUtils.getActivityNodeRef(activity.getActivityRef()))
            .collect(Collectors.toList());
        return JavaScriptImplUtils.wrapNodes(nodeRefs, this);
    }

    public ScriptNode[] getActivities(Object ref) {
        ActivityRef activityRef = ActivityUtilsJS.getActivityRef(ref, alfActivityUtils);
        List<CaseActivity> activities = caseActivityService.getActivities(activityRef);
        List<NodeRef> activityNodeRefs = activities.stream()
            .map(activity -> alfActivityUtils.getActivityNodeRef(activity.getActivityRef()))
            .collect(Collectors.toList());
        return JavaScriptImplUtils.wrapNodes(activityNodeRefs, this);
    }

    public ScriptNode getActivityByTitle(Object ref, String title) {
        return getActivityByTitle(ref, title, false);
    }

    public ScriptNode getActivityByTitle(Object ref, String title, boolean recurse) {
        ActivityRef activityRef = ActivityUtilsJS.getActivityRef(ref, alfActivityUtils);
        CaseActivity caseActivity = caseActivityService.getActivityByTitle(activityRef, title, recurse);
        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(caseActivity.getActivityRef());
        return JavaScriptImplUtils.wrapNode(activityNodeRef, this);
    }

    public void reset(Object ref) {
        ActivityRef activityRef = ActivityUtilsJS.getActivityRef(ref, alfActivityUtils);
        caseActivityService.reset(activityRef);
    }

    public void setParent(Object childRef, Object parentRef) {
        ActivityRef activityRef = ActivityUtilsJS.getActivityRef(childRef, alfActivityUtils);
        ActivityRef parentActivityRef = ActivityUtilsJS.getActivityRef(parentRef, alfActivityUtils);
        caseActivityService.setParent(activityRef, parentActivityRef);
    }

    public void setIndex(Object ref, Object newIndexRaw) {
        ActivityRef activityRef = ActivityUtilsJS.getActivityRef(ref, alfActivityUtils);
        int newIndex = getInt(newIndexRaw);
        caseActivityService.setParentInIndex(activityRef, newIndex);
    }

    public boolean hasActiveChildren(Object ref) {
        ActivityRef activityRef = ActivityUtilsJS.getActivityRef(ref, alfActivityUtils);
        return caseActivityService.hasActiveChildren(activityRef);
    }

    public boolean isActive(Object ref) {
        ActivityRef activityRef = ActivityUtilsJS.getActivityRef(ref, alfActivityUtils);
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

    public void setAlfActivityUtils(AlfActivityUtils alfActivityUtils) {
        this.alfActivityUtils = alfActivityUtils;
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
