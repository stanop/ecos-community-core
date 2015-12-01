package ru.citeck.ecos.icase.activity;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.service.CiteckServices;

/**
 * @author Pavel Simonov
 */
public interface CaseActivityPolicies {

    public interface OnCaseActivityStartedPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        public static final String NAMESPACE = CiteckServices.CITECK_NAMESPACE;

        public static final QName QNAME = QName.createQName(NAMESPACE, "onCaseActivityStarted");

        /**
         * Called when activity start event occurs
         * @param activityRef instance reference
         */
        public void onCaseActivityStarted(NodeRef activityRef);
    }

    public interface OnCaseActivityStoppedPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        public static final String NAMESPACE = CiteckServices.CITECK_NAMESPACE;

        public static final QName QNAME = QName.createQName(NAMESPACE, "onCaseActivityStopped");

        /**
         * Called when activity stop event occurs
         * @param activityRef instance reference
         */
        public void onCaseActivityStopped(NodeRef activityRef);
    }
}
