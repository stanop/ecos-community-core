package ru.citeck.ecos.behavior.common;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.utils.RepoUtils;
import ru.citeck.ecos.utils.converter.amount.AmountInWordConverter;
import ru.citeck.ecos.utils.converter.amount.AmountInWordConverterFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roman Makarskiy
 */
public class AmountInWordBehaviour implements NodeServicePolicies.OnCreateAssociationPolicy,
        NodeServicePolicies.OnUpdatePropertiesPolicy {

    private static final int ORDER = 80;

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    private PolicyComponent policyComponent;

    private QName docType;
    private QName amountProperty;
    private QName amountInWordProperty;
    private QName currencyAssoc;
    private String language;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                docType,
                new OrderedBehaviour(this, "onUpdateProperties",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT, ORDER)
        );
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                docType,
                currencyAssoc,
                new OrderedBehaviour(this, "onCreateAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT, ORDER)
        );
    }

    @Override
    public void onCreateAssociation(AssociationRef nodeAssocRef) {
        NodeRef docRef = nodeAssocRef.getSourceRef();
        if (!nodeService.exists(docRef)) {
            return;
        }
        updateTotalAmountInWord(docRef);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (!nodeService.exists(nodeRef)) {
            return;
        }

        Double amountAfter = (Double) after.get(amountProperty);
        Double amountBefore = (Double) before.get(amountProperty);

        if (!Objects.equals(amountAfter, amountBefore)) {
            updateTotalAmountInWord(nodeRef);
        }
    }

    private void updateTotalAmountInWord(NodeRef docRef) {
        Double amount = (Double) nodeService.getProperty(docRef, amountProperty);

        if (amount == null) {
            nodeService.removeProperty(docRef, amountInWordProperty);
        } else {
            String currencyCode = "";
            NodeRef currencyRef = RepoUtils.getFirstTargetAssoc(docRef, currencyAssoc, nodeService);

            if (currencyRef != null && nodeService.exists(currencyRef)) {
                currencyCode = (String) nodeService.getProperty(currencyRef, IdocsModel.PROP_CURRENCY_CODE);
            }

            AmountInWordConverter wordConverter;

            if (StringUtils.isNotBlank(language)) {
                wordConverter = new AmountInWordConverterFactory().getConverter(language);
            } else {
                wordConverter = new AmountInWordConverterFactory().getConverter();
            }

            String amountInWords = wordConverter.convert(amount, currencyCode);

            nodeService.setProperty(docRef, amountInWordProperty, amountInWords);
        }
    }

    public void setDocType(QName docType) {
        this.docType = docType;
    }

    public void setAmountProperty(QName amountProperty) {
        this.amountProperty = amountProperty;
    }

    public void setAmountInWordProperty(QName amountInWordProperty) {
        this.amountInWordProperty = amountInWordProperty;
    }

    public void setCurrencyAssoc(QName currencyAssoc) {
        this.currencyAssoc = currencyAssoc;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
