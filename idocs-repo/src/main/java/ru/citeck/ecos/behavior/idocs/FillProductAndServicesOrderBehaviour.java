package ru.citeck.ecos.behavior.idocs;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
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
    private int order;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                documentType,
                new OrderedBehaviour(this, "onCreateNode",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order) {
                }
        );
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        NodeRef document = childAssociationRef.getChildRef();
        if (document == null || !nodeService.exists(document)) {
            return;
        }

        ProductsAndServicesUtils.recalculateOrdersIfRequired(document, assocToPas, nodeService);
    }

    public void setDocumentType(QName documentType) {
        this.documentType = documentType;
    }

    public void setAssocToPas(QName assocToPas) {
        this.assocToPas = assocToPas;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
