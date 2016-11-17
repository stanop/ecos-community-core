package ru.citeck.ecos.behavior.common;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.currency.Currency;
import ru.citeck.ecos.currency.CurrencyService;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author alexander.nemerov
 *         date 01.11.2016.
 */
public class TotalDocumentsSumBehaviour implements
        NodeServicePolicies.OnUpdatePropertiesPolicy,
        NodeServicePolicies.OnCreateAssociationPolicy,
        NodeServicePolicies.OnDeleteAssociationPolicy {

    private static Log logger = LogFactory.getLog(FieldAutoFillBehaviour.class);

    // common properties
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private CurrencyService currencyService;

    // distinct properties
    private QName className;
    private QName assocName;
    private QName sumField;
    private QName sumCurrencyField;
    private QName totalSumField;
    private QName totalSumCurrencyField;
    private String totalSumCurrencyDefault;

    public void init() {
        JavaBehaviour updateBehaviour = new JavaBehaviour(this, "onUpdateProperties",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                className, updateBehaviour);
        JavaBehaviour createAssocBehaviour = new JavaBehaviour(this, "onCreateAssociation",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                className, assocName, createAssocBehaviour);
        JavaBehaviour deleteAssocBehaviour = new JavaBehaviour(this, "onDeleteAssociation",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
                className, assocName, deleteAssocBehaviour);
        JavaBehaviour createCurrencyBehaviour = new JavaBehaviour(this, "onCreateCurrency",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                className, sumCurrencyField, createCurrencyBehaviour);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef,
                                   Map<QName, Serializable> before,
                                   Map<QName, Serializable> after) {
        if (!nodeService.exists(nodeRef)) {return;}
        if (after.get(sumField).equals(before.get(sumField))) {return;}
        recalculateBranch(nodeRef);
    }

    @Override
    public void onCreateAssociation(AssociationRef associationRef) {
        NodeRef nodeRef = associationRef.getTargetRef();
        if (!nodeService.exists(nodeRef)) {return;}
        recalculateBranch(nodeRef);
    }

    @Override
    public void onDeleteAssociation(AssociationRef associationRef) {
        NodeRef nodeRef = associationRef.getTargetRef();
        if (nodeService.exists(nodeRef)) {
            recalculateBranch(nodeRef);
        } else {
            NodeRef archivedNode = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, nodeRef.getId());
            if (!nodeService.exists(archivedNode)) {
                logger.error("Document" + nodeRef + "is not in archive. We can't calculate total sum");
                return;
            }
            QName assocNameAdded = QName.createQName(assocName.toString() + "_added");
            if(nodeService.getProperty(archivedNode, assocNameAdded) == null) {
                return;
            }
            List<String> nodeRefs = (List<String>) nodeService.getProperty(archivedNode, assocNameAdded);
            for (String ref : nodeRefs) {
                recalculateBranch(new NodeRef(ref));
            }
        }
    }

    public void onCreateCurrency(AssociationRef associationRef) {
        NodeRef nodeRef = associationRef.getSourceRef();
        if(nodeService.exists(nodeRef)) {
            recalculateBranch(nodeRef);
        }
    }

    private void recalculateBranch(NodeRef nodeRef) {
        List<AssociationRef> refs = nodeService.getSourceAssocs(nodeRef, assocName);
        BigDecimal total = BigDecimal.ZERO;
        Currency currencyTo = getCurrencyByAssocName(nodeRef, totalSumCurrencyField);
        if (currencyTo == null) {
            currencyTo = currencyService.getCurrencyByCode(totalSumCurrencyDefault);
        }
        for (AssociationRef ref : refs) {
            if(nodeService.getProperty(ref.getSourceRef(), totalSumField) == null) {
                continue;
            }
            Currency currencyFrom = getCurrencyByAssocName(ref.getSourceRef(), totalSumCurrencyField);
            if (currencyFrom == null) {
                currencyFrom = currencyService.getCurrencyByCode(totalSumCurrencyDefault);
            }
            BigDecimal sum = new BigDecimal((Double) nodeService.getProperty(ref.getSourceRef(), totalSumField));
            total = total.add(currencyService.transferFromOneCurrencyToOther(currencyFrom, currencyTo, sum));
        }
        Currency currentCurrency = getCurrencyByAssocName(nodeRef, sumCurrencyField);
        BigDecimal currentSum = (nodeService.getProperty(nodeRef, sumField) != null)
                ? new BigDecimal((Double) nodeService.getProperty(nodeRef, sumField))
                : BigDecimal.ZERO;
        total = total.add(currencyService.transferFromOneCurrencyToOther(currentCurrency, currencyTo, currentSum));
        total = total.setScale(2, BigDecimal.ROUND_HALF_UP);
        nodeService.setProperty(nodeRef, totalSumField, total.doubleValue());
        List<AssociationRef> targetRefs = nodeService.getTargetAssocs(nodeRef, assocName);
        for (AssociationRef targetRef : targetRefs) {
            recalculateBranch(targetRef.getTargetRef());
        }
    }

    private Currency getCurrencyByAssocName(NodeRef nodeRef, QName assocName) {
        List<AssociationRef> refs = nodeService.getTargetAssocs(nodeRef, assocName);
        if(refs == null || refs.size() == 0) {
            return null;
        }
        return currencyService.getCurrencyByNodeRef(refs.get(0).getTargetRef());
    }

    //spring

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    public void setAssocName(QName assocName) {
        this.assocName = assocName;
    }

    public void setSumField(QName sumField) {
        this.sumField = sumField;
    }

    public void setTotalSumField(QName totalSumField) {
        this.totalSumField = totalSumField;
    }

    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public void setSumCurrencyField(QName sumCurrencyField) {
        this.sumCurrencyField = sumCurrencyField;
    }

    public void setTotalSumCurrencyField(QName totalSumCurrencyField) {
        this.totalSumCurrencyField = totalSumCurrencyField;
    }

    public void setTotalSumCurrencyDefault(String totalSumCurrencyDefault) {
        this.totalSumCurrencyDefault = totalSumCurrencyDefault;
    }

}
