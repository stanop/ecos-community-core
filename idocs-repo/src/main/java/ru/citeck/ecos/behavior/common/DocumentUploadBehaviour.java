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

import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;

import ru.citeck.ecos.utils.RepoUtils;

import java.lang.String;
import java.util.HashSet;
import java.util.Set;

/**
    <code>
  	<bean id="DocumentUploadBehaviourForType" depends-on="aaaa.dictionaryBootstrap"
            class="ru.citeck.ecos.behavior.common.DocumentUploadBehaviour" init-method="init">
  		<property name="policyComponent" ref="policyComponent"/>
        <property name="nodeService" ref="nodeService"/>
        <property name="mimetypeService" ref="mimetypeService"/>
        <property name="namespace" value="http://www.citeck.ru/model/content/idocs/1.0" />
        <property name="type" value="doc" />
  	</bean>
 * </code>
 *
  * */

public class DocumentUploadBehaviour implements OnContentUpdatePolicy{

    private static final String ERROR = "error.document.update";

    // Dependencies
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private String namespace;
    private String type;
    private MimetypeService mimetypeService;
    private Set<String> allowedExtensions;

    public void init() {

        if(allowedExtensions == null) {
            allowedExtensions = new HashSet<>();
            allowedExtensions.add(".doc");
            allowedExtensions.add(".docx");
        }
        
        this.policyComponent.bindClassBehaviour(OnContentUpdatePolicy.QNAME,
                QName.createQName(namespace, type), new JavaBehaviour(this,
                        "onContentUpdate",
                        NotificationFrequency.TRANSACTION_COMMIT));

    }

    @Override
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        if (nodeService.exists(nodeRef)) {
            String extension = RepoUtils.getExtension(nodeRef, null, nodeService, mimetypeService);
            
            if (!allowedExtensions.contains(extension)){
                throw new AlfrescoRuntimeException(ERROR);
            }
        }
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    public void setType(String type) {
        this.type = type;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    public void setAllowedExtensions(Set<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }
}