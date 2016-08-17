/*
 * Copyright (C) 2008-2016 Citeck LLC.
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

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ContractsModel;
import ru.citeck.ecos.model.PaymentsModel;
import ru.citeck.ecos.model.ProductsAndServicesModel;
import ru.citeck.ecos.template.GenerateContentActionExecuter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This behaviour react to change sum in "products and services" and calculate total amount in Documents
 *
 * @author Roman.Makarskiy on 04.04.2016.
 */
public class ProductsAndServicesBehaviorContracts implements NodeServicePolicies.OnCreateNodePolicy,
        NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.BeforeDeleteNodePolicy {

    private static Log logger = LogFactory.getLog(ProductsAndServicesBehaviorContracts.class);

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private String namespace;
    private String type;
    private ServiceRegistry serviceRegistry;

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

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                QName.createQName(namespace, type),
                new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                QName.createQName(namespace, type),
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                QName.createQName(namespace, type),
                new JavaBehaviour(this, "beforeDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT)
        );
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef) {
        if (!nodeService.exists(nodeRef)) {
            return;
        }
        updateTotalAmountInDocument(nodeRef, PaymentsModel.TYPE, PaymentsModel.PROP_PAYMENT_AMOUNT);
        updateTotalAmountInDocument(nodeRef, ContractsModel.TYPE_CONTRACTS_CLOSING_DOCUMENT,
                ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT);
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        NodeRef pasEntityRef = childAssociationRef.getChildRef();
        if (!nodeService.exists(pasEntityRef)) {
            return;
        }
        logger.error("before updateTotalAmountInDocument");
        updateTotalAmountInDocument(pasEntityRef, PaymentsModel.TYPE, PaymentsModel.PROP_PAYMENT_AMOUNT);
        updateTotalAmountInDocument(pasEntityRef, ContractsModel.TYPE_CONTRACTS_CLOSING_DOCUMENT,
                ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT);
        logger.error("after updateTotalAmountInDocument");
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> map, Map<QName, Serializable> map1) {
        if (!nodeService.exists(nodeRef)) {
            return;
        }
        updateTotalAmountInDocument(nodeRef, PaymentsModel.TYPE, PaymentsModel.PROP_PAYMENT_AMOUNT);
        updateTotalAmountInDocument(nodeRef, ContractsModel.TYPE_CONTRACTS_CLOSING_DOCUMENT,
                ContractsModel.PROP_CLOSING_DOCUMENT_AMOUNT);

    }

    private BigDecimal getTotalAmount(NodeRef nodeRef) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<AssociationRef> products;
        if (nodeService.exists(nodeRef)) {
            products = nodeService.getTargetAssocs(nodeRef, ProductsAndServicesModel.ASSOC_CONTAINS_PRODUCTS_AND_SERVICES);
            for (AssociationRef associationRef : products) {
                BigDecimal amount = new BigDecimal(
                        (double) nodeService.getProperty(associationRef.getTargetRef(),
                                ProductsAndServicesModel.PROP_TOTAL),
                        MathContext.DECIMAL64);
                totalAmount = totalAmount.add(amount);
            }
        }
        return totalAmount;
    }

    /**
     * Update total amount in document
     * @param nodeRef - nodeRef document
     * @param type - type of document
     * @param property - property amount in document, that you want to update
     */
    private void updateTotalAmountInDocument(NodeRef nodeRef, QName type, QName property) {
        List<AssociationRef> documents = nodeService.getSourceAssocs(nodeRef,
                ProductsAndServicesModel.ASSOC_CONTAINS_PRODUCTS_AND_SERVICES);

        QName qName = nodeService.getType(documents.get(0).getSourceRef());
        if (!documents.isEmpty() && qName.equals(type)) {
            for (AssociationRef document : documents) {
                BigDecimal totalAmount = getTotalAmount(document.getSourceRef());
                BigDecimal currentlyAmount = BigDecimal.ZERO;
                if (nodeService.getProperty(document.getSourceRef(), property) != null) {
                    currentlyAmount = new BigDecimal(
                            (double) nodeService.getProperty(document.getSourceRef(),
                                    property), MathContext.DECIMAL64);
                }
                if (!Objects.equals(totalAmount, currentlyAmount)) {
                    nodeService.setProperty(document.getSourceRef(), property, totalAmount);
                }
            }
            ActionService actionService = serviceRegistry.getActionService();
            Action actionGenerateContent = actionService.createAction(GenerateContentActionExecuter.NAME);
            actionService.executeAction(actionGenerateContent, nodeRef);
        }
    }
}
