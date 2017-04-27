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
 */
package ru.citeck.ecos.behavior.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponentImpl;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class FullNameBehaviour implements OnCreateNodePolicy, OnUpdatePropertiesPolicy {
    private static Log logger = LogFactory.getLog(FullNameBehaviour.class);

    private PolicyComponentImpl policyComponent;
    private NodeService nodeService;
    @SuppressWarnings("unused")
	private NamespaceService namespaceService;

    private QName fullNameProperty;
    private QName firstNameProperty;
    private QName lastNameProperty;
    private QName middleNameProperty;
    private boolean updateAlways = false;

    private QName PROP_CM_MIDDLE_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "middleName");

    private String separator;

    public void init() {
        policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        NodeRef nodeRef = childAssocRef.getChildRef();
        if(!nodeService.exists(nodeRef)) {
            return;
        }
        
        String fullName = getStringProperty(nodeService.getProperty(nodeRef, fullNameProperty));
        String firstName = getStringProperty(nodeService.getProperty(nodeRef, firstNameProperty));
        String lastName = getStringProperty(nodeService.getProperty(nodeRef, lastNameProperty));
        
        if(fullName != null && firstName != null && lastName != null) {
            updateNames(nodeRef, fullName, firstName, lastName);
        }
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        String fullNameBefore = getStringProperty(before.get(fullNameProperty)),
               fullName = getStringProperty(after.get(fullNameProperty)),
               firstNameBefore = getStringProperty(before.get(firstNameProperty)),
               firstName = getStringProperty(after.get(firstNameProperty)),
               lastNameBefore = getStringProperty(before.get(lastNameProperty)),
               lastName = getStringProperty(after.get(lastNameProperty));

        if (logger.isDebugEnabled()) {
            String TO = " -> ";
            logger.debug("Before updating person. nodeRef: " + nodeRef +
                    "; fullName: " + fullNameBefore + TO + fullName +
                    "; firstName: " + firstNameBefore + TO + firstName +
                    "; lastName: " + lastNameBefore + TO + lastName);
        }
        
        if (fullName == null 
                || firstName == null 
                || lastName == null
                || !updateAlways
                && fullName.equals(fullNameBefore)
                && firstName.equals(firstNameBefore)
                && lastName.equals(lastNameBefore)
                || !nodeService.exists(nodeRef)) {
            return;
        }
        
        updateNames(nodeRef, fullName, firstName, lastName);
    }

    private void updateNames(NodeRef nodeRef, String fullName,
            String firstName, String lastName) {
        String middleName = getMiddleName(fullName, firstName, lastName);

        nodeService.setProperty(nodeRef, ContentModel.PROP_FIRSTNAME, firstName + " " + middleName);
        nodeService.setProperty(nodeRef, PROP_CM_MIDDLE_NAME, "");
        nodeService.setProperty(nodeRef, ContentModel.PROP_LASTNAME, lastName);
        nodeService.setProperty(nodeRef, middleNameProperty, middleName);

        if (logger.isDebugEnabled()) {
            logger.debug("Updated person. nodeRef: " + nodeRef +
                    "; firstName: " + firstName +
                    "; middleName: " + middleName +
                    "; lastName: " + lastName);
        }
    }

    private String getStringProperty(Serializable property) {
        String propertyString = null;
        if (property instanceof MLText) {
            propertyString = ((MLText) property).getDefaultValue();
        }
        if (property instanceof String) {
            propertyString = (String) property;
        }
        return propertyString;
    }

    private String getMiddleName(String fio, String firstName, String lastName) {
        List<String> fioParts = Arrays.asList(fio.split("\\" + separator));
        if (fioParts.contains(firstName) && fioParts.contains(lastName)) {
            for (String part : fioParts) {
                if (!part.equals(firstName) && !part.equals(lastName)) {
                    return part;
                }
            }
        }
        return "";
    }

    public void setPolicyComponent(PolicyComponentImpl policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setFullNameProperty(QName fullNameProperty) {
        this.fullNameProperty = fullNameProperty;
    }

    public void setFirstNameProperty(QName firstNameProperty) {
        this.firstNameProperty = firstNameProperty;
    }

    public void setLastNameProperty(QName lastNameProperty) {
        this.lastNameProperty = lastNameProperty;
    }

    public void setMiddleNameProperty(QName middleNameProperty) {
        this.middleNameProperty = middleNameProperty;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void setUpdateAlways(boolean updateAlways) {
        this.updateAlways = updateAlways;
    }

}
