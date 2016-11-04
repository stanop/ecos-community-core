package ru.citeck.ecos.behavior.common;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    // distinct properties
    private QName className;
    private QName assocName;
    private QName sumField;
    private QName totalSumField;

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
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef,
                                   Map<QName, Serializable> before,
                                   Map<QName, Serializable> after) {
        if (!nodeService.exists(nodeRef)) {return;}
        if (after.get(sumField).equals(before.get(sumField))) {return;}
        BigDecimal sumBefore = (before.get(sumField) != null)
                ? new BigDecimal((Double) before.get(sumField))
                : BigDecimal.ZERO;
        BigDecimal sumAfter = (after.get(sumField) != null)
                ? new BigDecimal((Double) after.get(sumField))
                : BigDecimal.ZERO;
        BigDecimal sumDif = sumAfter.subtract(sumBefore);

        updateTotalSum(nodeRef, sumDif);
    }

    @Override
    public void onCreateAssociation(AssociationRef associationRef) {
        NodeRef nodeRef = associationRef.getTargetRef();
        if (!nodeService.exists(nodeRef)) {return;}
        BigDecimal sumDif = (nodeService.getProperty(nodeRef, totalSumField) != null)
                ? new BigDecimal((Double) nodeService.getProperty(nodeRef, totalSumField))
                : null;
        if (sumDif == null) {return;}
        updateTotalSum(associationRef.getSourceRef(), sumDif);
    }

    @Override
    public void onDeleteAssociation(AssociationRef associationRef) {
        NodeRef nodeRef = associationRef.getTargetRef();
        if (nodeService.exists(nodeRef)) {
            BigDecimal sumDif = (nodeService.getProperty(nodeRef, totalSumField) != null)
                    ? new BigDecimal((Double) nodeService.getProperty(nodeRef, totalSumField))
                    : null;
            if (sumDif == null) {
                return;
            }
            updateTotalSum(associationRef.getSourceRef(), sumDif.negate());
        } else {
            NodeRef archivedNode = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, nodeRef.getId());
            if (!nodeService.exists(archivedNode)) {
                logger.error("Document" + nodeRef + "is not in archive. We can't calculate total sum");
                return;
            }
            BigDecimal sumDif = (nodeService.getProperty(archivedNode, totalSumField) != null)
                    ? new BigDecimal((Double) nodeService.getProperty(archivedNode, totalSumField))
                    : null;
            if (sumDif == null) {return;}
            updateTotalSum(associationRef.getSourceRef(), sumDif.negate());
        }
    }

    private void updateTotalSum(NodeRef nodeRef, BigDecimal sumDif) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating node " + nodeRef + " with total sum " + sumDif);
        }
        BigDecimal totalSumOld = (nodeService.getProperty(nodeRef, totalSumField) != null)
                ? new BigDecimal((Double) nodeService.getProperty(nodeRef, totalSumField))
                : BigDecimal.ZERO;
        BigDecimal totalSumNew = totalSumOld.add(sumDif);
        nodeService.setProperty(nodeRef, totalSumField, totalSumNew);
        if (logger.isDebugEnabled()) {
            logger.debug("Node " + nodeRef + " is updated");
        }
        List<AssociationRef> refs = nodeService.getSourceAssocs(nodeRef, assocName);
        for (AssociationRef ref : refs) {
            updateTotalSum(ref.getSourceRef(), sumDif);
        }
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

}
