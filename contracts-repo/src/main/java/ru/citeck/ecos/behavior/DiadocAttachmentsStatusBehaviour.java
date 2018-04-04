package ru.citeck.ecos.behavior;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import ru.citeck.ecos.model.SecurityModel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Pavel Simonov
 */
public abstract class DiadocAttachmentsStatusBehaviour<T> implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private static final String ANY_STATUS = "ANY";

    protected PolicyComponent policyComponent;
    protected ScriptService scriptService;
    protected NodeService nodeService;

    private QName attachmentClassName;
    private QName parentAssocName;
    private QName assocName;

    protected int order = 100;

    private Map<Transition, T> transitions;

    public void init() {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                attachmentClassName,
                new OrderedBehaviour(this, "onUpdateProperties",
                                     Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order)
        );
    }

    @Override
    public void onUpdateProperties(final NodeRef attachmentRef, final Map<QName, Serializable> before,
                                   final Map<QName, Serializable> after) {

        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {
                onUpdatePropertiesImpl(attachmentRef, before, after);
                return null;
            }
        });
    }

    protected void onUpdatePropertiesImpl(NodeRef attachmentRef, Map<QName, Serializable> before,
                                        Map<QName, Serializable> after) {

        String statusBefore = (String) before.get(SecurityModel.PROP_PACKAGE_ATMNT_STATUS);
        String statusAfter = (String) after.get(SecurityModel.PROP_PACKAGE_ATMNT_STATUS);

        if (nodeService.exists(attachmentRef) && !Objects.equals(statusBefore, statusAfter)) {
            NodeRef parentRef = getParent(attachmentRef);
            if (parentRef != null) {
                Transition transition = getActualTransition(statusBefore, statusAfter);
                if (transition != null) {
                    processTransition(parentRef, attachmentRef, transition, getTransitionData(transition));
                }
            }
        }
    }

    protected T getTransitionData(Transition transition) {
        if (transitions == null || transition == null) {
            return null;
        }
        return transitions.get(transition);
    }

    protected Transition getActualTransition(String statusBefore, String statusAfter) {
        Transition exactTransition = new Transition(statusBefore, statusAfter);
        if (transitions == null || transitions.containsKey(exactTransition)) {
            return exactTransition;
        }
        Transition fromAnyTransition = new Transition(ANY_STATUS, statusAfter);
        if (transitions.containsKey(fromAnyTransition)) {
            return fromAnyTransition;
        }
        Transition toAnyTransition = new Transition(statusBefore, ANY_STATUS);
        if (transitions.containsKey(toAnyTransition)) {
            return toAnyTransition;
        }
        Transition fromAnyToAnyTransition = new Transition(ANY_STATUS, ANY_STATUS);
        if (transitions.containsKey(fromAnyToAnyTransition)) {
            return fromAnyToAnyTransition;
        }
        return null;
    }

    private NodeRef getParent(NodeRef attachmentRef) {
        QNamePattern assocQNamePattern = RegexQNamePattern.MATCH_ALL;
        if (parentAssocName != null) {
            List<ChildAssociationRef> assocs = nodeService.getParentAssocs(attachmentRef, parentAssocName, assocQNamePattern);
            if (assocs != null && !assocs.isEmpty()) {
                return assocs.get(0).getParentRef();
            }
        }

        if (assocName != null) {
            List<AssociationRef> assocs = nodeService.getSourceAssocs(attachmentRef, assocName);
            if (assocs != null && !assocs.isEmpty()) {
                return assocs.get(0).getSourceRef();
            }
        }

        return null;
    }

    protected abstract void processTransition(NodeRef parentRef, NodeRef attachmentRef,
                                              Transition transition, T data);

    public void setAttachmentClassName(QName attachmentClassName) {
        this.attachmentClassName = attachmentClassName;
    }

    public void setParentAssocName(QName parentAssocName) {
        this.parentAssocName = parentAssocName;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setTransitions(Map<Transition, T> transitions) {
        this.transitions = transitions;
    }

    public void setAssocName(QName assocName) {
        this.assocName = assocName;
    }

    public static class Transition implements Serializable {
        public final String from;
        public final String to;

        public Transition() {
            this.from = ANY_STATUS;
            this.to = ANY_STATUS;
        }

        public Transition(String from, String to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Transition that = (Transition) o;
            return Objects.equals(from, that.from) &&
                   Objects.equals(to, that.to);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(from);
            result = 31 * result + Objects.hashCode(to);
            return result;
        }
    }
}
