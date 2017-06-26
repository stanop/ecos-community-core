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
package ru.citeck.ecos.workflow.mirror;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.TransactionUtils;
import ru.citeck.ecos.workflow.listeners.AbstractExecutionListener;

import java.util.List;

/**
 * @author Pavel Simonov
 * @since 2.12
 */
public class MirrorEndProcessListener extends AbstractExecutionListener {

    private static final Log logger = LogFactory.getLog(MirrorEndProcessListener.class);
    private NodeService nodeService;
    private WorkflowMirrorService service;

    @Autowired
    private TransactionUtils transactionUtils;

    @Override
    protected void notifyImpl(final DelegateExecution delegateExecution) {
        if(!(delegateExecution instanceof ExecutionEntity)) return;

        ExecutionEntity entity = (ExecutionEntity) delegateExecution;
        String deleteReason = entity.getDeleteReason();

        if(!entity.isEnded() && deleteReason != null
                && (deleteReason.equals("cancelled") || deleteReason.equals("deleted"))) {

            String workflowId = "activiti$" + entity.getProcessInstanceId();
            List<NodeRef> mirrors = service.getTaskMirrorsByWorkflowId(workflowId);
            for (NodeRef mirror : mirrors) {
                final NodeRef taskMirror = mirror;
                AuthenticationUtil.runAs(new RunAsWork<Void>() {
                    @Override
                    public Void doWork() throws Exception {

                        nodeService.deleteNode(taskMirror);
                        return null;
                    }
                }, AuthenticationUtil.getSystemUserName());
                if(logger.isDebugEnabled()) {
                    logger.debug(String.format("Mirror node removed after workflow was %s. nodeRef:%s", deleteReason, taskMirror));
                }
            }
        } else if(entity.getProcessInstance().isEnded()) {
            String workflowId = "activiti$" + entity.getProcessInstanceId();
            List<NodeRef> mirrors = service.getTaskMirrorsByWorkflowId(workflowId);
            for(NodeRef mirror : mirrors) {
                String taskId = (String) nodeService.getProperty(mirror, ContentModel.PROP_NAME);
                transactionUtils.doAfterBehaviours(new MirrorTaskWork(service, taskId));
            }
        }
    }

    @Override
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.service = (WorkflowMirrorService) serviceRegistry.getService(CiteckServices.WORKFLOW_MIRROR_SERVICE);
    }
}
