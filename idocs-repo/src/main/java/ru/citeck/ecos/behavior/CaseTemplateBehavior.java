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
package ru.citeck.ecos.behavior;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;
import ru.citeck.ecos.cmmn.service.CaseXmlService;
import ru.citeck.ecos.event.EventService;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.model.ICaseEventModel;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.state.ItemsUpdateState;
import ru.citeck.ecos.utils.TransactionUtils;

import java.util.HashSet;
import java.util.Set;

public class CaseTemplateBehavior implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnAddAspectPolicy {

    private static final String KEY_FILLED_CASE_NODES = "filled-case-nodes";
    private static final String STATUS_PROCESS_START_ERROR = "ecos-process-start-error";

    private static final Log logger = LogFactory.getLog(CaseTemplateBehavior.class);

    protected PolicyComponent policyComponent;
    protected NodeService nodeService;
    protected NamespaceService namespaceService;
    private CaseXmlService caseXmlService;

    private RepositoryState repositoryState;
    private EventService eventService;
    private ItemsUpdateState itemsUpdateState;
    private CaseStatusService caseStatusService;

    private int order = 40;

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ICaseModel.ASPECT_CASE,
                new OrderedBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT, order));
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, ICaseModel.ASPECT_CASE,
                new OrderedBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT, order));
    }

    @Override
    public void onAddAspect(NodeRef caseNode, QName aspectTypeQName) {
        if (nodeService.exists(caseNode) &&
                ICaseModel.ASPECT_CASE.equals(aspectTypeQName)) {
            copyFromTemplate(caseNode);
        }
    }


    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        NodeRef caseNode = childAssocRef.getChildRef();
        copyFromTemplate(caseNode);
    }

    private void copyFromTemplate(final NodeRef caseNode) {
        if (repositoryState.isBootstrapping()
                || !isAllowedCaseNode(caseNode)
                || !getFilledCaseNodes().add(caseNode)) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Applying template to node. nodeRef=" + caseNode);
        }

        Runnable job = () -> {
            itemsUpdateState.startUpdate(CaseTemplateBehavior.class, caseNode);
            itemsUpdateState.endUpdate(CaseTemplateBehavior.class, caseNode, true, false);
            copyFromTemplateImpl(caseNode);
        };
        Runnable errorHandler = () -> {
            itemsUpdateState.endUpdate(CaseTemplateBehavior.class, caseNode, true, true);
            caseStatusService.setStatus(caseNode, STATUS_PROCESS_START_ERROR);
        };

        TransactionUtils.doAfterCommit(job, errorHandler);
    }

    private void copyFromTemplateImpl(final NodeRef caseNode) {

        StopWatch stopWatch = new StopWatch(CaseTemplateBehavior.class.getName());

        stopWatch.start("copyFromTemplate caseRef: " + caseNode);
        caseXmlService.fillCaseFromTemplate(caseNode);
        stopWatch.stop();

        stopWatch.start("fire '" + ICaseEventModel.CONSTR_CASE_CREATED + "' event. caseRef: " + caseNode);
        eventService.fireEvent(caseNode, ICaseEventModel.CONSTR_CASE_CREATED);
        stopWatch.stop();

        logger.info(stopWatch.prettyPrint());
    }

    private boolean isAllowedCaseNode(NodeRef caseNode) {
        return caseNode != null && nodeService.exists(caseNode)
                && !nodeService.hasAspect(caseNode, ContentModel.ASPECT_COPIEDFROM)
                && !nodeService.hasAspect(caseNode, ICaseModel.ASPECT_COPIED_FROM_TEMPLATE)
                && !nodeService.hasAspect(caseNode, ICaseModel.ASPECT_CASE_TEMPLATE);
    }

    private Set<NodeRef> getFilledCaseNodes() {
        Set<NodeRef> filledCaseNodes = AlfrescoTransactionSupport.getResource(KEY_FILLED_CASE_NODES);
        if (filledCaseNodes == null) {
            AlfrescoTransactionSupport.bindResource(KEY_FILLED_CASE_NODES, filledCaseNodes = new HashSet<>());
        }
        return filledCaseNodes;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setCaseXmlService(CaseXmlService caseXmlService) {
        this.caseXmlService = caseXmlService;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setRepositoryState(RepositoryState repositoryState) {
        this.repositoryState = repositoryState;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setItemsUpdateState(ItemsUpdateState itemsUpdateState) {
        this.itemsUpdateState = itemsUpdateState;
    }

    public void setCaseStatusService(CaseStatusService caseStatusService) {
        this.caseStatusService = caseStatusService;
    }
}
