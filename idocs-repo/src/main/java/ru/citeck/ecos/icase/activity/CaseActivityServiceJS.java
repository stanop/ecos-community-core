package ru.citeck.ecos.icase.activity;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import ru.citeck.ecos.cases.RemoteRestoreCaseModelService;
import ru.citeck.ecos.icase.activity.create.dto.ActivityCreateVariant;
import ru.citeck.ecos.icase.activity.create.provider.CreateVariantsProvider;
import ru.citeck.ecos.icase.activity.create.provider.impl.MenuCreateVariantsProvider;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CaseActivityServiceJS extends AlfrescoScopableProcessorExtension {

    private CaseActivityService caseActivityService;
    private CreateVariantsProvider createVariantsProvider;
    private NamespaceService namespaceService;
    private RemoteRestoreCaseModelService remoteRestoreCaseModelService;

    public void startActivity(Object stageRef) {
        /* Call restore activity */
        if (stageRef instanceof String) {
            String stageRefUUID = (String) stageRef;
            if (((String) stageRef).startsWith(RemoteRestoreCaseModelService.RESTORE_CASE_MODEL_UUID)) {
                NodeRef documentRef = new NodeRef(stageRefUUID.substring(RemoteRestoreCaseModelService.RESTORE_CASE_MODEL_UUID.length()));
                remoteRestoreCaseModelService.restoreCaseModels(documentRef);
                return;
            }
        }
        /* Call common activity */
        NodeRef ref = JavaScriptImplUtils.getNodeRef(stageRef);
        if (ref != null) {
            CaseActivity activity = caseActivityService.getActivity(ref.toString());
            if (activity != null) {
                caseActivityService.startActivity(activity);
            }
        }
    }

    public void stopActivity(Object stageRef) {
        NodeRef ref = JavaScriptImplUtils.getNodeRef(stageRef);
        if (ref != null) {
            CaseActivity activity = caseActivityService.getActivity(ref.toString());
            if (activity != null) {
                caseActivityService.stopActivity(activity);
            }
        }
    }

    public ScriptNode[] getStartedActivities(Object nodeRef) {
        NodeRef nRef = JavaScriptImplUtils.getNodeRef(nodeRef);
        List<CaseActivity> activities = caseActivityService.getStartedActivities(nRef.toString());
        Set<NodeRef> startedActivitiesNodeRefs = activities.stream()
            .map(a -> new NodeRef(a.getId()))
            .collect(Collectors.toSet());
        return JavaScriptImplUtils.wrapNodes(startedActivitiesNodeRefs, this);
    }

    public ScriptNode[] getActivities(Object nodeRef) {
        NodeRef nRef = JavaScriptImplUtils.getNodeRef(nodeRef);
        List<CaseActivity> activities = caseActivityService.getActivities(nRef.toString());
        Set<NodeRef> activitiesNodeRefs = activities.stream()
            .map(a -> new NodeRef(a.getId()))
            .collect(Collectors.toSet());
        return JavaScriptImplUtils.wrapNodes(activitiesNodeRefs, this);
    }

    public ScriptNode[] getActivities(Object nodeRef, String type) {
        NodeRef nRef = JavaScriptImplUtils.getNodeRef(nodeRef);
        QName typeQName = QName.createQName(type, namespaceService);
        List<CaseActivity> activities = caseActivityService.getActivities(nRef.toString(), typeQName);
        Set<NodeRef> activitiesNodeRefs = activities.stream()
            .map(a -> new NodeRef(a.getId()))
            .collect(Collectors.toSet());
        return JavaScriptImplUtils.wrapNodes(activitiesNodeRefs, this);
    }

    public ScriptNode[] getActivitiesByAssoc(Object nodeRef, String assocType) {
        NodeRef nRef = JavaScriptImplUtils.getNodeRef(nodeRef);
        QName assocTypeQName = QName.createQName(assocType, namespaceService);
        List<CaseActivity> activities = caseActivityService.getActivities(nRef.toString(), assocTypeQName,
            RegexQNamePattern.MATCH_ALL);
        Set<NodeRef> activitiesNodeRefs = activities.stream()
            .map(a -> new NodeRef(a.getId()))
            .collect(Collectors.toSet());
        return JavaScriptImplUtils.wrapNodes(activitiesNodeRefs, this);
    }

    public ScriptNode getActivityByTitle(Object nodeRef, String title) {
        return getActivityByTitle(nodeRef, title, false);
    }

    public ScriptNode getActivityByTitle(Object nodeRef, String title, boolean recurse) {
        NodeRef nRef = JavaScriptImplUtils.getNodeRef(nodeRef);
        CaseActivity activity = caseActivityService.getActivityByTitle(nRef.toString(), title, recurse);
        NodeRef activityNodeRef = new NodeRef(activity.getId());
        return JavaScriptImplUtils.wrapNode(activityNodeRef, this);
    }

    public ScriptNode getDocument(Object nodeRef) {
        NodeRef ref = JavaScriptImplUtils.getNodeRef(nodeRef);
        String parentId = caseActivityService.getDocumentId(ref.toString());
        NodeRef parentNodeRef = new NodeRef(parentId);
        return JavaScriptImplUtils.wrapNode(parentNodeRef, this);
    }

    public void reset(Object nodeRef) {
        NodeRef ref = JavaScriptImplUtils.getNodeRef(nodeRef);
        caseActivityService.reset(ref.toString());
    }

    public void setParent(Object activityRef, Object newParent) {
        caseActivityService.setParent(JavaScriptImplUtils.getNodeRef(activityRef).toString(),
            JavaScriptImplUtils.getNodeRef(newParent).toString());
    }

    public void setIndex(Object activityRef, Object newIndex) {
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

        String activityId = JavaScriptImplUtils.getNodeRef(activityRef).toString();
        CaseActivity activity = caseActivityService.getActivity(activityId);

        caseActivityService.setParentInIndex(activity, index);
    }

    public boolean hasActiveChildren(Object activityObj) {
        String activityId = JavaScriptImplUtils.getNodeRef(activityObj).toString();
        CaseActivity activity = caseActivityService.getActivity(activityId);
        return caseActivityService.hasActiveChildren(activity);
    }

    public boolean isActive(Object activityObj) {
        String activityId = JavaScriptImplUtils.getNodeRef(activityObj).toString();
        CaseActivity activity = caseActivityService.getActivity(activityId);
        return activity.isActive();
    }

    public void restartChildrenActivity(Object parentActivityRef, Object childActivityRef) {

        NodeRef parentRef = JavaScriptImplUtils.getNodeRef(parentActivityRef);
        CaseActivity parentActivity = caseActivityService.getActivity(parentRef.toString());

        NodeRef childRef = JavaScriptImplUtils.getNodeRef(childActivityRef);
        CaseActivity childActivity = caseActivityService.getActivity(childRef.toString());

        caseActivityService.restartChildActivity(parentActivity, childActivity);
    }

    public List<ActivityCreateVariant> getCreateVariants() {
        return createVariantsProvider.getCreateVariants();
    }

    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setRemoteRestoreCaseModelService(RemoteRestoreCaseModelService remoteRestoreCaseModelService) {
        this.remoteRestoreCaseModelService = remoteRestoreCaseModelService;
    }

    public void setCreateVariantsProvider(MenuCreateVariantsProvider createVariantsProvider) {
        this.createVariantsProvider = createVariantsProvider;
    }
}
