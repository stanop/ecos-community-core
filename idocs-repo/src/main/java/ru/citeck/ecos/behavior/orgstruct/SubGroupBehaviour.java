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
package ru.citeck.ecos.behavior.orgstruct;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.repo.node.NodeServicePolicies;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.model.OrgStructModel;
import ru.citeck.ecos.orgstruct.OrgMetaService;

public class SubGroupBehaviour implements
        NodeServicePolicies.OnUpdatePropertiesPolicy,
        NodeServicePolicies.OnAddAspectPolicy,
        NodeServicePolicies.OnRemoveAspectPolicy {

    private static final Log logger = LogFactory.getLog(SubGroupBehaviour.class);

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private OrgMetaService orgMetaService;
    private PolicyComponent policyComponent;
    private QName className;
    private String groupType;
    private QName subGroupTypeProp;
    private int aspectRemovalDepth = -1;

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                className, new JavaBehaviour(this, "onUpdateProperties",
                        NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME,
                className, new JavaBehaviour(this, "onAddAspect",
                        NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnRemoveAspectPolicy.QNAME,
                className, new JavaBehaviour(this, "onRemoveAspect",
                        NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef,
                                   Map<QName, Serializable> before, Map<QName, Serializable> after) {
        Object oldType = before.get(subGroupTypeProp);
        Object newType = after.get(subGroupTypeProp);
        if (oldType == null && newType == null
                || oldType != null && newType != null && oldType.equals(newType)) {
            return;
        }

        process(nodeRef);
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
        if (aspectTypeQName.equals(className)) {
            process(nodeRef);
        }
    }

    @Override
    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
        if (aspectTypeQName.equals(className)) {
            process(nodeRef);
        }
    }

    private void process(NodeRef nodeRef) {
        if (!nodeService.exists(nodeRef)) {
            return;
        }

        QName oldAspect = (QName) nodeService.getProperty(nodeRef, OrgStructModel.PROP_CUSTOM_ASPECT);
        QName newAspect = null;

        // get new aspect
        String newType = (String) nodeService.getProperty(nodeRef, subGroupTypeProp);
        if (newType != null) {
            NodeRef subGroupType = orgMetaService.getSubType(groupType, newType);
            newAspect = (QName) nodeService.getProperty(subGroupType, OrgStructModel.PROP_CUSTOM_ASPECT);
        }

        // remove old aspect if it is not the same as new aspect
        if (oldAspect != null && !oldAspect.equals(newAspect)) {
            removeCustomAspect(nodeRef, oldAspect);
        }

        // add new aspect if it is not the same as old aspect
        if (newAspect != null && !newAspect.equals(oldAspect)) {
            addCustomAspect(nodeRef, newAspect);
        }
    }

    private void addCustomAspect(NodeRef nodeRef, QName aspect) {
        nodeService.addAspect(nodeRef, aspect, new TreeMap<>());
        nodeService.setProperty(nodeRef, OrgStructModel.PROP_CUSTOM_ASPECT, aspect);
    }

    private void removeCustomAspect(NodeRef nodeRef, QName aspect) {
        AspectDefinition aspectDef = dictionaryService.getAspect(aspect);
        if (aspectDef != null) {
            removeAspectRecursively(nodeRef, aspectDef, aspectRemovalDepth);
        } else {
            logger.warn("Trying to remove non-existent aspect: " + aspect);
            return;
        }
        nodeService.removeProperty(nodeRef, OrgStructModel.PROP_CUSTOM_ASPECT);
    }

    private void removeAspectRecursively(NodeRef nodeRef, AspectDefinition aspectDef, int depth) {
        nodeService.removeAspect(nodeRef, aspectDef.getName());
        if (depth != 0) {
            for (AspectDefinition defaultAspectDef : aspectDef.getDefaultAspects(true)) {
                removeAspectRecursively(nodeRef, defaultAspectDef, depth - 1);
            }
        }
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setOrgMetaService(OrgMetaService orgMetaService) {
        this.orgMetaService = orgMetaService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public void setSubGroupType(QName subGroupType) {
        this.subGroupTypeProp = subGroupType;
    }

    public void setAspectRemovalDepth(int aspectRemovalDepth) {
        this.aspectRemovalDepth = aspectRemovalDepth;
    }
}
