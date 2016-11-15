package ru.citeck.ecos.icase;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.service.CiteckServices;

/**
 * @author Roman Makarskiy
 */
public interface CaseStatusPolicies {

    public interface OnCaseStatusChangedPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        public static final String NAMESPACE = CiteckServices.CITECK_NAMESPACE;
        public static final QName QNAME = QName.createQName(NAMESPACE, "onCaseStatusChanged");

        //without this fields TransactionBehaviourQueue takes invocations
        //with a same first argument as totally equals and processes only one
        static Arg ARG_0 = Arg.KEY;
        static Arg ARG_2 = Arg.KEY;

        /**
         * Called when case status changed.
         *
         * @param documentRef document nodeRef which changed case status
         * @param caseStatusBefore case status nodeRef before changed
         * @param caseStatusAfter case status nodeRef after changed
         */
        public void onCaseStatusChanged(NodeRef documentRef, NodeRef caseStatusBefore, NodeRef caseStatusAfter);
    }
}
