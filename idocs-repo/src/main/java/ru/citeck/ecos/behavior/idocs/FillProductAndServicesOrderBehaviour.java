package ru.citeck.ecos.behavior.idocs;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.model.ProductsAndServicesModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author Roman Makarskiy
 */

public class FillProductAndServicesOrderBehaviour implements NodeServicePolicies.OnCreateNodePolicy {

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;
    @Autowired
    protected PolicyComponent policyComponent;

    private QName documentType;
    private QName assocToPas;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                documentType,
                new JavaBehaviour(this, "onCreateNode",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT) {
                }
        );
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        NodeRef document = childAssociationRef.getChildRef();
        if (document == null || !nodeService.exists(document)) {
            return;
        }

        if (recalculateIsNeeded(document)) {
            recalculateOrders(document);
        }
    }

    private boolean recalculateIsNeeded(NodeRef document) {
        List<NodeRef> productAndServices = RepoUtils.getChildrenByAssoc(document, assocToPas, nodeService);

        for (int i = 0; i < productAndServices.size(); i++) {
            NodeRef pas = productAndServices.get(i);
            Integer currentOrder = RepoUtils.getProperty(pas, ProductsAndServicesModel.PROP_ORDER, Integer.class,
                    nodeService);

            if (currentOrder == null) {
                return false;
            }

            Integer requiredOrder = i + 1;

            if (!Objects.equals(currentOrder, requiredOrder)) {
                return true;
            }
        }

        return false;
    }

    private void recalculateOrders(NodeRef document) {
        List<NodeRef> productAndServices = RepoUtils.getChildrenByAssoc(document, assocToPas, nodeService);
        for (int i = 0; i < productAndServices.size(); i++) {
            NodeRef pas = productAndServices.get(i);
            nodeService.setProperty(pas, ProductsAndServicesModel.PROP_ORDER, i + 1);
        }
    }

    public void setDocumentType(QName documentType) {
        this.documentType = documentType;
    }

    public void setAssocToPas(QName assocToPas) {
        this.assocToPas = assocToPas;
    }
}
