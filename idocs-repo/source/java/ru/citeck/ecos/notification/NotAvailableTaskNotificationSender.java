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
package ru.citeck.ecos.notification;

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.deputy.AvailabilityService;
import ru.citeck.ecos.model.CiteckWorkflowModel;

import java.io.Serializable;
import java.util.*;

/**
 * Notification sender for tasks(ItemType = DelegateTask).
 * <p>
 * The following implementation is used:
 * - subject line: default
 * - template: retrieved by key = process-definition
 * - template args:
 * {
 * "task": {
 * "id": "task id",
 * "name": "task name",
 * "description": "task description",
 * "priority": "task priority",
 * "dueDate": "task dueDate",
 * }
 * },
 * "workflow": {
 * "id": "workflow id",
 * "documents": [
 * "nodeRef1",
 * ...
 * ]
 * }
 * }
 * - notification recipients - assignee or pooled actors, whichever present
 */
class NotAvailableTaskNotificationSender extends DelegateTaskNotificationSender {

    private AvailabilityService availabilityService;

    @Override
    public void sendNotification(DelegateTask task) {
        NotificationContext notificationContext = new NotificationContext();
        NodeRef template = getNotificationTemplate(task);
        if (template != null && nodeService.exists(template)) {
            setBodyTemplate(notificationContext, template);
        }

        Map<String, Serializable> argsMap = getNotificationArgs(task);
        Map<String, String> answerByUnavailableUser = new HashMap<>();
        Map<String, ScriptNode> assigneesNodesByName = new HashMap<>();
        Set<String> recipients = new HashSet<>();
        String from = null;
        String initiator = getInitiator(task);

        WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(services.getNamespaceService());
        String lastTaskOwnerVar = qNameConverter.mapQNameToName(CiteckWorkflowModel.PROP_LAST_TASK_OWNER);
        String owner = task.getAssignee();
        String originalOwner = (String) task.getVariable("taskOriginalOwner");

        String lastTaskOwner = (String) task.getVariable(lastTaskOwnerVar);
        if (((lastTaskOwner != null && lastTaskOwner.equals(owner) && originalOwner != null && originalOwner.equals(lastTaskOwner)) || (lastTaskOwner == null && owner != null && originalOwner != null && owner.equals(originalOwner))) && isSendToInitiator(template)) {
            return;
        }
        if (isSendToInitiator(template)) {
            Set<String> assignees = getAssignee(task);
            for (String assignee : assignees) {
                String answer = availabilityService.getUserUnavailableAutoAnswer(assignee);
                if (answer != null) {
                    answerByUnavailableUser.put(assignee, answer);
                    assigneesNodesByName.put(assignee, new ScriptNode(services.getPersonService().getPerson(assignee), services));
                    argsMap.put("assignees", (Serializable) assigneesNodesByName);
                }
            }
            recipients.addAll(assigneesNodesByName.keySet());
            argsMap.put("isSendToInitiator", true);
        } else if (isSendToAssignee(template)) {
            String answer = availabilityService.getUserUnavailableAutoAnswer(initiator);
            if (answer != null) {
                answerByUnavailableUser.put(initiator, answer);
            }
            recipients.addAll(getAssignee(task));
            argsMap.put("isSendToAssignee", true);
        }

        from = initiator;


        if (!answerByUnavailableUser.isEmpty()) {
            argsMap.put("answerByUser", (Serializable) answerByUnavailableUser);
            send(recipients, from, argsMap, template, task, isSendToInitiator(template));
        }

    }

    @Override
    public NodeRef getNotificationTemplate(DelegateTask task) {
        return getNotificationTemplate(null, null, true);
    }

    public void setAvailabilityService(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    private void send(Set<String> recipients, String from, Map<String, Serializable> args, NodeRef template, DelegateTask task, boolean isSendFromAssegneesToInitiatior) {
        String taskFormKey = (String) task.getVariableLocal("taskFormKey");
        if (recipients != null && !recipients.isEmpty() && template != null) {
            String notificationProviderName = EMailNotificationProvider.NAME;
            String subject = getSubject(task, args, template, taskFormKey);
            if (isSendToInitiator(template)) {
                for (String rec : recipients) {
                    sendNotification(notificationProviderName, rec, subject, template, args, Collections.singletonList(from), false);
                }
            } else {
                for (String rec : recipients) {
                    sendNotification(notificationProviderName, from, subject, template, args, Collections.singletonList(rec), false);
                }
            }


        }
    }
}
