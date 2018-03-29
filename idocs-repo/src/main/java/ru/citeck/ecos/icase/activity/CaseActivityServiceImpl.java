package ru.citeck.ecos.icase.activity;

import com.google.common.collect.Lists;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.icase.activity.create.ActivityCreateVariant;
import ru.citeck.ecos.icase.activity.create.CreateVariantsProvider;
import ru.citeck.ecos.model.*;
import ru.citeck.ecos.utils.DictionaryUtils;
import ru.citeck.ecos.utils.RepoUtils;
import ru.citeck.ecos.icase.activity.CaseActivityPolicies.*;

import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CaseActivityServiceImpl implements CaseActivityService {

    public static final String STATE_NOT_STARTED = "Not started";
    public static final String STATE_STARTED = "Started";
    public static final String STATE_COMPLETED = "Completed";

    private static final Log log = LogFactory.getLog(CaseActivityService.class);

    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private MessageService messageService;
    private NamespaceService namespaceService;

    private ClassPolicyDelegate<BeforeCaseActivityStartedPolicy> beforeStartedDelegate;
    private ClassPolicyDelegate<OnCaseActivityStartedPolicy> onStartedDelegate;
    private ClassPolicyDelegate<BeforeCaseActivityStoppedPolicy> beforeStoppedDelegate;
    private ClassPolicyDelegate<OnCaseActivityStoppedPolicy> onStoppedDelegate;
    private ClassPolicyDelegate<OnCaseActivityResetPolicy> onResetDelegate;
    private ClassPolicyDelegate<OnChildrenIndexChangedPolicy> onIndexChangedDelegate;

    private Map<String, List<String>> allowedTransitions = new HashMap<>();

    private List<CreateVariantsProvider> createVariantsProviders = new ArrayList<>();
    private List<QName> createMenuTypes = Collections.emptyList();

    public void init() {

        beforeStartedDelegate = policyComponent.registerClassPolicy(BeforeCaseActivityStartedPolicy.class);
        onStartedDelegate = policyComponent.registerClassPolicy(OnCaseActivityStartedPolicy.class);
        beforeStoppedDelegate = policyComponent.registerClassPolicy(BeforeCaseActivityStoppedPolicy.class);
        onStoppedDelegate = policyComponent.registerClassPolicy(OnCaseActivityStoppedPolicy.class);
        onResetDelegate = policyComponent.registerClassPolicy(OnCaseActivityResetPolicy.class);
        onIndexChangedDelegate = policyComponent.registerClassPolicy(OnChildrenIndexChangedPolicy.class);

        allowedTransitions.put(STATE_NOT_STARTED, Lists.newArrayList(STATE_STARTED));
        allowedTransitions.put(STATE_STARTED, Lists.newArrayList(STATE_COMPLETED));
    }

    @Override
    public void startActivity(NodeRef activityRef) {

        if (!canSetState(activityRef, STATE_STARTED)) {
            return;
        }

        Map<QName, Serializable> props = new HashMap<>();
        props.put(LifeCycleModel.PROP_STATE, STATE_STARTED);
        props.put(ActivityModel.PROP_ACTUAL_START_DATE, new Date());
        nodeService.addProperties(activityRef, props);

        HashSet<QName> classes = new HashSet<>(DictionaryUtils.getNodeClassNames(activityRef, nodeService));

        beforeStartedDelegate.get(classes).beforeCaseActivityStarted(activityRef);
        onStartedDelegate.get(classes).onCaseActivityStarted(activityRef);
    }

    @Override
    public void stopActivity(NodeRef activityRef) {

        if (!canSetState(activityRef, STATE_COMPLETED)) {
            return;
        }

        Map<QName, Serializable> props = new HashMap<>();
        props.put(LifeCycleModel.PROP_STATE, STATE_COMPLETED);
        props.put(ActivityModel.PROP_ACTUAL_END_DATE, new Date());
        nodeService.addProperties(activityRef, props);

        HashSet<QName> classes = new HashSet<>(DictionaryUtils.getNodeClassNames(activityRef, nodeService));

        beforeStoppedDelegate.get(classes).beforeCaseActivityStopped(activityRef);
        onStoppedDelegate.get(classes).onCaseActivityStopped(activityRef);
    }

    @Override
    public void restartChildrenActivity(NodeRef parentActivityRef, NodeRef childActivityRef) {

        if (nodeService.exists(parentActivityRef) && nodeService.exists(childActivityRef)) {

            if (!isActive(parentActivityRef)) {

                Map<QName, Serializable> props = new HashMap<>();
                props.put(ActivityModel.PROP_ACTUAL_END_DATE, null);
                props.put(LifeCycleModel.PROP_STATE, STATE_STARTED);
                nodeService.addProperties(parentActivityRef, props);

                resetActivitiesInChildren(childActivityRef);
                startActivity(childActivityRef);
            }
        }
    }

    @Override
    public NodeRef getDocument(NodeRef activityRef) {
        ChildAssociationRef parent = nodeService.getPrimaryParent(activityRef);
        while (parent.getParentRef() != null
                && RepoUtils.isSubType(parent.getParentRef(), ActivityModel.TYPE_ACTIVITY, nodeService, dictionaryService)) {
            parent = nodeService.getPrimaryParent(parent.getParentRef());
        }
        return parent.getParentRef();
    }

    @Override
    public List<NodeRef> getActivities(NodeRef nodeRef) {
        return getActivities(nodeRef, RegexQNamePattern.MATCH_ALL);
    }

    @Override
    public List<NodeRef> getActivities(NodeRef nodeRef, boolean recurse) {
        return getActivities(nodeRef, RegexQNamePattern.MATCH_ALL, recurse);
    }

    @Override
    public List<NodeRef> getActivities(NodeRef nodeRef, QNamePattern type) {
        return getActivities(nodeRef, ActivityModel.ASSOC_ACTIVITIES, type);
    }

    @Override
    public List<NodeRef> getActivities(NodeRef nodeRef, QNamePattern type, boolean recurse) {
        return getActivities(nodeRef, ActivityModel.ASSOC_ACTIVITIES, type, recurse);
    }

    @Override
    public List<NodeRef> getActivities(NodeRef nodeRef, QName assocType, QNamePattern type) {
        return getActivities(nodeRef, assocType, type, false);
    }

    @Override
    public List<NodeRef> getActivities(NodeRef nodeRef, QName assocType, QNamePattern type, boolean recurse) {

        List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef, assocType, RegexQNamePattern.MATCH_ALL);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }

        List<Pair<NodeRef, Integer>> indexedChildren = new ArrayList<>(children.size());
        for (ChildAssociationRef child : children) {
            NodeRef childRef = child.getChildRef();
            if (type.isMatch(nodeService.getType(childRef))) {
                Integer index = (Integer) nodeService.getProperty(childRef, ActivityModel.PROP_INDEX);
                indexedChildren.add(new Pair<>(childRef, index != null ? index : 0));
            }
        }

        indexedChildren.sort(Comparator.comparingInt(Pair::getSecond));

        List<NodeRef> result = new ArrayList<>(indexedChildren.size());
        for (Pair<NodeRef, Integer> child : indexedChildren) {
            result.add(child.getFirst());
        }

        if (recurse) {
            for (ChildAssociationRef child : children) {
                result.addAll(getActivities(child.getChildRef(), assocType, type, true));
            }
        }

        return result;
    }

    public List<NodeRef> getStartedActivities(NodeRef nodeRef) {
        List<NodeRef> startedActivities = new ArrayList<>();
        for (NodeRef activiti : getActivities(nodeRef)) {
            String status = (String) nodeService.getProperty(activiti, LifeCycleModel.PROP_STATE);
            if (status != null && status.equals(STATE_STARTED)) {
                startedActivities.add(activiti);
            }
        }
        return startedActivities;
    }

    public NodeRef getActivityByTitle(NodeRef nodeRef, String title) {
        for (NodeRef activiti : getActivities(nodeRef)) {
            String actTitle = (String) nodeService.getProperty(activiti, ContentModel.PROP_TITLE);
            if (actTitle.equals(title)) {
                return activiti;
            }
        }
        return null;
    }

    @Override
    public void reset(NodeRef nodeRef) {
        QName nodeType = nodeService.getType(nodeRef);
        if (dictionaryService.isSubClass(nodeType, ActivityModel.TYPE_ACTIVITY)) {
            resetActivity(nodeRef);
        } else {
            resetActivitiesInChildren(nodeRef);
        }
    }

    @Override
    public void setParent(NodeRef activityRef, NodeRef newParent) {

        mandatoryActivity("activityRef", activityRef);
        mandatoryNodeRef("newParent", newParent);

        ChildAssociationRef assocRef = nodeService.getPrimaryParent(activityRef);
        NodeRef parent = assocRef.getParentRef();

        if (!parent.equals(newParent)) {
            if (!nodeService.hasAspect(newParent, ActivityModel.ASPECT_HAS_ACTIVITIES)) {
                throw new IllegalArgumentException("New parent doesn't have aspect 'hasActivities'");
            }
            nodeService.moveNode(activityRef, newParent, ActivityModel.ASSOC_ACTIVITIES,
                                                         ActivityModel.ASSOC_ACTIVITIES);
        }
    }

    @Override
    public void setIndex(NodeRef activityRef, int newIndex) {

        mandatoryActivity("activityRef", activityRef);

        ChildAssociationRef assocRef = nodeService.getPrimaryParent(activityRef);
        NodeRef parentRef = assocRef.getParentRef();

        List<NodeRef> activities = getActivities(parentRef);

        if (newIndex >= activities.size()) {
            newIndex = activities.size() - 1;
        } else if (newIndex < 0) {
            newIndex = 0;
        }

        if (newIndex != activities.indexOf(activityRef)) {

            activities.remove(activityRef);
            activities.add(newIndex, activityRef);

            for (int index = 0; index < activities.size(); index++) {
                nodeService.setProperty(activities.get(index), ActivityModel.PROP_INDEX, index);
            }

            HashSet<QName> classes = new HashSet<>(DictionaryUtils.getNodeClassNames(parentRef, nodeService));
            onIndexChangedDelegate.get(classes).onChildrenIndexChanged(parentRef);
        }
    }

    @Override
    public boolean hasActiveChildren(NodeRef nodeRef) {

        mandatoryNodeRef("nodeRef", nodeRef);

        List<NodeRef> children = RepoUtils.getChildrenByAssoc(nodeRef, ActivityModel.ASSOC_ACTIVITIES, nodeService);
        for (NodeRef childRef : children) {
            String state = (String) nodeService.getProperty(childRef, LifeCycleModel.PROP_STATE);
            if (state != null && state.equals(STATE_STARTED)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isActive(NodeRef activityRef) {
        mandatoryNodeRef("activityRef", activityRef);
        return STATE_STARTED.equals(getActivityState(activityRef));
    }

    @Override
    public void registerCreateVariantsProvider(CreateVariantsProvider provider) {
        createVariantsProviders.add(provider);
    }

    @Override
    public List<ActivityCreateVariant> getCreateVariants() {

        List<ActivityCreateVariant> variants = new ArrayList<>();
        for (QName menuType : createMenuTypes) {
            ActivityCreateVariant variant = new ActivityCreateVariant();
            variant.setTitle(dictionaryService.getType(menuType).getTitle(messageService));
            variant.setType(menuType);
            variant.setId(menuType.toPrefixString(namespaceService));
            variant.setCanBeCreated(false);
            variants.add(variant);
        }

        for (CreateVariantsProvider provider : createVariantsProviders) {
            for (ActivityCreateVariant variant : provider.getCreateVariants()) {
                for (ActivityCreateVariant baseVariant : variants) {
                    if (dictionaryService.isSubClass(variant.getType(), baseVariant.getType())) {
                        baseVariant.addChild(variant);
                        break;
                    }
                }
            }
        }

        return variants;
    }

    private void resetActivity(NodeRef activityRef) {

        Map<QName, Serializable> props = new HashMap<>();
        props.put(ActivityModel.PROP_ACTUAL_START_DATE, null);
        props.put(ActivityModel.PROP_ACTUAL_END_DATE, null);
        props.put(LifeCycleModel.PROP_STATE, STATE_NOT_STARTED);
        nodeService.addProperties(activityRef, props);

        HashSet<QName> classes = new HashSet<>(DictionaryUtils.getNodeClassNames(activityRef, nodeService));
        onResetDelegate.get(classes).onCaseActivityReset(activityRef);

        resetActivitiesInChildren(activityRef);
    }

    private void resetActivitiesInChildren(NodeRef nodeRef) {
        List<NodeRef> children = RepoUtils.getChildrenByAssoc(nodeRef, ActivityModel.ASSOC_ACTIVITIES, nodeService);
        for (NodeRef activityRef : children) {
            resetActivity(activityRef);
        }
    }

    private boolean canSetState(NodeRef activityRef, String state) {

        if (!nodeService.exists(activityRef)) {
            return false;
        }

        String currentState = getActivityState(activityRef);

        if (isRequiredReset(activityRef, currentState, state)) {
            reset(activityRef);
            currentState = getActivityState(activityRef);
        }

        if (!currentState.equals(state)) {

            List<String> transitions = allowedTransitions.get(currentState);
            if (transitions != null && transitions.contains(state)) {
                return true;
            }
        }

        return false;
    }

    private boolean isRequiredReset(NodeRef activityRef, String fromState, String toState) {
        if (!STATE_NOT_STARTED.equals(fromState) && STATE_STARTED.equals(toState)) {
            Boolean repeatable = (Boolean) nodeService.getProperty(activityRef, ActivityModel.PROP_REPEATABLE);
            return Boolean.TRUE.equals(repeatable);
        }
        return false;
    }

    private String getActivityState(NodeRef activityRef) {
        String state = (String) nodeService.getProperty(activityRef, LifeCycleModel.PROP_STATE);
        return state != null ? state : STATE_NOT_STARTED;
    }

    private void mandatoryActivity(String paramName, NodeRef activityRef) {
        mandatoryNodeRef(paramName, activityRef);
        QName type = nodeService.getType(activityRef);
        if (!dictionaryService.isSubClass(type, ActivityModel.TYPE_ACTIVITY)) {
            throw new IllegalArgumentException(paramName + " must inherit activ:activity");
        }
    }

    private void mandatoryNodeRef(String paramName, NodeRef nodeRef) {
        if (nodeRef == null) {
            throw new IllegalArgumentException(paramName + " is a mandatory parameter");
        } else if (!nodeService.exists(nodeRef)) {
            throw new IllegalArgumentException("Parameter " + paramName + " have incorrect " +
                                               "NodeRef: " + nodeRef + ". The node doesn't exists.");
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setCreateMenuTypes(List<QName> createMenuTypes) {
        this.createMenuTypes = createMenuTypes;
    }
}
