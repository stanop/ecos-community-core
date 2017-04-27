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

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;

import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.model.CiteckWorkflowModel;

import java.io.Serializable;
import java.util.Map;

/**
 * This task listener automatically copies variable cwf:sender from execution context.
 * 
 * @author Sergey Tiunov
 */
public class TaskSenderPull extends AbstractTaskListener 
{

	private NodeService nodeService;
    private WorkflowQNameConverter qNameConverter;
    private PersonService personService;

    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.personService = serviceRegistry.getPersonService();
        this.qNameConverter = new WorkflowQNameConverter(serviceRegistry.getNamespaceService());
    }

    @Override
    protected void notifyImpl(DelegateTask task) {
        String varName = qNameConverter.mapQNameToName(CiteckWorkflowModel.PROP_SENDER);
        
        // if sender is set in execution context - it is set in task
        // otherwise workflow initiator is considered as sender
        Object value = task.getExecution().getVariable(varName);
        if(value == null) {
            NodeRef initiator = ((ScriptNode) task.getExecution().getVariable(WorkflowConstants.PROP_INITIATOR)).getNodeRef();
            value = nodeService.getProperty(initiator, ContentModel.PROP_USERNAME);
        }
        task.setVariableLocal(varName, value);
        updateSenderName(task, (String) value);
    }

    private void updateSenderName(DelegateTask task, String person) {
        if (!StringUtils.isEmpty(person) && personService.personExists(person)) {
            NodeRef personNode =  personService.getPerson(person);
            Map<QName, Serializable> properties = nodeService.getProperties(personNode);
            String firstName = (String) properties.get(ContentModel.PROP_FIRSTNAME);
            String lastName = (String) properties.get(ContentModel.PROP_LASTNAME);
            String name = firstName;
            if (!StringUtils.isEmpty(lastName)) {
                name += StringUtils.isEmpty(firstName)? lastName : " " + lastName;
            }
            task.setVariableLocal(qNameConverter.mapQNameToName(CiteckWorkflowModel.PROP_SENDER_NAME), name);
        }
    }

}
