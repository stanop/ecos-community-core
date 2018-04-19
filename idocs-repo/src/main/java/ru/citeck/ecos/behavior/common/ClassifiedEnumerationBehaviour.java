package ru.citeck.ecos.behavior.common;

/*
 * Copyright (C) 2008-2015 Citeck LLC.
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
 *
 * @author Maxim Strizhov 21.03.17.
 */

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.counter.EnumerationException;
import ru.citeck.ecos.counter.EnumerationService;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.node.NodeInfoFactory;
import ru.citeck.ecos.service.CiteckServices;

public class ClassifiedEnumerationBehaviour implements NodeServicePolicies.OnCreateNodePolicy {
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private EnumerationService enumerationService;
    private NodeInfoFactory nodeInfoFactory;

    private QName className;
    private QName numberField;
    private NodeRef type;
    private NodeRef kind;
    private QName enumerationStateField;
    private Object enabledState;
    private String templateName;
    private int order = 76;

    public void init() {

        policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME,
                className, new OrderedBehaviour(this, "onCreateNode",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order));
        if(enumerationStateField == null) {
            enumerationStateField = numberField;
        }
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        NodeRef nodeRef = childAssocRef.getChildRef();
        if (!nodeService.exists(nodeRef)) {
            return;
        }
        // check node for it's type and kind
        Object docTypeRef = nodeService.getProperty(nodeRef, ClassificationModel.PROP_DOCUMENT_TYPE);
        Object docKindRef = nodeService.getProperty(nodeRef, ClassificationModel.PROP_DOCUMENT_KIND);
        if ((this.type != null && docTypeRef == null) || (this.type != null && !this.type.equals(docTypeRef))) {
            return;
        }
        if ((this.kind != null && docKindRef == null) || (this.kind != null && !this.kind.equals(docKindRef))) {
            return;
        }
        // check if enumeration is enabled
        Object enumerationState = nodeService.getProperty(nodeRef, enumerationStateField);
        if ((enabledState != null && !enabledState.equals(enumerationState)) ||
                (enabledState == null && enumerationState != null)) {
            return;
        }
        NodeRef template = enumerationService.getTemplate(templateName);
        NodeInfo nodeInfo = nodeInfoFactory.createNodeInfo(nodeRef);

        String number;
        try {
            number = enumerationService.getNumber(template, nodeInfo);
        } catch (EnumerationException e) {
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        }

        nodeService.setProperty(nodeRef, numberField, number);
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.enumerationService = (EnumerationService) serviceRegistry.getService(CiteckServices.ENUMERATION_SERVICE);
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeInfoFactory(NodeInfoFactory nodeInfoFactory) {
        this.nodeInfoFactory = nodeInfoFactory;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    public void setNumberField(QName numberField) {
        this.numberField = numberField;
    }

    public void setEnumerationStateField(QName enumerationStateField) {
        this.enumerationStateField = enumerationStateField;
    }

    public void setEnabledState(Object enabledState) {
        this.enabledState = enabledState;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setType(NodeRef type) {
        this.type = type;
    }

    public void setKind(NodeRef kind) {
        this.kind = kind;
    }
}
