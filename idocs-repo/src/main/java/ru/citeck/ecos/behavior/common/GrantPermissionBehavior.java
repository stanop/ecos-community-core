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

import org.alfresco.repo.node.NodeServicePolicies.OnCreateAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteAssociationPolicy;
import org.alfresco.repo.policy.Behaviour;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.security.GrantPermissionService;
import ru.citeck.ecos.utils.RepoUtils;

/**
 * Behaviour grants (revokes) permission to (from) document for authority who associated by given association type
 *
 * @author Pavel Simonov
 * @since 2.11.1
 */
public class GrantPermissionBehavior implements OnCreateAssociationPolicy, OnDeleteAssociationPolicy {

    private String provider;

    private GrantPermissionService grantPermissionService;
    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    private QName associationType;
    private QName className;
    private String permission;

    public void init() {
        bind(OnCreateAssociationPolicy.QNAME, "onCreateAssociation");
        bind(OnDeleteAssociationPolicy.QNAME, "onDeleteAssociation");
        if(provider == null) {
            provider = String.format("%s-%s-%s",
                                     getClass().getSimpleName(),
                                     associationType.getLocalName(),
                                     permission);
        }
    }

    private void bind(QName policy, String method) {
        policyComponent.bindAssociationBehaviour(policy, className, associationType,
                new JavaBehaviour(this, method, Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onCreateAssociation(AssociationRef associationRef) {
        final NodeRef document = associationRef.getSourceRef();
        final NodeRef authority = associationRef.getTargetRef();
        if (!nodeService.exists(document) || !nodeService.exists(authority)) return;

        final String authorityName = RepoUtils.getAuthorityName(authority, nodeService, dictionaryService);
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {
                grantPermissionService.grantPermission(document, authorityName, permission, provider);
                return null;
            }
        });
    }

    @Override
    public void onDeleteAssociation(AssociationRef associationRef) {
        final NodeRef document = associationRef.getSourceRef();
        final NodeRef authority = associationRef.getTargetRef();
        if(!nodeService.exists(document) || !nodeService.exists(authority)) return;

        final String authorityName = RepoUtils.getAuthorityName(authority, nodeService, dictionaryService);
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {
                grantPermissionService.revokePermission(document, authorityName, permission, provider);
                return null;
            }
        });
    }

    public void setGrantPermissionService(GrantPermissionService grantPermissionService) {
        this.grantPermissionService = grantPermissionService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    public void setAssociationType(QName associationType) {
        this.associationType = associationType;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
