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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;

/**
 * @author: Elena Zaripova
 * @date: 16.01.2015
 */
public class SetInitiatorStartProcessListener extends AbstractExecutionListener {

    private NodeService nodeService;
    private PersonService personService;

    @Override
    protected void notifyImpl(DelegateExecution delegateExecution) throws Exception {
		Object setInitiator = delegateExecution.getVariable("cwf_setInitiator");
		if (Boolean.TRUE.equals((Boolean)setInitiator))
		{
			NodeRef docRef = ListenerUtils.getDocument(delegateExecution, nodeService);
			if (docRef == null)
				return;
			String docCreatorUserName = (String)nodeService.getProperty(docRef, ContentModel.PROP_CREATOR);
			NodeRef creatorUser = personService.getPerson(docCreatorUserName);
			if(creatorUser!=null)
			{
				delegateExecution.setVariable("initiator", new ActivitiScriptNode(creatorUser, serviceRegistry));
			}
		}
    }

    @Override
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.personService = serviceRegistry.getPersonService();
    }

}
