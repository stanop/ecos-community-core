package ru.citeck.ecos.icase.activity.service.alfresco;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.ActivityState;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.CaseActivityDelegate;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.LifeCycleModel;
import ru.citeck.ecos.utils.AlfActivityUtils;
import ru.citeck.ecos.utils.DictionaryUtils;
import ru.citeck.ecos.utils.RepoUtils;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Service
@DependsOn("idocs.dictionaryBootstrap")
public class AlfrescoCaseActivityDelegate implements CaseActivityDelegate {

    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private AlfActivityUtils alfActivityUtils;

    private ClassPolicyDelegate<CaseActivityPolicies.BeforeCaseActivityStartedPolicy> beforeStartedDelegate;
    private ClassPolicyDelegate<CaseActivityPolicies.OnCaseActivityStartedPolicy> onStartedDelegate;
    private ClassPolicyDelegate<CaseActivityPolicies.BeforeCaseActivityStoppedPolicy> beforeStoppedDelegate;
    private ClassPolicyDelegate<CaseActivityPolicies.OnCaseActivityStoppedPolicy> onStoppedDelegate;
    private ClassPolicyDelegate<CaseActivityPolicies.OnCaseActivityResetPolicy> onResetDelegate;
    private ClassPolicyDelegate<CaseActivityPolicies.OnChildrenIndexChangedPolicy> onIndexChangedDelegate;

    private Map<ActivityState, List<ActivityState>> allowedTransitions = new HashMap<>();

    @PostConstruct
    public void init() {
        beforeStartedDelegate = policyComponent.registerClassPolicy(CaseActivityPolicies.BeforeCaseActivityStartedPolicy.class);
        onStartedDelegate = policyComponent.registerClassPolicy(CaseActivityPolicies.OnCaseActivityStartedPolicy.class);
        beforeStoppedDelegate = policyComponent.registerClassPolicy(CaseActivityPolicies.BeforeCaseActivityStoppedPolicy.class);
        onStoppedDelegate = policyComponent.registerClassPolicy(CaseActivityPolicies.OnCaseActivityStoppedPolicy.class);
        onResetDelegate = policyComponent.registerClassPolicy(CaseActivityPolicies.OnCaseActivityResetPolicy.class);
        onIndexChangedDelegate = policyComponent.registerClassPolicy(CaseActivityPolicies.OnChildrenIndexChangedPolicy.class);

        allowedTransitions.put(ActivityState.NOT_STARTED, Collections.singletonList(ActivityState.STARTED));
        allowedTransitions.put(ActivityState.STARTED, Collections.singletonList(ActivityState.COMPLETED));
    }

    @Override
    public void startActivity(ActivityRef activityRef) {
        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        if (!canSetState(activityNodeRef, ActivityState.STARTED)) {
            return;
        }

        Map<QName, Serializable> props = new HashMap<>();
        props.put(LifeCycleModel.PROP_STATE, ActivityState.STARTED.getValue());
        props.put(ActivityModel.PROP_ACTUAL_START_DATE, new Date());
        nodeService.addProperties(activityNodeRef, props);

        List<QName> nodeClassNames = DictionaryUtils.getNodeClassNames(activityNodeRef, nodeService);
        Set<QName> classes = new HashSet<>(nodeClassNames);
        beforeStartedDelegate.get(classes).beforeCaseActivityStarted(activityNodeRef);
        onStartedDelegate.get(classes).onCaseActivityStarted(activityNodeRef);
    }

    @Override
    public void stopActivity(ActivityRef activityRef) {
        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        if (!canSetState(activityNodeRef, ActivityState.COMPLETED)) {
            return;
        }

        Map<QName, Serializable> props = new HashMap<>();
        props.put(LifeCycleModel.PROP_STATE, ActivityState.COMPLETED.getValue());
        props.put(ActivityModel.PROP_ACTUAL_END_DATE, new Date());
        nodeService.addProperties(activityNodeRef, props);

        List<QName> nodeClassNames = DictionaryUtils.getNodeClassNames(activityNodeRef, nodeService);
        Set<QName> classes = new HashSet<>(nodeClassNames);
        beforeStoppedDelegate.get(classes).beforeCaseActivityStopped(activityNodeRef);
        onStoppedDelegate.get(classes).onCaseActivityStopped(activityNodeRef);
    }

