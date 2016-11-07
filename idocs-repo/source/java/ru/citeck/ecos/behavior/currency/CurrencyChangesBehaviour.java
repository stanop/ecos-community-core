package ru.citeck.ecos.behavior.currency;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.currency.CurrencyService;
import ru.citeck.ecos.model.IdocsModel;

import java.io.Serializable;
import java.util.Map;

/**
 * @author alexander.nemerov
 *         date 04.11.2016.
 */
public class CurrencyChangesBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private PolicyComponent policyComponent;
    private CurrencyService currencyService;

    public void init() {
        JavaBehaviour updateBehaviour = new JavaBehaviour(this, "onUpdateProperties",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                IdocsModel.TYPE_CURRENCY, updateBehaviour);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> map, Map<QName, Serializable> map1) {
        currencyService.updateCurrenciesFromRepo();
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }
}
