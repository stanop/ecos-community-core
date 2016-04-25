package ru.citeck.ecos.behavior.contracts;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ContractsModel;
import ru.citeck.ecos.utils.ConvertAmountInWords;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClosingDocumentBehaviour implements NodeServicePolicies.OnCreateAssociationPolicy,
        NodeServicePolicies.OnDeleteAssociationPolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private String namespace;
    private String type;



    private static Log logger = LogFactory.getLog(ClosingDocumentBehaviour.class);

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespace(String namespace) { this.namespace = namespace; }

    public void setType(String type) { this.type = type; }

    public void init() {
        bind(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, "onCreateAssociation");
        bind(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, "onDeleteAssociation");
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                QName.createQName(namespace, type),
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    private void bind(QName policy, String method) {
        this.policyComponent.bindAssociationBehaviour(
                policy,
                ContractsModel.TYPE_CONTRACTS_CLOSING_DOCUMENT,
                ContractsModel.ASSOC_CLOSING_DOCUMENT_AGREEMENT,
                new JavaBehaviour(this, method, Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onCreateAssociation(AssociationRef associationRef) {
        NodeRef sourceRef = associationRef.getSourceRef();
        NodeRef targetRef = associationRef.getTargetRef();
        updateAssoc(sourceRef, targetRef, true);
    }

    @Override
    public void onDeleteAssociation(AssociationRef associationRef) {
        NodeRef sourceRef = associationRef.getSourceRef();
        NodeRef targetRef = associationRef.getTargetRef();
        updateAssoc(sourceRef, targetRef, false);
    }

    private void updateAssoc(NodeRef closDoc, NodeRef contracts, boolean isCreate) {
        List<AssociationRef> contractors = nodeService.getTargetAssocs(contracts, ContractsModel.ASSOC_CONTRACTOR);
        if (contractors.size() > 0) {
            if (isCreate) {
                nodeService.createAssociation(closDoc, contractors.get(0).getTargetRef(), ContractsModel.ASSOC_CONTRACTOR);
            } else {
                nodeService.removeAssociation(closDoc,contractors.get(0).getTargetRef(), ContractsModel.ASSOC_CONTRACTOR);
            }
        } else {
            logger.error("Contractor is not exists");
        }
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (!nodeService.exists(nodeRef)) {
            return;
        }

        Double amountAfter = (Double) after.get(ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT);
        Double amountBefore = (Double) before.get(ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT);

        if (!Objects.equals(amountAfter, amountBefore)) {
            setTotalAmountInWords(nodeRef);
        }
    }

    private void setTotalAmountInWords(NodeRef nodeRef) {
        Double amount;
        String paymentCurrency;

        //default
        String currency = "RUB";

        NodeRef currencyRef = RepoUtils.getFirstTargetAssoc(nodeRef, ContractsModel.ASSOC_CLOSING_DOCUMENT_CURRENCY, nodeService);
        paymentCurrency = currencyRef != null ? currencyRef.toString() : "";

        switch (paymentCurrency) {
            case "workspace://SpacesStore/currency-usd": {
                currency = "USD";
                break;
            }
            case "workspace://SpacesStore/currency-eur": {
                currency = "EUR";
                break;
            }
        }

        if (nodeService.getProperty(nodeRef, ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT) != null) {
            amount = (Double) nodeService.getProperty(nodeRef, ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT);
            String amountInWords = ConvertAmountInWords.convert(amount, currency);
            nodeService.setProperty(nodeRef, ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT_IN_WORDS, amountInWords);
        }
    }
}