    @Override
    public void reset(ActivityRef activityRef) {
        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        resetImpl(activityNodeRef);
    }

    @Override
    public CaseActivity getActivity(ActivityRef activityRef) {
        if (activityRef == null) {
            return null;
        }

        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        if (!nodeService.exists(activityNodeRef)) {
            return null;
        }

        CaseActivity caseActivity = new CaseActivity();

        caseActivity.setActivityRef(activityRef);

        ActivityState activityState = getActivityState(activityNodeRef);
        caseActivity.setState(activityState);

        boolean isActive = ActivityState.STARTED == activityState;
        caseActivity.setActive(isActive);

        String title = (String) nodeService.getProperty(activityNodeRef, ContentModel.PROP_TITLE);
        caseActivity.setTitle(title);

        return caseActivity;
    }

    @Override
    public List<CaseActivity> getActivities(ActivityRef activityRef) {
        return getActivities(activityRef, false);
    }

    @Override
    public List<CaseActivity> getActivities(ActivityRef activityRef, boolean recurse) {
        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        return alfActivityUtils.getActivities(activityNodeRef, ActivityModel.ASSOC_ACTIVITIES, recurse);
    }

    @Override
    public List<CaseActivity> getStartedActivities(ActivityRef activityRef) {
        List<CaseActivity> activities = getActivities(activityRef);
        return activities.stream()
                .filter(caseActivity -> {
                    NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(caseActivity.getActivityRef());
                    String status = (String) nodeService.getProperty(activityNodeRef, LifeCycleModel.PROP_STATE);
                    return status != null && status.equals(ActivityState.STARTED.getValue());
                })
                .collect(Collectors.toList());
    }

