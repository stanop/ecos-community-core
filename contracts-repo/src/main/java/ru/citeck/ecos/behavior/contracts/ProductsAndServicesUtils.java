package ru.citeck.ecos.behavior.contracts;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.ProductsAndServicesModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductsAndServicesUtils {

    public static void setCopiedProductsAndServicesAssocs(NodeRef nodeRef, List<NodeRef> assocsToClone, NodeService nodeService) {

        for (NodeRef assocRef : assocsToClone) {
            Map<QName, Serializable> nodeProps = new HashMap<>(1);
            nodeProps.put(ContentModel.PROP_TITLE, nodeService.getProperty(assocRef,
                    ContentModel.PROP_TITLE));
            nodeProps.put(ContentModel.PROP_DESCRIPTION, nodeService.getProperty(assocRef,
                    ContentModel.PROP_DESCRIPTION));
            nodeProps.put(ProductsAndServicesModel.PROP_PRICE_PER_UNIT, nodeService.getProperty(assocRef,
                    ProductsAndServicesModel.PROP_PRICE_PER_UNIT));
            nodeProps.put(ProductsAndServicesModel.PROP_TYPE, nodeService.getProperty(assocRef,
                    ProductsAndServicesModel.PROP_TYPE));

            if (nodeService.getProperty(assocRef, ProductsAndServicesModel.PROP_QUANTITY) != null) {
                nodeProps.put(ProductsAndServicesModel.PROP_QUANTITY, nodeService.getProperty(assocRef,
                        ProductsAndServicesModel.PROP_QUANTITY));
            } else {
                nodeProps.put(ProductsAndServicesModel.PROP_QUANTITY, "1");
            }

            nodeProps.put(ProductsAndServicesModel.PROP_TOTAL, nodeService.getProperty(assocRef,
                    ProductsAndServicesModel.PROP_PRICE_PER_UNIT));

            ChildAssociationRef childAssocRef = nodeService.createNode(
                    nodeRef,
                    ProductsAndServicesModel.ASSOC_CONTAINS_PRODUCTS_AND_SERVICES,
                    ProductsAndServicesModel.ASSOC_PROD_AND_SERV,
                    ProductsAndServicesModel.TYPE_ENTITY_COPIED,
                    nodeProps
            );

            NodeRef childNodeRef = childAssocRef.getChildRef();

            nodeService.addAspect(childNodeRef, ProductsAndServicesModel.ASPECT_HASUNIT, null);

            List<AssociationRef> assocRefs = nodeService.getTargetAssocs(assocRef,
                    ProductsAndServicesModel.ASSOC_ENTITY_UNIT);
            nodeService.createAssociation(childNodeRef, assocRefs.get(0).getTargetRef(),
                    ProductsAndServicesModel.ASSOC_ENTITY_UNIT);

            NodeRef currencyRef = (nodeService.getTargetAssocs(assocRef,
                    ProductsAndServicesModel.ASSOC_CURRENCY)).get(0).getTargetRef();
            nodeService.createAssociation(childNodeRef, currencyRef, ProductsAndServicesModel.ASSOC_CURRENCY);

            nodeService.removeAssociation(nodeRef, assocRef,
                    ProductsAndServicesModel.ASSOC_CONTAINS_ORIG_PRODUCTS_AND_SERVICES);
        }
    }
}
