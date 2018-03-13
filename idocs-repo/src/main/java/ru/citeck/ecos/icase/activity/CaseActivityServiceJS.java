package ru.citeck.ecos.icase.activity;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import ru.citeck.ecos.cases.RemoteRestoreCaseModelService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.icase.activity.create.ActivityCreateVariant;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.List;

/**
 * @author Pavel Simonov
 */
public class CaseActivityServiceJS extends AlfrescoScopableProcessorExtension {
    private static final Log log = LogFactory.getLog(CaseActivityServiceJS.class);

    private CaseActivityService caseActivityService;
    private NamespaceService namespaceService;
    private RemoteRestoreCaseModelService remoteRestoreCaseModelService;

    public void startActivity(Object stageRef) {
        /** Call restore activity */
        if (stageRef instanceof String) {
            String stageRefUUID = (String) stageRef;
            if (((String) stageRef).startsWith(RemoteRestoreCaseModelService.RESTORE_CASE_MODEL_UUID)) {
                NodeRef documentRef = new NodeRef(stageRefUUID.substring(RemoteRestoreCaseModelService.RESTORE_CASE_MODEL_UUID.length()));
                remoteRestoreCaseModelService.restoreCaseModels(documentRef);
                return;
            }
        }
        /** Call common activity */
        NodeRef ref = JavaScriptImplUtils.getNodeRef(stageRef);
        caseActivityService.startActivity(ref);
    }

    public void stopActivity(Object stageRef) {
        NodeRef ref = JavaScriptImplUtils.getNodeRef(stageRef);
        caseActivityService.stopActivity(ref);
    }

    public ScriptNode[] getStartedActivities(Object nodeRef) {
        NodeRef nRef = JavaScriptImplUtils.getNodeRef(nodeRef);
        List<NodeRef> activities = caseActivityService.getStartedActivities(nRef);
        return JavaScriptImplUtils.wrapNodes(activities, this);
    }

    public ScriptNode[] getActivities(Object nodeRef) {
        NodeRef nRef = JavaScriptImplUtils.getNodeRef(nodeRef);
        List<NodeRef> activities = caseActivityService.getActivities(nRef);
        return JavaScriptImplUtils.wrapNodes(activities, this);
    }

    public ScriptNode[] getActivities(Object nodeRef, String type) {
        NodeRef nRef = JavaScriptImplUtils.getNodeRef(nodeRef);
        QName typeQName = QName.createQName(type, namespaceService);
        List<NodeRef> activities = caseActivityService.getActivities(nRef, typeQName);
        return JavaScriptImplUtils.wrapNodes(activities, this);
    }

    public ScriptNode[] getActivitiesByAssoc(Object nodeRef, String assocType) {
        NodeRef nRef = JavaScriptImplUtils.getNodeRef(nodeRef);
        QName assocTypeQName = QName.createQName(assocType, namespaceService);
        List<NodeRef> activities = caseActivityService.getActivities(nRef, assocTypeQName, RegexQNamePattern.MATCH_ALL);
        return JavaScriptImplUtils.wrapNodes(activities, this);
    }

    public ScriptNode getActivityByTitle(Object nodeRef, String title) {
        NodeRef nRef = JavaScriptImplUtils.getNodeRef(nodeRef);
        NodeRef activity = caseActivityService.getActivityByTitle(nRef, title);
        return JavaScriptImplUtils.wrapNode(activity, this);
    }

    public ScriptNode getDocument(Object nodeRef) {
        NodeRef ref = JavaScriptImplUtils.getNodeRef(nodeRef);
        NodeRef parent = caseActivityService.getDocument(ref);
        return JavaScriptImplUtils.wrapNode(parent, this);
    }

    public void reset(Object nodeRef) {
        NodeRef ref = JavaScriptImplUtils.getNodeRef(nodeRef);
        caseActivityService.reset(ref);
    }

    public void setParent(Object activityRef, Object newParent) {
        caseActivityService.setParent(JavaScriptImplUtils.getNodeRef(activityRef), JavaScriptImplUtils.getNodeRef(newParent));
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
        caseActivityService.setIndex(JavaScriptImplUtils.getNodeRef(activityRef), index);
    }

    public boolean hasActiveChildren(Object activity) {
        return caseActivityService.hasActiveChildren(JavaScriptImplUtils.getNodeRef(activity));
    }

    public boolean isActive(Object activity) {
        return caseActivityService.isActive(JavaScriptImplUtils.getNodeRef(activity));
    }

    public void restartChildrenActivity(Object parentActivityRef, Object childActivityRef) {
        NodeRef parentRef = JavaScriptImplUtils.getNodeRef(parentActivityRef);
        NodeRef childRef = JavaScriptImplUtils.getNodeRef(childActivityRef);
        caseActivityService.restartChildrenActivity(parentRef, childRef);
    }

    public List<ActivityCreateVariant> getCreateVariants() {
        return caseActivityService.getCreateVariants();
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
}
