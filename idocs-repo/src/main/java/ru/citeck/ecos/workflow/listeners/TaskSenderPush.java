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
 * This task listener automatically sets variable cwf:sender with name of current user.
 * When next tasks are created this variable is copied into them.
 * 
 * @author Sergey Tiunov
 */
public class TaskSenderPush extends AbstractTaskListener 
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
        task.getExecution().setVariable(
            qNameConverter.mapQNameToName(CiteckWorkflowModel.PROP_SENDER), 
            task.getAssignee()
        );
        updateSenderName(task, task.getAssignee());
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
            task.getExecution().setVariable(
                    qNameConverter.mapQNameToName(CiteckWorkflowModel.PROP_SENDER_NAME),
                    name
            );
        }
    }

}
