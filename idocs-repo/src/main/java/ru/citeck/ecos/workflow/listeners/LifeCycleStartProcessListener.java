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
package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import ru.citeck.ecos.lifecycle.LifeCycleService;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.workflow.utils.ActivitiVariableScopeMap;

/**
 * @author: Alexander Nemerov
 * @date: 28.03.2014
 */
public class LifeCycleStartProcessListener extends AbstractExecutionListener {

    private LifeCycleService lifeCycleService;
    private NodeService nodeService;

    @Override
    protected void notifyImpl(DelegateExecution delegateExecution) throws Exception {
        NodeRef docRef = ListenerUtils.getDocument(delegateExecution, nodeService);
        if(docRef == null) {return;}
        String definitionId = "activiti$" + Context.getExecutionContext().getProcessDefinition().getKey();
        lifeCycleService.doTransitionOnStartProcess(docRef, definitionId, 
                new ActivitiVariableScopeMap(delegateExecution, serviceRegistry));
    }

    @Override
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.lifeCycleService = (LifeCycleService) serviceRegistry.getService(CiteckServices.LIFECYCLE_SERVICE);
    }

}
