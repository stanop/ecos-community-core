package ru.citeck.ecos.behavior.contracts;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.model.PaymentsModel;
import ru.citeck.ecos.model.ProductsAndServicesModel;
import ru.citeck.ecos.utils.RepoUtils;
import ru.citeck.ecos.utils.converter.amount.AmountInWordConverter;
import ru.citeck.ecos.utils.converter.amount.AmountInWordConverterFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roman.Makarskiy on 22.04.2016.
 */
public class PaymentsBehaviour implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.OnCreateAssociationPolicy {

    private NodeService nodeService;
    private SearchService searchService;
    private PolicyComponent policyComponent;
    private String namespace;
    private String type;
    private int order;

    public void setType(String type) {
        this.type = type;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) { this.policyComponent = policyComponent; }

    public void setOrder(int order) { this.order = order; }

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                QName.createQName(namespace, type),
                new OrderedBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order)
        );
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                QName.createQName(namespace, type),
                new OrderedBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order)
        );
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                PaymentsModel.TYPE,
                PaymentsModel.ASSOC_PAYMENT_CURRENCY,
                new OrderedBehaviour(this, "onCreateAssociation", Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order)
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
        setCopiedAssociations(paymentRef);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

        if (!nodeService.exists(nodeRef)) {
            return;
        }

        Double amountAfter = (Double) after.get(PaymentsModel.PROP_PAYMENT_AMOUNT);
        Double amountBefore = (Double) before.get(PaymentsModel.PROP_PAYMENT_AMOUNT);

        if (!Objects.equals(amountAfter, amountBefore)) {
            setTotalAmountInWords(nodeRef);
        }
    }

    private void setTotalAmountInWords(NodeRef nodeRef) {
        Double amount;
        String currencyCode = "";

        NodeRef currencyRef = RepoUtils.getFirstTargetAssoc(nodeRef, PaymentsModel.ASSOC_PAYMENT_CURRENCY, nodeService);

        if (currencyRef != null && nodeService.exists(currencyRef)) {
            currencyCode = (String) nodeService.getProperty(currencyRef, IdocsModel.PROP_CURRENCY_CODE);
        }

        if (nodeService.getProperty(nodeRef, PaymentsModel.PROP_PAYMENT_AMOUNT) != null) {
            amount = (Double) nodeService.getProperty(nodeRef, PaymentsModel.PROP_PAYMENT_AMOUNT);

            AmountInWordConverter wordConverter = new AmountInWordConverterFactory().getConverter();
            String amountInWords = wordConverter.convert(amount, currencyCode);

            nodeService.setProperty(nodeRef, PaymentsModel.PROP_PAYMENT_AMOUNT_IN_WORDS, amountInWords);
        }
    }

    private void setCopiedAssociations(NodeRef nodeRef) {
        List<AssociationRef> origProdAndServs = nodeService.getTargetAssocs(nodeRef, ProductsAndServicesModel.ASSOC_CONTAINS_ORIG_PRODUCTS_AND_SERVICES);
        ProductsAndServicesUtils.setCopiedProductsAndServicesAssocs(nodeRef, origProdAndServs, searchService, nodeService);
    }
}