    @Override
    public CaseActivity getActivityByName(ActivityRef activityRef, String name, boolean recurse) {
        List<CaseActivity> activities = getActivities(activityRef, recurse);
        for (CaseActivity activity : activities) {
            NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activity.getActivityRef());
            String actName = (String) nodeService.getProperty(activityNodeRef, ContentModel.PROP_NAME);
            if (actName.equals(name)) {
                return activity;
            }
        }
        return null;
    }

    @Override
    public CaseActivity getActivityByTitle(ActivityRef activityRef, String title, boolean recurse) {
        List<CaseActivity> activities = getActivities(activityRef, recurse);
        for (CaseActivity activity : activities) {
            NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activity.getActivityRef());
            String actTitle = (String) nodeService.getProperty(activityNodeRef, ContentModel.PROP_TITLE);
            if (actTitle.equals(title)) {
                return activity;
            }
        }
        return null;
    }

    @Override
    public void setParent(ActivityRef activityRef, ActivityRef newParentRef) {
        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        NodeRef newParentNodeRef = alfActivityUtils.getActivityNodeRef(newParentRef);

        NodeRef currentParentRef = nodeService.getPrimaryParent(activityNodeRef).getParentRef();

        if (!currentParentRef.equals(newParentNodeRef)) {
            if (!nodeService.hasAspect(newParentNodeRef, ActivityModel.ASPECT_HAS_ACTIVITIES)) {
                throw new IllegalArgumentException("New parent doesn't have aspect 'hasActivities'");
            }
            nodeService.moveNode(activityNodeRef, newParentNodeRef, ActivityModel.ASSOC_ACTIVITIES,
                    ActivityModel.ASSOC_ACTIVITIES);
        }
    }

    @Override
    public void setParentInIndex(ActivityRef activityRef, int newIndex) {
        ActivityRef parentRef = alfActivityUtils.getParentActivityRef(activityRef);
        List<ActivityRef> activityRefs = getActivities(parentRef).stream()
                .map(CaseActivity::getActivityRef)
                .collect(Collectors.toList());

        if (newIndex >= activityRefs.size()) {
            newIndex = activityRefs.size() - 1;
        } else if (newIndex < 0) {
            newIndex = 0;
        }

        if (newIndex != activityRefs.indexOf(activityRef)) {
            activityRefs.remove(activityRef);
            activityRefs.add(newIndex, activityRef);

            for (int index = 0; index < activityRefs.size(); index++) {
                ActivityRef selectedActivity = activityRefs.get(index);
                NodeRef selectedActivityNodeRef = alfActivityUtils.getActivityNodeRef(selectedActivity);
                nodeService.setProperty(selectedActivityNodeRef, ActivityModel.PROP_INDEX, index);
            }

            NodeRef parentNodeRef = alfActivityUtils.getActivityNodeRef(parentRef);
            HashSet<QName> classes = new HashSet<>(DictionaryUtils.getNodeClassNames(parentNodeRef, nodeService));
            onIndexChangedDelegate.get(classes).onChildrenIndexChanged(parentNodeRef);
        }
    }

    @Override
    public boolean hasActiveChildren(ActivityRef activityRef) {
        return getActivities(activityRef).stream()
                .anyMatch(activity -> activity.getState() == ActivityState.STARTED);
    }

    private ActivityState getActivityState(NodeRef activityNodeRef) {
        String rawState = (String) nodeService.getProperty(activityNodeRef, LifeCycleModel.PROP_STATE);
        if (StringUtils.isNotBlank(rawState)) {
            return ActivityState.getByValue(rawState);
        }
        return ActivityState.NOT_STARTED;
    }

    private boolean canSetState(NodeRef activityNodeRef, ActivityState state) {
        if (!nodeService.exists(activityNodeRef)) {
            return false;
        }

        ActivityState currentState = getActivityState(activityNodeRef);

        if (isRequiredReset(activityNodeRef, currentState, state)) {
            resetImpl(activityNodeRef);
            currentState = getActivityState(activityNodeRef);
        }

        if (!currentState.equals(state)) {
            List<ActivityState> transitions = allowedTransitions.get(currentState);
            return transitions != null && transitions.contains(state);
        }

        return false;
    }

    private boolean isRequiredReset(NodeRef activityNodeRef, ActivityState fromState, ActivityState toState) {
        if (fromState != ActivityState.NOT_STARTED && toState == ActivityState.STARTED) {
            Boolean repeatable = (Boolean) nodeService.getProperty(activityNodeRef, ActivityModel.PROP_REPEATABLE);
            return Boolean.TRUE.equals(repeatable);
        }
        return false;
    }

    private void resetImpl(NodeRef activityNodeRef) {
        QName nodeType = nodeService.getType(activityNodeRef);
        if (dictionaryService.isSubClass(nodeType, ActivityModel.TYPE_ACTIVITY)) {
            resetActivity(activityNodeRef);
        } else {
            resetChildrenActivities(activityNodeRef);
        }
    }

    private void resetActivity(NodeRef activityNodeRef) {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(ActivityModel.PROP_ACTUAL_START_DATE, null);
        props.put(ActivityModel.PROP_ACTUAL_END_DATE, null);
        props.put(LifeCycleModel.PROP_STATE, ActivityState.NOT_STARTED.getValue());
        nodeService.addProperties(activityNodeRef, props);

        HashSet<QName> classes = new HashSet<>(DictionaryUtils.getNodeClassNames(activityNodeRef, nodeService));
        onResetDelegate.get(classes).onCaseActivityReset(activityNodeRef);

        resetChildrenActivities(activityNodeRef);
    }

    private void resetChildrenActivities(NodeRef activityNodeRef) {
        List<NodeRef> childrenNodeRefs = RepoUtils.getChildrenByAssoc(
                activityNodeRef, ActivityModel.ASSOC_ACTIVITIES, nodeService);
        for (NodeRef nodeRef : childrenNodeRefs) {
            resetActivity(nodeRef);
        }
    }

    @Autowired
    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Autowired
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Autowired
    public void setAlfActivityUtils(AlfActivityUtils alfActivityUtils) {
        this.alfActivityUtils = alfActivityUtils;
    }
}
