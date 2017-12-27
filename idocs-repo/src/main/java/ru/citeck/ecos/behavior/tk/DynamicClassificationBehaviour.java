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
package ru.citeck.ecos.behavior.tk;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
//import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;

import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.utils.RepoUtils;

public class DynamicClassificationBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy
{

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private int order = 50;

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ClassificationModel.ASPECT_DOCUMENT_TYPE_KIND,
//                new OrderedBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT, order));
                new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

        if(!nodeService.exists(nodeRef)) return;

        NodeRef typeAfter = (NodeRef) after.get(ClassificationModel.PROP_DOCUMENT_TYPE);
        NodeRef kindAfter = (NodeRef) after.get(ClassificationModel.PROP_DOCUMENT_KIND);

        // update type

        QName typeToApply = null;
        if(kindAfter != null) {
            typeToApply = RepoUtils.getProperty(kindAfter, ClassificationModel.PROP_APPLIED_TYPE, nodeService);
        }
        if(typeToApply == null && typeAfter != null) {
            typeToApply = RepoUtils.getProperty(typeAfter, ClassificationModel.PROP_APPLIED_TYPE, nodeService);
        }

        if (typeToApply != null) {
            QName appliedType = nodeService.getType(nodeRef);
            if (!typeToApply.equals(appliedType) && dictionaryService.isSubClass(typeToApply, appliedType)) {
                nodeService.setType(nodeRef, typeToApply);
            }
        }

        // update aspects

        Set<QName> aspectsToApply = new HashSet<>();
        if(kindAfter != null) {
            List<QName> kindAspects = RepoUtils.getProperty(kindAfter, ClassificationModel.PROP_APPLIED_ASPECTS, nodeService);
            if (kindAspects != null) {
                aspectsToApply.addAll(kindAspects);
            }
        }
        if(typeAfter != null) {
            List<QName> typeAspects = RepoUtils.getProperty(typeAfter, ClassificationModel.PROP_APPLIED_ASPECTS, nodeService);
            if (typeAspects != null) {
                aspectsToApply.addAll(typeAspects);
            }
        }

        if(!aspectsToApply.isEmpty()) {
            Set<QName> appliedAspects = nodeService.getAspects(nodeRef);

            @SuppressWarnings("unchecked")
            Collection<QName> newAspects = CollectionUtils.subtract(aspectsToApply, appliedAspects);

            for(QName aspect : newAspects) {
                nodeService.addAspect(nodeRef, aspect, null);
            }
        }

    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
