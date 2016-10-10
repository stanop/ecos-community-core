package ru.citeck.ecos.behavior.contracts;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.ContractsModel;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.utils.RepoUtils;
import ru.citeck.ecos.utils.converter.amount.AmountInWordConverter;
import ru.citeck.ecos.utils.converter.amount.AmountInWordConverterFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class ClosingDocumentBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private String namespace;
    private String type;

    public void setPolicyComponent(PolicyComponent policyComponent) { this.policyComponent = policyComponent; }

    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }

    public void setNamespace(String namespace) { this.namespace = namespace; }

    public void setType(String type) { this.type = type; }

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                QName.createQName(namespace, type),
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
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

        NodeRef currencyRef = RepoUtils.getFirstTargetAssoc(nodeRef, ContractsModel.ASSOC_CLOSING_DOCUMENT_CURRENCY, nodeService);
        NodeRef agreementRef = RepoUtils.getFirstTargetAssoc(nodeRef, ContractsModel.ASSOC_CLOSING_DOCUMENT_AGREEMENT, nodeService);

        if (nodeService.exists(agreementRef)) {
            currencyRef = RepoUtils.getFirstTargetAssoc(agreementRef, ContractsModel.ASSOC_AGREEMENT_CURRENCY, nodeService);
        }

        if (nodeService.exists(currencyRef)) {
            currencyCode = (String) nodeService.getProperty(currencyRef, IdocsModel.PROP_CURRENCY_CODE);
        }

        if (nodeService.getProperty(nodeRef, ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT) != null) {
            amount = (Double) nodeService.getProperty(nodeRef, ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT);
            AmountInWordConverter wordConverter = new AmountInWordConverterFactory().getConverter();
            String amountInWords = wordConverter.convert(amount, currencyCode);
            nodeService.setProperty(nodeRef, ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT_IN_WORDS, amountInWords);
        }
    }
}
