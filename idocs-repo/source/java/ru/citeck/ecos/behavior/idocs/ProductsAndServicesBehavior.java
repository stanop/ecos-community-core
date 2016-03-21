package ru.citeck.ecos.behavior.idocs;

import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeUtils;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ProductsAndServicesModel;
import ru.citeck.ecos.utils.RepoUtils;

/**
 * @author Maxim Strizhov
 */
public class ProductsAndServicesBehavior implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {
    private static final Log log = LogFactory.getLog(ProductsAndServicesBehavior.class);

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private String namespace;
    private String type;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME,
                QName.createQName(namespace, type), new JavaBehaviour(this,
                        "onCreateNode",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                QName.createQName(namespace, type), new JavaBehaviour(this,
                        "onUpdateProperties",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        NodeRef pasEntityRef = childAssociationRef.getChildRef();
        List<NodeRef> sources = RepoUtils.getSourceNodeRefs(pasEntityRef, ProductsAndServicesModel.ASSOC_CONTAINS_PRODUCTS_AND_SERVICES, nodeService);
        List<NodeRef> pasEntityRefs = RepoUtils.getTargetAssoc(sources.get(0), ProductsAndServicesModel.ASSOC_CONTAINS_PRODUCTS_AND_SERVICES, nodeService);
        int maxOrder = -1;
        if (!pasEntityRefs.isEmpty()) {
            for (NodeRef entityRef : pasEntityRefs) {
                if (!pasEntityRef.equals(entityRef)) {
                    Integer order = (Integer) nodeService.getProperty(entityRef, ProductsAndServicesModel.PROP_ORDER);
                    if (order != null && order > maxOrder) {
                        maxOrder = order;
                    }
                }
            }
        }
        if (maxOrder > 0) {
            nodeService.setProperty(pasEntityRef, ProductsAndServicesModel.PROP_ORDER, maxOrder + 1);
        } else {
            nodeService.setProperty(pasEntityRef, ProductsAndServicesModel.PROP_ORDER, 1);
        }
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        Double priceBefore = (Double) before.get(ProductsAndServicesModel.PROP_PRICE_PER_UNIT);
        Double priceAfter = (Double) after.get(ProductsAndServicesModel.PROP_PRICE_PER_UNIT);
        Double quantityBefore = (Double) before.get(ProductsAndServicesModel.PROP_QUANTITY);
        Double quantityAfter = (Double) after.get(ProductsAndServicesModel.PROP_QUANTITY);
        if (!Objects.equals(priceBefore, priceAfter) || !Objects.equals(quantityBefore, quantityAfter)) {
            BigDecimal price = new BigDecimal(priceAfter, MathContext.DECIMAL64);
            BigDecimal quantity = new BigDecimal(quantityAfter, MathContext.DECIMAL64);
            nodeService.setProperty(nodeRef, ProductsAndServicesModel.PROP_TOTAL, (price.multiply(quantity).doubleValue()));
        }
    }
}
