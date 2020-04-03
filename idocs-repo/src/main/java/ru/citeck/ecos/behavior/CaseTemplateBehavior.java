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

import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import ru.citeck.ecos.cmmn.service.CaseXmlService;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseServiceType;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventService;
import ru.citeck.ecos.model.ICaseEventModel;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.service.AlfrescoServices;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.state.ItemsUpdateState;
import ru.citeck.ecos.utils.TransactionUtils;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Component
@DependsOn("idocs.dictionaryBootstrap")
public class CaseTemplateBehavior implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnAddAspectPolicy {

    private static final String KEY_FILLED_CASE_NODES = "filled-case-nodes";
    private static final String STATUS_PROCESS_START_ERROR = "ecos-process-start-error";

    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private CaseXmlService caseXmlService;

    private RepositoryState repositoryState;
    private ItemsUpdateState itemsUpdateState;
    private CaseStatusService caseStatusService;
    private CaseActivityEventService caseActivityEventService;

    private int order;

    @Autowired
    public CaseTemplateBehavior(ServiceRegistry serviceRegistry, @Value("${behavior.order.case.template:40}") int order) {
        this.policyComponent = serviceRegistry.getPolicyComponent();
        this.nodeService = serviceRegistry.getNodeService();
        this.caseXmlService = (CaseXmlService) serviceRegistry.getService(CiteckServices.CASE_XML_SERVICE);
        this.repositoryState = (RepositoryState) serviceRegistry.getService(AlfrescoServices.REPOSITORY_STATE);
        this.itemsUpdateState = (ItemsUpdateState) serviceRegistry.getService(CiteckServices.ITEMS_UPDATE_STATE);
        this.caseStatusService = (CaseStatusService) serviceRegistry.getService(CiteckServices.CASE_STATUS_SERVICE);
        this.caseActivityEventService = (CaseActivityEventService) serviceRegistry
            .getService(CiteckServices.CASE_ACTIVITY_EVENT_SERVICE);
        this.order = order;
    }

    @PostConstruct
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

        if (log.isDebugEnabled()) {
            log.debug("Applying template to node. nodeRef=" + caseNode);
        }

        Consumer<Exception> errorHandler = e -> {
            itemsUpdateState.endUpdate(CaseTemplateBehavior.class, caseNode, true, true);
            caseStatusService.setStatus(caseNode, STATUS_PROCESS_START_ERROR);
        };

        StopWatch stopWatch = new StopWatch(CaseTemplateBehavior.class.getName());

        TransactionUtils.doAfterCommit(() -> {

            itemsUpdateState.startUpdate(CaseTemplateBehavior.class, caseNode);

            if (!stopWatch.isRunning()) {
                stopWatch.start("copyFromTemplate caseRef: " + caseNode);
            }
            caseXmlService.fillCaseFromTemplate(caseNode);
            stopWatch.stop();

            TransactionUtils.doAfterCommit(() -> {

                itemsUpdateState.endUpdate(CaseTemplateBehavior.class, caseNode, true, false);

                if (!stopWatch.isRunning()) {
                    stopWatch.start("fire '" + ICaseEventModel.CONSTR_CASE_CREATED + "' event. caseRef: " + caseNode);
                }

                RecordRef caseRef = RecordRef.valueOf(caseNode.toString());
                ActivityRef activityRef = ActivityRef.of(CaseServiceType.ALFRESCO, caseRef, ActivityRef.ROOT_ID);
                caseActivityEventService.fireEvent(activityRef, ICaseEventModel.CONSTR_CASE_CREATED);
                stopWatch.stop();

                log.info(stopWatch.prettyPrint());

            }, errorHandler);

        }, errorHandler);
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
}
