/*
 * Copyright (C) 2008-2018 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */

package ru.citeck.ecos.behavior.contracts;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.model.ProductsAndServicesModel;
import ru.citeck.ecos.utils.MathUtils;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This behaviour react to change sum in "products and services" and calculate total amount in Documents
 *
 * @author Roman Makarskiy
 */
public class ProductsAndServicesTotalAmountInDocumentBehaviour implements NodeServicePolicies.OnCreateNodePolicy,
        NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.BeforeDeleteNodePolicy {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    private PolicyComponent policyComponent;

    private Map<QName, QName> allowedTypes;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                ProductsAndServicesModel.TYPE_ENTITY_COPIED,
                new JavaBehaviour(this, "onUpdateProperties",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );

        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                ProductsAndServicesModel.TYPE_ENTITY_COPIED,
                new JavaBehaviour(this, "onCreateNode",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );

        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                ProductsAndServicesModel.TYPE_ENTITY_COPIED,
                new JavaBehaviour(this, "beforeDeleteNode",
                        Behaviour.NotificationFrequency.FIRST_EVENT)
        );
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        NodeRef pasEntityRef = childAssocRef.getChildRef();
        if (!nodeService.exists(pasEntityRef)) {
            return;
        }
        updateTotalAmount(pasEntityRef);
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef) {
        if (!nodeService.exists(nodeRef)) {
            return;
        }
        updateTotalAmount(nodeRef);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (!nodeService.exists(nodeRef)) {
            return;
        }

        Serializable pasAmountBefore = before.get(ProductsAndServicesModel.PROP_TOTAL);
        Serializable pasAmountAfter = after.get(ProductsAndServicesModel.PROP_TOTAL);

        if (!Objects.equals(pasAmountBefore, pasAmountAfter)) {
            updateTotalAmount(nodeRef);
        }
    }

    private void updateTotalAmount(NodeRef nodeRef) {
        allowedTypes.forEach((documentType, totalAmountProperty)
                -> processPasEntry(nodeRef, documentType, totalAmountProperty));
    }

    private void processPasEntry(NodeRef pasEntityRef, QName requiredDocumentType, QName totalAmountProperty) {
        NodeRef document = RepoUtils.getPrimaryParentRef(pasEntityRef, nodeService);
        updateTotalAmountInDocument(document, requiredDocumentType, totalAmountProperty);
    }

    private void updateTotalAmountInDocument(NodeRef document, QName requiredDocumentType, QName totalAmountProperty) {
        QName docType = nodeService.getType(document);
        if (docType.equals(requiredDocumentType)) {
            BigDecimal totalAmount = getTotalAmount(document);
            BigDecimal currentlyAmount = MathUtils.createBigDecimalFromPropOrZero(document, totalAmountProperty,
                    nodeService);

            if (!Objects.equals(totalAmount, currentlyAmount)) {
                nodeService.setProperty(document, totalAmountProperty, totalAmount);
            }
        }
    }

    private BigDecimal getTotalAmount(NodeRef nodeRef) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<NodeRef> productsAndServices = RepoUtils.getChildrenByAssoc(nodeRef,
                ProductsAndServicesModel.ASSOC_CONTAINS_PRODUCTS_AND_SERVICES,
                nodeService);

        for (NodeRef pasEntity : productsAndServices) {
            if (!nodeService.hasAspect(pasEntity, ContentModel.ASPECT_PENDING_DELETE)) {
                BigDecimal amount = MathUtils.createBigDecimalFromPropOrZero(pasEntity,
                        ProductsAndServicesModel.PROP_TOTAL, nodeService);
                totalAmount = totalAmount.add(amount);
            }
        }

        return totalAmount;
    }

    public void setAllowedTypes(Map<QName, QName> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }
}
