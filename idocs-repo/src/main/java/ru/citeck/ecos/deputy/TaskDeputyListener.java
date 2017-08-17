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
package ru.citeck.ecos.deputy;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.workflow.AdvancedWorkflowService;
import ru.citeck.ecos.workflow.listeners.GrantWorkflowTaskPermissionExecutor;
import ru.citeck.ecos.workflow.mirror.WorkflowMirrorService;
import ru.citeck.ecos.workflow.tasks.AdvancedTaskQuery;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class TaskDeputyListener extends AbstractDeputyListener {
    private WorkflowService workflowService;

    private AdvancedWorkflowService advancedWorkflowService;

    private PersonServiceImpl personService;

    private WorkflowMirrorService workflowMirrorService;

    private GrantWorkflowTaskPermissionExecutor grantWorkflowTaskPermissionExecutor;

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setAdvancedWorkflowService(AdvancedWorkflowService advancedWorkflowService) {
        this.advancedWorkflowService = advancedWorkflowService;
    }

    public void setPersonService(PersonServiceImpl personService) {
        this.personService = personService;
    }

    @Override
    public void onRoleMemberAvailable(String roleFullName, String memberName) {
        // role member is available (new or old)
        // reset owner for all role tasks from his deputies
        List<String> deputies = this.deputyService.getRoleDeputies(roleFullName); // members?
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .setCandidateGroups(Collections.singletonList(roleFullName))
                .setAssignees(deputies);
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);
        resetTaskOwner(workflowTasks);
    }

    @Override
    public void onRoleMemberUnavailable(String roleFullName, String memberName) {
        // role member is unavailable (new or old)
        // reset owner for all role tasks, that are assigned to him
        // deputies should be added to role by another deputy listener,
        // so they should have access to role tasks
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .setCandidateGroups(Collections.singletonList(roleFullName))
                .setAssignee(memberName);
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);
        resetTaskOwner(workflowTasks);
    }

    @Override
    public void onRoleDeputyAvailable(String roleFullName, String deputyName) {
        // role deputy is available
        // deputies are added to role by another deputy listener,
        // so nothing else is necessary
    }

    @Override
    public void onRoleDeputyUnavailable(String roleFullName, String deputyName) {
        // role deputy is not available
        // reset owner for all tasks, that are assigned to this deputy
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .setCandidateGroups(Collections.singletonList(roleFullName))
                .setAssignee(deputyName);
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);
        resetTaskOwner(workflowTasks);
    }

    @Override
    public void onUserDeputyAvailable(String userName, String deputyName) {
        // do nothing by default
    }

    @Override
    public void onUserDeputyUnavailable(String userName, String deputyName) {
        // reset owner for all deputied tasks
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .withoutGroupCandidates()
                .setAssignee(deputyName)
                .setCandidateUser(deputyName);
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);
        resetTaskOwner(workflowTasks);
    }

    @Override
    public void onUserAvailable(String userName) {
        // user is available (new or old)
        // reset owner for all tasks, that not assigned to any role
        // where original owner is user

        // TODO combine two queries into one
        AdvancedTaskQuery taskQuery = (AdvancedTaskQuery) new AdvancedTaskQuery()
                .withoutGroupCandidates()
                .taskOwner(userName);
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);

        taskQuery = (AdvancedTaskQuery) new AdvancedTaskQuery()
                .withoutGroupCandidates()
                .processVariableValueEquals("claimOwner", userName);
        workflowTasks.addAll(advancedWorkflowService.queryTasks(taskQuery));

        List<String> userDeputies = deputyService.getUserDeputies(userName);
        userDeputies.add(userName);

        removePooledActors(workflowTasks, userDeputies);

        workflowTasks = workflowTasks.stream()
                .map(workflowTask -> workflowService.getTaskById(workflowTask.getId())).collect(Collectors.toList());

        // Keep in mind assistants. They always have permissions if user is owner
        if (deputyService.getUserAssistants(userName).size() == 0)
            setTaskOwner(workflowTasks, userName);
        else {
            resetTaskOwner(workflowTasks, userName);
            workflowTasks.forEach(workflowTask -> {
                workflowMirrorService.mirrorTask(workflowTask);
                grantWorkflowTaskPermissionExecutor.grantPermissions(workflowTask);
            });
        }
    }

    @Override
    public void onUserUnavailable(String userName) {
        // user is unavailable (new or old)
        // reset owner for all tasks, that are owned to him, but not assigned to any role
        // and set pooled actors of user and his deputies

        // TODO combine two queries into one

        AdvancedTaskQuery taskQuery = (AdvancedTaskQuery) new AdvancedTaskQuery()
                .withoutGroupCandidates()
                .taskOwner(userName);
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);

        taskQuery = (AdvancedTaskQuery) new AdvancedTaskQuery()
                .withoutGroupCandidates()
                .processVariableValueEquals("claimOwner", userName);
        workflowTasks.addAll(advancedWorkflowService.queryTasks(taskQuery));

        List<String> actors = deputyService.getUserDeputies(userName);
        actors.add(userName);

        addPooledActors(workflowTasks, actors);
        resetTaskOwner(workflowTasks, userName);

        List<WorkflowTask> updatedTasks = workflowTasks.stream()
                .map(workflowTask -> workflowService.getTaskById(workflowTask.getId())).collect(Collectors.toList());

        for (WorkflowTask task : updatedTasks) {
            workflowMirrorService.mirrorTask(task);
            grantWorkflowTaskPermissionExecutor.grantPermissions(task);
        }
    }

    @Override
    public void onAssistantAdded(String userName) {
        //addDeputiesToTasks(userName);
    }

    @Override
    public void onAssistantRemoved(String userName, String deputyName) {
        //removeDeputiesFromTasks(userName, Collections.singletonList(deputyName));
    }

    public ArrayList<String> getActorsList(String userName) {
        Set<String> deputies = new HashSet<>();
        deputies.addAll(deputyService.getUserAssistants(userName));
        if (!deputyService.isUserAvailable(userName)) {
            deputies.addAll(deputyService.getUserDeputies(userName));
        }
        deputies.add(userName);
        return new ArrayList<String>(deputies);
    }

    private void addPooledActors(List<WorkflowTask> tasks, List<String> actors) {
        updatePooledActors(tasks, actors, true);
    }

    private void removePooledActors(List<WorkflowTask> tasks, List<String> actors) {
        updatePooledActors(tasks, actors, false);
    }

    public void updatePooledActors(List<WorkflowTask> tasks, List<String> actors, final boolean add) {
        List<NodeRef> actorsList = new ArrayList<>();

        for (String actor : actors) {
            NodeRef person = personService.getPerson(actor);
            actorsList.add(person);
        }

        for (final WorkflowTask task : tasks) {
            final Map<QName, List<NodeRef>> assocs = Collections.singletonMap(WorkflowModel.ASSOC_POOLED_ACTORS, actorsList);
            final Map<QName, Serializable> properties = new HashMap<>();

            AuthenticationUtil.runAsSystem((AuthenticationUtil.RunAsWork<Void>) () -> {
                if (add) {
                    workflowService.updateTask(task.getId(), properties, assocs, null);
                } else {
                    workflowService.updateTask(task.getId(), properties, null, assocs);
                }
                return null;
            });
        }
    }

    private void resetTaskOwner(List<WorkflowTask> tasks) {
        changeTaskOwner(tasks, Collections.singletonMap(ContentModel.PROP_OWNER, null));
    }

    private void resetTaskOwner(List<WorkflowTask> tasks, String owner) {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(PROP_OWNER, null);
        props.put(CLAIM_OWNER, owner);
        changeTaskOwner(tasks, props);
    }

    private void setTaskOwner(List<WorkflowTask> tasks, String owner) {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(PROP_OWNER, owner);
        props.put(CLAIM_OWNER, null);
        changeTaskOwner(tasks, props);
    }

    private void changeTaskOwner(List<WorkflowTask> tasks, Map<QName, Serializable> props) {
        for (final WorkflowTask task : tasks) {
            if (task.getProperties().get(PROP_OWNER) == null && props.get(CLAIM_OWNER) != null)
                continue;
            AuthenticationUtil.runAsSystem((AuthenticationUtil.RunAsWork<Void>) () -> {
                workflowService.updateTask(task.getId(), props, null, null);
                return null;
            });
        }
    }

    public void setWorkflowMirrorService(WorkflowMirrorService workflowMirrorService) {
        this.workflowMirrorService = workflowMirrorService;
    }

    public void setGrantWorkflowTaskPermissionExecutor(GrantWorkflowTaskPermissionExecutor grantWorkflowTaskPermissionExecutor) {
        this.grantWorkflowTaskPermissionExecutor = grantWorkflowTaskPermissionExecutor;
    }

    private static final QName PROP_OWNER = ContentModel.PROP_OWNER;
    private static final QName CLAIM_OWNER = QName.createQName(null, "claimOwner");
}
