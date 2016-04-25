package ru.citeck.ecos.behavior.contracts;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.PaymentsModel;
import ru.citeck.ecos.utils.ConvertAmountInWords;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roman.Makarskiy on 22.04.2016.
 */
public class PaymentsBehaviour implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private String namespace;
    private String type;

    public void setType(String type) {
        this.type = type;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                QName.createQName(namespace, type),
                new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                QName.createQName(namespace, type),
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        NodeRef paymentRef = childAssociationRef.getChildRef();
        if (!nodeService.exists(paymentRef)) {
            return;
        }
        setTotalAmountInWords(paymentRef);
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
        String paymentCurrency;

        //default
        String currency = "RUB";

        NodeRef currencyRef = RepoUtils.getFirstTargetAssoc(nodeRef, PaymentsModel.ASSOC_PAYMENT_CURRENCY, nodeService);
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

        if (nodeService.getProperty(nodeRef, PaymentsModel.PROP_PAYMENT_AMOUNT) != null) {
            amount = (Double) nodeService.getProperty(nodeRef, PaymentsModel.PROP_PAYMENT_AMOUNT);
            String amountInWords = ConvertAmountInWords.convert(amount, currency);
            nodeService.setProperty(nodeRef, PaymentsModel.PROP_PAYMENT_AMOUNT_IN_WORDS, amountInWords);
        }
    }
}
