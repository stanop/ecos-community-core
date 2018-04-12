package ru.citeck.ecos.behavior.contracts;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.ContractsModel;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.model.ProductsAndServicesModel;
import ru.citeck.ecos.utils.RepoUtils;
import ru.citeck.ecos.utils.converter.amount.AmountInWordConverter;
import ru.citeck.ecos.utils.converter.amount.AmountInWordConverterFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClosingDocumentBehaviour implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.OnCreateAssociationPolicy {

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private String namespace;
    private String type;
    private int order;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                QName.createQName(namespace, type),
                new OrderedBehaviour(this, "onCreateNode",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order)
        );
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                QName.createQName(namespace, type),
                new OrderedBehaviour(this, "onUpdateProperties",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order)
        );
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                ContractsModel.TYPE_CONTRACTS_CLOSING_DOCUMENT,
                ContractsModel.ASSOC_CLOSING_DOCUMENT_CURRENCY,
                new OrderedBehaviour(this, "onCreateAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order)
        );
    }

    @Override
    public void onCreateAssociation(AssociationRef associationRef) {
        NodeRef sourceRef = associationRef.getSourceRef();
        if (!nodeService.exists(sourceRef)) {
            return;
        }
        setTotalAmountInWords(sourceRef);
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        NodeRef paymentRef = childAssociationRef.getChildRef();
        if (!nodeService.exists(paymentRef)) {
            return;
        }
        setTotalAmountInWords(paymentRef);
        cloneProductsAndServices(paymentRef);
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
        String currencyCode = "";

        NodeRef currencyRef = RepoUtils.getFirstTargetAssoc(nodeRef, ContractsModel.ASSOC_CLOSING_DOCUMENT_CURRENCY,
                nodeService);
        NodeRef agreementRef = RepoUtils.getFirstTargetAssoc(nodeRef, ContractsModel.ASSOC_CLOSING_DOCUMENT_AGREEMENT,
                nodeService);

        if (agreementRef != null && nodeService.exists(agreementRef)) {
            currencyRef = RepoUtils.getFirstTargetAssoc(agreementRef, ContractsModel.ASSOC_AGREEMENT_CURRENCY, nodeService);
        }

        if (currencyRef != null && nodeService.exists(currencyRef)) {
            currencyCode = (String) nodeService.getProperty(currencyRef, IdocsModel.PROP_CURRENCY_CODE);
        }

        if (nodeService.getProperty(nodeRef, ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT) != null) {
            amount = (Double) nodeService.getProperty(nodeRef, ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT);
            AmountInWordConverter wordConverter = new AmountInWordConverterFactory().getConverter();
            String amountInWords = wordConverter.convert(amount, currencyCode);
            nodeService.setProperty(nodeRef, ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT_IN_WORDS, amountInWords);
        }
    }

    private void cloneProductsAndServices(NodeRef nodeRef) {
        NodeRef payment = RepoUtils.getFirstTargetAssoc(nodeRef, ContractsModel.ASSOC_CLOSING_DOCUMENT_PAYMENT,
                nodeService);
        if (payment != null) {
            List<NodeRef> paymentProdAndServs = RepoUtils.getChildrenByAssoc(payment,
                    ProductsAndServicesModel.ASSOC_CONTAINS_PRODUCTS_AND_SERVICES, nodeService);
            ProductsAndServicesUtils.setCopiedProductsAndServicesAssocs(nodeRef, paymentProdAndServs, nodeService);
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
