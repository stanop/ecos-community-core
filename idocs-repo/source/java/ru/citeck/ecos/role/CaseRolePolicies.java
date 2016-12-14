package ru.citeck.ecos.role;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.service.CiteckServices;

import java.util.List;
import java.util.Set;

/**
 * @author Pavel Simonov
 */
public interface CaseRolePolicies {

    interface OnRoleAssigneesChangedPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        String NAMESPACE = CiteckServices.CITECK_NAMESPACE;

        QName QNAME = QName.createQName(NAMESPACE, "onRoleAssigneesChanged");

        Arg ARG_0 = Arg.KEY;
        Arg ARG_1 = Arg.KEY;
        Arg ARG_2 = Arg.KEY;

        /**
         * Called when role assignees was changed
         * @param roleRef role node reference
         * @param added removed assignees
         * @param removed added assignees
         */
        void onRoleAssigneesChanged(NodeRef roleRef, Set<NodeRef> added, Set<NodeRef> removed);
    }

    interface OnCaseRolesAssigneesChangedPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        String NAMESPACE = CiteckServices.CITECK_NAMESPACE;

        QName QNAME = QName.createQName(NAMESPACE, "onCaseRolesAssigneesChanged");

        Arg ARG_0 = Arg.KEY;

        /**
         * Called when assignees was changed in any role of case
         * @param caseRef case node reference
         */
        void onCaseRolesAssigneesChanged(NodeRef caseRef);
    }
}
