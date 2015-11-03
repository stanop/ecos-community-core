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
package ru.citeck.ecos.icase;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.utils.RepoUtils;

public class CaseSubcaseBehavior implements 
    NodeServicePolicies.OnCreateNodePolicy,
    CaseElementPolicies.OnCaseElementAddPolicy,
    CaseElementPolicies.OnCaseElementRemovePolicy
{
	private static final Log logger = LogFactory.getLog(CaseSubcaseBehavior.class);

	private PolicyComponent policyComponent;
    private NodeService nodeService;
	private CaseElementServiceImpl caseElementService;

    private int order = 50;
	
	public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ICaseModel.ASPECT_CASE, 
                new OrderedBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT, order));
        policyComponent.bindAssociationBehaviour(CaseElementPolicies.OnCaseElementAddPolicy.QNAME, ICaseModel.ASPECT_CASE, 
                new JavaBehaviour(this, "onCaseElementAdd", NotificationFrequency.EVERY_EVENT));
        policyComponent.bindAssociationBehaviour(CaseElementPolicies.OnCaseElementRemovePolicy.QNAME, ICaseModel.ASPECT_CASE, 
                new JavaBehaviour(this, "onCaseElementRemove", NotificationFrequency.EVERY_EVENT));
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
    public void setOrder(int order) {
        this.order = order;
    }
    
    public void setCaseElementService(CaseElementServiceImpl caseElementService) {
        this.caseElementService = caseElementService;
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        NodeRef caseRef = childAssocRef.getChildRef();
        if(!nodeService.exists(caseRef)) {
            return;
        }
        List<NodeRef> configs = CaseUtils.getConfigsByPropertyValue(caseRef, 
                ICaseModel.PROP_CREATE_SUBCASE, Boolean.TRUE, 
                nodeService, caseElementService);
        for(NodeRef config : configs) {
            List<NodeRef> elements = getElements(caseRef, config);
            for(NodeRef element : elements) {
                onCreate(caseRef, element, config);
            }
        }
    }
    
    @Override
    public void onCaseElementAdd(NodeRef caseRef, NodeRef element, NodeRef config) {
        if(!nodeService.exists(caseRef) || !nodeService.exists(element) || !nodeService.exists(config)) {
            return;
        }
        onCreate(caseRef, element, config);
    }

    @Override
    public void onCaseElementRemove(NodeRef caseRef, NodeRef element, NodeRef config) {
        if(!nodeService.exists(caseRef) || !nodeService.exists(element) || !nodeService.exists(config)) {
            return;
        }
        onDelete(caseRef, element, config);
    }


    private void onCreate(NodeRef caseRef, NodeRef elementRef, NodeRef config) {
        // check if subcase already exists:
        NodeRef subcase = getSubcase(caseRef, elementRef, config);
        if(subcase != null) return;
        
        // perform actual creation of subcase
        QName subcaseType = (QName) nodeService.getProperty(config, ICaseModel.PROP_SUBCASE_TYPE);
        QName subcaseAssoc = (QName) nodeService.getProperty(config, ICaseModel.PROP_SUBCASE_ASSOC);
        if(subcaseType == null) subcaseType = ContentModel.TYPE_FOLDER;
        if(subcaseAssoc == null) subcaseAssoc = ContentModel.ASSOC_CONTAINS;
        
        subcase = nodeService.createNode(caseRef, 
            ContentModel.ASSOC_CONTAINS, 
            QName.createQName(ICaseModel.NAMESPACE, elementRef.getId()), 
            subcaseType).getChildRef();
        nodeService.createAssociation(subcase, elementRef, ICaseModel.ASSOC_SUBCASE_ELEMENT);
        nodeService.createAssociation(subcase, config, ICaseModel.ASSOC_SUBCASE_ELEMENT_CONFIG);
    }
    
    private void onDelete(NodeRef caseRef, NodeRef element, NodeRef config) {
        Boolean removeSubcase = (Boolean) nodeService.getProperty(config, ICaseModel.PROP_REMOVE_SUBCASE);
        Boolean removeEmptySubcase = (Boolean) nodeService.getProperty(config, ICaseModel.PROP_REMOVE_EMPTY_SUBCASE);
        if(Boolean.TRUE != removeSubcase && Boolean.TRUE != removeEmptySubcase)
            return;
        
        NodeRef subcase = getSubcase(caseRef, element, config);
        if(subcase == null)
            return;
        
        if(Boolean.TRUE != removeSubcase && !isEmptyCase(subcase))
            return;
        
        // perform actual removal of subcase
        nodeService.deleteNode(subcase);
    }
    
    private boolean isEmptyCase(NodeRef caseNodeRef) {
        List<NodeRef> configs = CaseUtils.getConfigs(caseNodeRef, caseElementService);
        for(NodeRef config : configs) {
            List<NodeRef> elements = getElements(caseNodeRef, config);
            if(elements.size() > 0) {
                return false;
            }
        }
        return true;
    }
    
    private NodeRef getSubcase(NodeRef caseRef, NodeRef elementRef, NodeRef config) {
        List<AssociationRef> subcaseAssocs = nodeService.getSourceAssocs(elementRef, ICaseModel.ASSOC_SUBCASE_ELEMENT);
        for(AssociationRef assoc : subcaseAssocs) {
            NodeRef subcase = assoc.getSourceRef();
            
            NodeRef subcaseParent = RepoUtils.getPrimaryParentRef(subcase, nodeService);
            if(!subcaseParent.equals(caseRef)) continue;
            
            // the same node can be an element in several configurations
            // if one configuration is deleted, only those subcase should be deleted
            List<AssociationRef> subcaseConfigs = nodeService.getTargetAssocs(subcase, ICaseModel.ASSOC_SUBCASE_ELEMENT_CONFIG);
            for(AssociationRef subcaseConfig : subcaseConfigs) {
                if(subcaseConfig.getTargetRef().equals(config)) {
                    return subcase;
                } 
            }
        }
        return null;
    }

    private List<NodeRef> getElements(NodeRef caseRef, NodeRef config) {
        CaseElementDAO strategy = CaseUtils.getStrategy(config, caseElementService);
        return strategy.get(caseRef, config);
    }

}
