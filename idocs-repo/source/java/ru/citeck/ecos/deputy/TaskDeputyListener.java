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
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .withoutGroupCandidates()
                .setOriginalOwner(userName);
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);
        resetTaskOwner(workflowTasks, userName);
        removePooledActors(workflowTasks, getActorsList(userName));
    }

    @Override
    public void onUserUnavailable(String userName) {
        // user is unavailable (new or old)
        // reset owner for all tasks, that are owned to him, but not assigned to any role
        // and set pooled actors of user and his deputies
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .setAssignee(userName)
                .withoutGroupCandidates();
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);
        resetTaskOwner(workflowTasks);
        addPooledActors(workflowTasks, getActorsList(userName));
    }

    @Override
    public void onAssistantAdded(String userName) {
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .setOriginalOwner(userName)
                .withoutGroupCandidates();
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);
        List<String> actors = getActorsList(userName);
        addPooledActors(workflowTasks, actors);
        resetTaskOwner(workflowTasks);
        List<WorkflowTask> updatedWorkflowTasks = advancedWorkflowService.queryTasks(taskQuery);
        if (actors.size() > 2) {
            for (WorkflowTask task : updatedWorkflowTasks) {
                workflowMirrorService.mirrorTask(task);
                grantWorkflowTaskPermissionExecutor.grantPermissions(task);
            }
        }
    }

    @Override
    public void onAssistantRemoved(String userName, String deputyName) {
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .withoutGroupCandidates()
                .setOriginalOwner(userName);
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);

        List<String> actors = getActorsList(userName);
        removePooledActors(workflowTasks, Collections.singletonList(deputyName));
        if (actors.size() == 1) {
            resetTaskOwner(workflowTasks, userName);
        } else {
            resetTaskOwner(workflowTasks);
            List<WorkflowTask> updatedWorkflowTasks = advancedWorkflowService.queryTasks(taskQuery);
            for (WorkflowTask task : updatedWorkflowTasks) {
                workflowMirrorService.mirrorTask(task);
                grantWorkflowTaskPermissionExecutor.grantPermissions(task);
            }
        }
    }

    public ArrayList<String> getActorsList(String userName) {
        List<String> deputies = new ArrayList<>();
        deputies = this.deputyService.getUserAssistants(userName);
        if (!deputyService.isUserAvailable(userName)) {
            deputies.addAll(this.deputyService.getUserDeputies(userName));
        }
        ArrayList<String> actors = new ArrayList<String>(deputies);
        actors.add(userName);
        return actors;
    }

    private void addPooledActors(List<WorkflowTask> tasks, List<String> actors) {
        updatePooledActors(tasks, actors, true);
    }

    private void removePooledActors(List<WorkflowTask> tasks, List<String> actors) {
        updatePooledActors(tasks, actors, false);
    }

    private void updatePooledActors(List<WorkflowTask> tasks, List<String> actors, final boolean add) {
        for (final WorkflowTask task : tasks) {
            List<NodeRef> actorsList = new ArrayList<NodeRef>();
            for (String actor : actors) {
                NodeRef person = personService.getPerson(actor);
                actorsList.add(person);
            }
            final Map<QName, List<NodeRef>> assocs = Collections.singletonMap(WorkflowModel.ASSOC_POOLED_ACTORS, actorsList);
            final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
            AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
                public Void doWork() throws Exception {
                    if (add) {
                        workflowService.updateTask(task.getId(), properties, assocs, null);
                    } else {
                        workflowService.updateTask(task.getId(), properties, null, assocs);
                    }
                    return null;
                }
            });
        }
    }

    private void resetTaskOwner(List<WorkflowTask> tasks) {
        resetTaskOwner(tasks, null);
    }

    private void resetTaskOwner(List<WorkflowTask> tasks, final String owner) {
        for (final WorkflowTask task : tasks) {
            AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
                public Void doWork() throws Exception {
                    workflowService.updateTask(task.getId(), Collections.singletonMap(ContentModel.PROP_OWNER, (Serializable) owner), null, null);
                    return null;
                }
            });
        }
    }

    public void setWorkflowMirrorService(WorkflowMirrorService workflowMirrorService) {
        this.workflowMirrorService = workflowMirrorService;
    }

    public void setGrantWorkflowTaskPermissionExecutor(GrantWorkflowTaskPermissionExecutor grantWorkflowTaskPermissionExecutor) {
        this.grantWorkflowTaskPermissionExecutor = grantWorkflowTaskPermissionExecutor;
    }
}
