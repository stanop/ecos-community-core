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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.solr.AlfrescoModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import ru.citeck.ecos.orgstruct.OrgStructService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;

import java.util.List;
import java.util.Set;
import java.io.Serializable;

/**
 * @author Maxim Strizhov <maxim.strizhov@citeck.ru>
 */
public class BranchAssignBehavior implements OnCreateNodePolicy {
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private OrgStructService orgStructService;
    private AuthorityService authorityService;
    private DictionaryService dictionaryService;
    private QName targetAssociationOrProperty;
    private QName authorityContainerProperty;
    private String groupType;
    private String groupSubType;
    private QName className;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setOrgStructService(OrgStructService orgStructService) {
        this.orgStructService = orgStructService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setTargetAssociationOrProperty(QName targetAssociationOrProperty) {
        this.targetAssociationOrProperty = targetAssociationOrProperty;
    }

    public void setAuthorityContainerProperty(QName authorityContainerProperty) {
        this.authorityContainerProperty = authorityContainerProperty;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public void setGroupSubType(String groupSubType) {
        this.groupSubType = groupSubType;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }
    
    public void setClassName(QName className) {
        this.className = className;
    }

    public void init() throws Exception {
        ParameterCheck.mandatory("policyComponent", policyComponent);
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                className,
                new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        final NodeRef nodeRef = childAssocRef.getChildRef();
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                if (nodeService.exists(nodeRef)) {
                    String creator = nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR) != null ? (String) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR) : "";
                    List<String> branches = orgStructService.getTypedGroupsForUser(creator, groupType, groupSubType);
                    AssociationDefinition targetDefinition = dictionaryService.getAssociation(targetAssociationOrProperty);
                    
                    for (String groupName : branches) {
                        NodeRef groupRef = authorityService.getAuthorityNodeRef(groupName);
//                        Set<QName> aspects = nodeService.getAspects(groupRef);
                        if (targetDefinition == null)
                        {
                            PropertyDefinition propertyDefinition = dictionaryService.getProperty(targetAssociationOrProperty);
                            if (propertyDefinition != null)
                            {
                                Serializable propValue = null;
                                if(authorityContainerProperty!=null)
                                {
                                    propValue = nodeService.getProperty(groupRef, authorityContainerProperty);
                                }
                                else
                                {
                                    propValue = nodeService.getProperty(groupRef, ContentModel.PROP_NODE_REF);
                                }
                                nodeService.setProperty(nodeRef, targetAssociationOrProperty, propValue);
                            }
                        }
                        else
                            nodeService.createAssociation(nodeRef, groupRef, targetAssociationOrProperty);
                    }
                }
                return null;
            }
        });
    }
}
