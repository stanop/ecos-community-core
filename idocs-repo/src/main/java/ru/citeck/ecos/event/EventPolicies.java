package ru.citeck.ecos.event;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.service.CiteckServices;

/**
 * @author Pavel Simonov
 */
public interface EventPolicies {

    public interface OnEventPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        public static final String NAMESPACE = CiteckServices.CITECK_NAMESPACE;

        public static final QName QNAME = QName.createQName(NAMESPACE, "onEvent");

        /**
         * Called on event
         * @param eventRef event instance reference
         */
        public void onEvent(NodeRef eventRef);
    }

    public interface BeforeEventPolicy extends ClassPolicy {
        // NOTE: this is important, that this field is here
        // if it is removed, this behaviours will be registered with
        // default namespace and will not be matched
        public static final String NAMESPACE = CiteckServices.CITECK_NAMESPACE;

        public static final QName QNAME = QName.createQName(NAMESPACE, "beforeEvent");

        /**
         * Called before event
         * @param eventRef event instance reference
         */
        public void beforeEvent(NodeRef eventRef);
    }

}
