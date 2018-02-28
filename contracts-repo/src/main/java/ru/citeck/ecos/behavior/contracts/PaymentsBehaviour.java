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
import ru.citeck.ecos.model.ProductsAndServicesModel;

import java.util.List;

/**
 * @author Roman Makarskiy
 */
public class PaymentsBehaviour implements NodeServicePolicies.OnCreateNodePolicy {

    private NodeService nodeService;
    private SearchService searchService;
    private PolicyComponent policyComponent;
    private QName type;
    private int order;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                type,
                new OrderedBehaviour(this, "onCreateNode",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order)
        );
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        NodeRef paymentRef = childAssociationRef.getChildRef();
        if (!nodeService.exists(paymentRef)) {
            return;
        }
        setCopiedAssociations(paymentRef);
    }

    private void setCopiedAssociations(NodeRef nodeRef) {
        List<AssociationRef> origProdAndServs = nodeService.getTargetAssocs(nodeRef,
                ProductsAndServicesModel.ASSOC_CONTAINS_ORIG_PRODUCTS_AND_SERVICES);
        ProductsAndServicesUtils.setCopiedProductsAndServicesAssocs(nodeRef, origProdAndServs, searchService,
                nodeService);
    }

    public void setType(QName type) {
        this.type = type;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
