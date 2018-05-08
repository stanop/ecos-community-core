package ru.citeck.ecos.behavior.common;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.behavior.JavaBehaviour;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EcosCopyBehaviourCallback extends DefaultCopyBehaviourCallback
                                       implements CopyServicePolicies.OnCopyNodePolicy,
                                                  CopyBehaviourCallback {

    @Autowired
    private PolicyComponent policyComponent;

    private QName className;
    private boolean enabled = true;
    private List<QName> ignoredAttributes;

    @PostConstruct
    public void init() {
        policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME,
                                           className,
                                           new JavaBehaviour(this, "getCopyCallback"));
    }

    @Override
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
        return this;
    }

    @Override
    public Map<QName, Serializable> getCopyProperties(QName classQName,
                                                      CopyDetails copyDetails,
                                                      Map<QName, Serializable> properties) {

        if (!enabled) {
            return super.getCopyProperties(classQName, copyDetails, properties);
        }

        Map<QName, Serializable> resultProps = new HashMap<>(properties);
        for (QName attr : ignoredAttributes) {
            resultProps.remove(attr);
        }

        return resultProps;
    }

    @Override
    public Pair<AssocCopySourceAction, AssocCopyTargetAction> getAssociationCopyAction(
               QName classQName,
               CopyDetails copyDetails,
               CopyAssociationDetails assocCopyDetails) {

        if (!enabled) {
            return super.getAssociationCopyAction(classQName, copyDetails, assocCopyDetails);
        }

        QName assocName = assocCopyDetails.getAssocRef().getTypeQName();
        if (ignoredAttributes.contains(assocName)) {
            AssocCopySourceAction sourceAction = AssocCopySourceAction.IGNORE;
            AssocCopyTargetAction targetAction = AssocCopyTargetAction.USE_ORIGINAL_TARGET;
            return new Pair<>(sourceAction, targetAction);
        }

        return super.getAssociationCopyAction(classQName, copyDetails, assocCopyDetails);
    }

    @Override
    public ChildAssocCopyAction getChildAssociationCopyAction(QName classQName,
                                                              CopyDetails copyDetails,
                                                              CopyChildAssociationDetails childAssocCopyDetails) {

        if (!enabled) {
            return super.getChildAssociationCopyAction(classQName, copyDetails, childAssocCopyDetails);
        }

        QName assocName = childAssocCopyDetails.getChildAssocRef().getTypeQName();
        if (ignoredAttributes.contains(assocName)) {
            return ChildAssocCopyAction.IGNORE;
        }
        return super.getChildAssociationCopyAction(classQName, copyDetails, childAssocCopyDetails);
    }

    public void setIgnoredAttributes(List<QName> ignoredAttributes) {
        this.ignoredAttributes = ignoredAttributes;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    public void setEnabled(boolean value) {
        this.enabled = value;
    }
}
