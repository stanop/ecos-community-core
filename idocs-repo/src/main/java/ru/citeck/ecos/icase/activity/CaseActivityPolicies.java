package ru.citeck.ecos.icase.activity;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.service.CiteckServices;

/**
 * @author Pavel Simonov
 */
public interface CaseActivityPolicies {

    interface BeforeCaseActivityStartedPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        String NAMESPACE = CiteckServices.CITECK_NAMESPACE;

        QName QNAME = QName.createQName(NAMESPACE, "beforeCaseActivityStarted");

        /**
         * Called before activity start event occurs
         * @param activityRef instance reference
         */
        void beforeCaseActivityStarted(NodeRef activityRef);
    }

    interface OnCaseActivityStartedPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        String NAMESPACE = CiteckServices.CITECK_NAMESPACE;

        QName QNAME = QName.createQName(NAMESPACE, "onCaseActivityStarted");

        /**
         * Called when activity start event occurs
         * @param activityRef instance reference
         */
        void onCaseActivityStarted(NodeRef activityRef);
    }

    interface BeforeCaseActivityStoppedPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        String NAMESPACE = CiteckServices.CITECK_NAMESPACE;

        QName QNAME = QName.createQName(NAMESPACE, "beforeCaseActivityStopped");

        /**
         * Called when activity stop event occurs
         * @param activityRef instance reference
         */
        void beforeCaseActivityStopped(NodeRef activityRef);
    }

    interface OnCaseActivityStoppedPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        String NAMESPACE = CiteckServices.CITECK_NAMESPACE;

        QName QNAME = QName.createQName(NAMESPACE, "onCaseActivityStopped");

        /**
         * Called when activity stop event occurs
         * @param activityRef instance reference
         */
        void onCaseActivityStopped(NodeRef activityRef);
    }

    interface OnCaseActivityResetPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        String NAMESPACE = CiteckServices.CITECK_NAMESPACE;

        QName QNAME = QName.createQName(NAMESPACE, "onCaseActivityReset");

        /**
         * Called when activity reset event occurs
         * @param activityRef instance reference
         */
        void onCaseActivityReset(NodeRef activityRef);
    }

    interface OnChildrenIndexChangedPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        String NAMESPACE = CiteckServices.CITECK_NAMESPACE;

        QName QNAME = QName.createQName(NAMESPACE, "onChildrenIndexChanged");

        Arg ARG_0 = Arg.KEY;

        /**
         * Called when children indexes was changed
         * @param activityRef instance reference
         */
        void onChildrenIndexChanged(NodeRef activityRef);
    }
}
