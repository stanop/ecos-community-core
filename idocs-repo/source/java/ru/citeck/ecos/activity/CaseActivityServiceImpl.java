package ru.citeck.ecos.activity;

import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.*;
import ru.citeck.ecos.utils.DictionaryUtils;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CaseActivityServiceImpl implements CaseActivityService {

    public static final String STATE_STARTED = "Started";
    public static final String STATE_COMPLETED = "Completed";

    private static final Log log = LogFactory.getLog(CaseActivityService.class);

    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    private ClassPolicyDelegate<CaseActivityPolicies.OnCaseActivityStartedPolicy> onActivityStartedDelegate;
    private ClassPolicyDelegate<CaseActivityPolicies.OnCaseActivityStoppedPolicy> onActivityStoppedDelegate;

    public void init() {
        onActivityStartedDelegate = policyComponent.registerClassPolicy(CaseActivityPolicies.OnCaseActivityStartedPolicy.class);
        onActivityStoppedDelegate = policyComponent.registerClassPolicy(CaseActivityPolicies.OnCaseActivityStoppedPolicy.class);
    }

    @Override
    public void startActivity(NodeRef activityRef) {
        if(!setState(activityRef, STATE_STARTED)) return;

        nodeService.setProperty(activityRef, ActivityModel.PROP_ACTUAL_START_DATE, new Date());

        HashSet<QName> classes = new HashSet<QName>(DictionaryUtils.getNodeClassNames(activityRef, nodeService));
        CaseActivityPolicies.OnCaseActivityStartedPolicy policy = onActivityStartedDelegate.get(classes);
        policy.onCaseActivityStarted(activityRef);
    }

    @Override
    public void stopActivity(NodeRef activityRef) {
        if(!setState(activityRef, STATE_COMPLETED)) return;

        nodeService.setProperty(activityRef, ActivityModel.PROP_ACTUAL_END_DATE, new Date());

        HashSet<QName> classes = new HashSet<QName>(DictionaryUtils.getNodeClassNames(activityRef, nodeService));
        CaseActivityPolicies.OnCaseActivityStoppedPolicy policy = onActivityStoppedDelegate.get(classes);
        policy.onCaseActivityStopped(activityRef);
    }

    private boolean setState(NodeRef nodeRef, String state) {
        if (!nodeService.exists(nodeRef)) {
            return false;
        }
        String currentState = (String) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_STATE);

        if(state.equals(currentState)
                || currentState == null && !state.equals(STATE_STARTED)
                || currentState != null && currentState.equals(STATE_COMPLETED)) {
            return false;
        }
        nodeService.setProperty(nodeRef, LifeCycleModel.PROP_STATE, state);
        return true;
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
    public List<NodeRef> getActivities(NodeRef nodeRef, QName type) {
        return RepoUtils.getChildrenByType(nodeRef, type, nodeService);
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
}
