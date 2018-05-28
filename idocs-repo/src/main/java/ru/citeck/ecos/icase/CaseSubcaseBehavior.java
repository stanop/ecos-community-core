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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.icase.element.CaseElementDAO;
import ru.citeck.ecos.icase.element.CaseElementPolicies;
import ru.citeck.ecos.icase.element.CaseElementServiceImpl;
import ru.citeck.ecos.icase.element.config.ElementConfigDto;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.utils.RepoUtils;

public class CaseSubcaseBehavior implements
        NodeServicePolicies.OnCreateNodePolicy,
        CaseElementPolicies.OnCaseElementAddPolicy,
        CaseElementPolicies.OnCaseElementRemovePolicy {
    private static final Log logger = LogFactory.getLog(CaseSubcaseBehavior.class);

    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private CaseElementServiceImpl caseElementService;

    private int order = 50;

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ICaseModel.ASPECT_CASE,
//                new OrderedBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT, order));
                new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(CaseElementPolicies.OnCaseElementAddPolicy.QNAME, ICaseModel.ASPECT_CASE,
                new JavaBehaviour(this, "onCaseElementAdd", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(CaseElementPolicies.OnCaseElementRemovePolicy.QNAME, ICaseModel.ASPECT_CASE,
                new JavaBehaviour(this, "onCaseElementRemove", NotificationFrequency.EVERY_EVENT));
    }



    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        NodeRef caseRef = childAssocRef.getChildRef();
        if (!nodeService.exists(caseRef)) {
            return;
        }
        List<NodeRef> configs = CaseUtils.getConfigsByPropertyValue(caseRef,
                ICaseModel.PROP_CREATE_SUBCASE, Boolean.TRUE,
                nodeService, caseElementService);
        for (NodeRef config : configs) {
            List<NodeRef> elements = getElements(caseRef, config);
            Optional<ElementConfigDto> configDto = caseElementService.getConfig(config);
            if (configDto.isPresent()) {
                for (NodeRef element : elements) {
                    onCreate(caseRef, element, configDto.get());
                }
            }
        }
    }

    @Override
    public void onCaseElementAdd(NodeRef caseRef, NodeRef element, ElementConfigDto config) {
        if (config == null || !nodeService.exists(caseRef) || !nodeService.exists(element)) {
            return;
        }
        onCreate(caseRef, element, config);
    }

    @Override
    public void onCaseElementRemove(NodeRef caseRef, NodeRef element, ElementConfigDto config) {
        if (config == null || !nodeService.exists(caseRef) || !nodeService.exists(element)) {
            return;
        }
        onDelete(caseRef, element, config);
    }


    private void onCreate(NodeRef caseRef, NodeRef elementRef, ElementConfigDto config) {
        // check if subcase already exists:
        NodeRef subcase = getSubcase(caseRef, elementRef, config);
        if (subcase != null) {
            return;
        }

        // perform actual creation of subcase
        boolean createSubcase = config.isCreateSubcase();
        if (!createSubcase) {
            return;
        }
        QName subcaseType = config.getSubcaseType();
        QName subcaseAssoc = config.getSubcaseAssoc();
        if (subcaseType == null) {
            subcaseType = ContentModel.TYPE_FOLDER;
        }
        if (subcaseAssoc == null) {
            subcaseAssoc = ContentModel.ASSOC_CONTAINS;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Trying to create subcase for case " + caseRef + " and " +
                         "element " + elementRef + " (type " + subcaseType + "; assoc " + subcaseAssoc + ")");
        }

        subcase = nodeService.createNode(caseRef,
                subcaseAssoc,
                QName.createQName(ICaseModel.NAMESPACE, elementRef.getId()),
                subcaseType).getChildRef();
        nodeService.createAssociation(subcase, elementRef, ICaseModel.ASSOC_SUBCASE_ELEMENT);
        nodeService.createAssociation(subcase, caseRef, ICaseModel.ASSOC_PARENT_CASE);
        nodeService.createAssociation(subcase, config.getNodeRef(), ICaseModel.ASSOC_SUBCASE_ELEMENT_CONFIG);
    }

    private void onDelete(NodeRef caseRef, NodeRef element, ElementConfigDto config) {

        boolean removeSubcase = config.isRemoveSubcase();
        boolean removeEmptySubcase = config.isRemoveEmptySubcase();

        if (!removeSubcase && !removeEmptySubcase) {
            return;
        }

        final NodeRef subcase = getSubcase(caseRef, element, config);
        if (subcase == null)
            return;

        if (Boolean.TRUE != removeSubcase && !isEmptyCase(subcase))
            return;

        if (logger.isDebugEnabled()) {
            logger.debug("Deferring subcase removal for case " + caseRef + " and element " + element + " (subcase " + subcase + ")");
        }

        // perform actual removal of subcase
        // we should remove it in the end of transaction,
        // because direct execution will cause concurrency conflict
        // (both deletes would try to remove icase:subcaseElement association)
        AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
            @Override
            public void beforeCommit(boolean readOnly) {
                if (nodeService.exists(subcase)) {
                    nodeService.deleteNode(subcase);
                    logger.debug("Subcase " + subcase + " was removed");
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Subcase " + subcase + " does not exist");
                }
            }
        });

    }

    private boolean isEmptyCase(NodeRef caseNodeRef) {
        Optional<ElementConfigDto> elementTypesConfigOpt = caseElementService.getConfig(CaseConstants.ELEMENT_TYPES);
        if (!elementTypesConfigOpt.isPresent()) {
            return true;
        }
        ElementConfigDto elementTypesConfig = elementTypesConfigOpt.get();
        CaseElementDAO<ElementConfigDto> elementTypesDAO = CaseUtils.getStrategy(elementTypesConfig,
                                                                                 caseElementService);
        List<NodeRef> configs = elementTypesDAO.get(caseNodeRef, elementTypesConfig);
        for (NodeRef config : configs) {
            if (elementTypesConfig.getNodeRef().equals(config)) {
                continue;
            }
            List<NodeRef> elements = getElements(caseNodeRef, config);
            if (elements.size() > 0) {
                return false;
            }
        }
        return true;
    }

    private NodeRef getSubcase(NodeRef caseRef, NodeRef elementRef, ElementConfigDto config) {
        List<AssociationRef> subcaseAssocs = nodeService.getSourceAssocs(elementRef, ICaseModel.ASSOC_SUBCASE_ELEMENT);
        for (AssociationRef assoc : subcaseAssocs) {
            NodeRef subcase = assoc.getSourceRef();

            NodeRef subcaseParent = RepoUtils.getPrimaryParentRef(subcase, nodeService);
            if (!subcaseParent.equals(caseRef)) continue;

            // the same node can be an element in several configurations
            // if one configuration is deleted, only those subcase should be deleted
            List<AssociationRef> subcaseConfigs = nodeService.getTargetAssocs(subcase, ICaseModel.ASSOC_SUBCASE_ELEMENT_CONFIG);
            for (AssociationRef subcaseConfig : subcaseConfigs) {
                if (subcaseConfig.getTargetRef().equals(config.getNodeRef())) {
                    return subcase;
                }
            }
        }
        return null;
    }

    private List<NodeRef> getElements(NodeRef caseRef, NodeRef config) {
        Optional<ElementConfigDto> configDto = caseElementService.getConfig(config);
        if (configDto.isPresent()) {
            CaseElementDAO<ElementConfigDto> strategy = CaseUtils.getStrategy(configDto.get(), caseElementService);
            return strategy.get(caseRef, configDto.get());
        }
        return Collections.emptyList();
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
}
