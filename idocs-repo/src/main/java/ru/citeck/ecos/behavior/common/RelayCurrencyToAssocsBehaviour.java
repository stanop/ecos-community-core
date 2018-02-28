package ru.citeck.ecos.behavior.common;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Makarskiy
 */
public class RelayCurrencyToAssocsBehaviour implements NodeServicePolicies.OnCreateAssociationPolicy,
        NodeServicePolicies.OnDeleteAssociationPolicy {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    private PolicyComponent policyComponent;

    private QName docType;
    private QName childrenAssoc;
    private QName targetAssoc;
    private QName sourceCurrencyAssoc;
    private QName targetCurrencyAssoc;

    public void init() {
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                docType,
                sourceCurrencyAssoc,
                new JavaBehaviour(this, "onCreateAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
                docType,
                sourceCurrencyAssoc,
                new JavaBehaviour(this, "onDeleteAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );


    }

    @Override
    public void onCreateAssociation(AssociationRef nodeAssocRef) {
        NodeRef sourceNode = nodeAssocRef.getSourceRef();
        NodeRef currency = nodeAssocRef.getTargetRef();
        if (!nodeService.exists(currency) || !nodeService.exists(sourceNode)) {
            return;
        }

        List<NodeRef> children = getAssocs(sourceNode);
        children.forEach(child -> {
            removeCurrentCurrency(child, targetCurrencyAssoc);
            nodeService.createAssociation(child, currency, targetCurrencyAssoc);
        });
    }

    @Override
    public void onDeleteAssociation(AssociationRef nodeAssocRef) {
        NodeRef sourceNode = nodeAssocRef.getSourceRef();
        NodeRef currency = nodeAssocRef.getTargetRef();
        if (!nodeService.exists(currency) || !nodeService.exists(sourceNode)) {
            return;
        }

        List<NodeRef> children = getAssocs(sourceNode);
        children.forEach(child -> removeCurrentCurrency(child, targetCurrencyAssoc));
    }

    private List<NodeRef> getAssocs(NodeRef nodeRef) {
        List<NodeRef> assocs = new ArrayList<>();

        if (childrenAssoc != null) {
            List<NodeRef> children = RepoUtils.getChildrenByAssoc(nodeRef, childrenAssoc, nodeService);
            assocs.addAll(children);
        }

        if (targetAssoc != null) {
            List<NodeRef> targetAssocs = RepoUtils.getTargetAssoc(nodeRef, targetAssoc, nodeService);
            assocs.addAll(targetAssocs);
        }

        return assocs;
    }

    private void removeCurrentCurrency(NodeRef nodeRef, QName currencyAssoc) {
        List<NodeRef> currencies = RepoUtils.getTargetAssoc(nodeRef, currencyAssoc, nodeService);
        currencies.forEach(currency -> nodeService.removeAssociation(nodeRef, currency, currencyAssoc));
    }

    public void setDocType(QName docType) {
        this.docType = docType;
    }

    public void setChildrenAssoc(QName childrenAssoc) {
        this.childrenAssoc = childrenAssoc;
    }

    public void setTargetAssoc(QName targetAssoc) {
        this.targetAssoc = targetAssoc;
    }

    public void setSourceCurrencyAssoc(QName sourceCurrencyAssoc) {
        this.sourceCurrencyAssoc = sourceCurrencyAssoc;
    }

    public void setTargetCurrencyAssoc(QName targetCurrencyAssoc) {
        this.targetCurrencyAssoc = targetCurrencyAssoc;
    }
}
