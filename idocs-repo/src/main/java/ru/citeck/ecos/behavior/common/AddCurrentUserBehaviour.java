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
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.model.ContentModel;

/**
 * This behavior helps to add reference of the current user into
 * the specified association.
 * Using (all specified properties are mandatory):
 * <code>
 * 	<bean id="aaaa.AddCurrentUserToAttorneyFormBehaviour" depends-on="aaaa.dictionaryBootstrap"
 * 			class="ru.citeck.ecos.behavior.common.AddCurrentUserBehaviour" init-method="init">
 * 		<property name="policyComponent" ref="policyComponent"/>
 * 		<property name="serviceRegistry" ref="ServiceRegistry"/>
 * 		<property name="className" value="target:document_type" />
 * 		<property name="targetAssociationOrProperty" value="target:association_type/property" />
 * 	</bean>
 * </code>
 * @author Ruslan
 *
 */
public class AddCurrentUserBehaviour {

	protected EveryUpdating everyUpdating = null;
	protected OnceUpdating onceUpdating = null;

	protected PolicyComponent policyComponent;
	protected NodeService nodeService;
	protected AuthenticationService authenticationService;
	protected PersonService personService;
	protected DictionaryService dictionaryService;

	/**
	 * Target document (node) type
	 */
	protected QName className;
	/**
	 * Target association reference in the document (node) of the added user.
	 */
	protected QName targetAssociationOrProperty;
	protected QName personProperty;
	/**
	 * If it is {@code true}, it updates only when creating the node.
	 * By default it is {@code false}.
	 */
	protected boolean whenCreating = false;

	public void init() throws Exception {
		if (whenCreating) {
			onceUpdating = new OnceUpdating();
			onceUpdating.init();
		}
		else {
			everyUpdating = new EveryUpdating();
			everyUpdating.init();
		}
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.nodeService = serviceRegistry.getNodeService();
		this.authenticationService = serviceRegistry.getAuthenticationService();
		this.personService = serviceRegistry.getPersonService();
		this.dictionaryService = serviceRegistry.getDictionaryService();
	}

	/**
	 * It sets new target document (node) type
	 * @param className - new type
	 */
	public void setClassName(QName className) {
		this.className = className;
	}

	/**
	 * It sets new target association reference in the document (node) of
	 * added user.
	 * @param targetAssociationOrProperty - new association type
	 */
	public void setTargetAssociationOrProperty(QName targetAssociationOrProperty) {
		this.targetAssociationOrProperty = targetAssociationOrProperty;
	}
    
	/**
	 * It is property from cm:person which will be set to targetAssociationOrProperty property
	 * @param personProperty - property from cm:person
	 */
	public void setPersonProperty(QName personProperty) {
		this.personProperty = personProperty;
	}

	/**
	 * It sets new value. By default it is {@code false}.
	 * @param whenCreating - if it is {@code true}, current behavior updates
	 * only when creating the node.
	 */
	public void setWhenCreating(boolean whenCreating) {
		this.whenCreating = whenCreating;
	}

	protected void update(NodeRef nodeRef) {
		if (nodeRef != null && nodeService.exists(nodeRef)) {
			String currentUserName = authenticationService.getCurrentUserName();
			NodeRef userNodeRef = personService.getPerson(currentUserName);
			AssociationDefinition targetDefinition = dictionaryService.getAssociation(targetAssociationOrProperty);
			if (targetDefinition == null)
			{
				PropertyDefinition propertyDefinition = dictionaryService.getProperty(targetAssociationOrProperty);
				if (propertyDefinition != null)
                {
                    Serializable propValue = null;
                    if(personProperty!=null)
                    {
                        propValue = nodeService.getProperty(userNodeRef, personProperty);
                    }
                    else
                    {
                        propValue = nodeService.getProperty(userNodeRef, ContentModel.PROP_NODE_REF);
                    }
                    nodeService.setProperty(nodeRef, targetAssociationOrProperty, propValue);
                }
			}
			else
				nodeService.createAssociation(nodeRef, userNodeRef, targetAssociationOrProperty);
		}
	}

	public class EveryUpdating implements NodeServicePolicies.OnUpdatePropertiesPolicy {
		public void init() {
			policyComponent.bindClassBehaviour(
			NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
			className, 
			new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
		}

		@Override
		public void onUpdateProperties(NodeRef nodeRef,
				Map<QName, Serializable> before, Map<QName, Serializable> after) {
			update(nodeRef);
		}
	}

	public class OnceUpdating implements NodeServicePolicies.OnCreateNodePolicy {
		public void init() {
			policyComponent.bindClassBehaviour(
					NodeServicePolicies.OnCreateNodePolicy.QNAME,
					className, 
					new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
		}

		@Override
		public void onCreateNode(ChildAssociationRef childAssocRef) {
			NodeRef nodeRef = childAssocRef.getChildRef();
			update(nodeRef);
		}
	}

}
