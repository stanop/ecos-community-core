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
package ru.citeck.ecos.delegate;

import java.io.Serializable;
import java.util.*;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;

import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.workflow.tasks.AdvancedTaskQuery;
import ru.citeck.ecos.workflow.AdvancedWorkflowService;

public class TaskDelegateListener extends AbstractDelegateListener
{
	private WorkflowService workflowService;

	private AdvancedWorkflowService advancedWorkflowService;

    private PersonServiceImpl personService;

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
		// reset owner for all role tasks from his delegates
        List<String> delegates = this.delegateService.getRoleDelegates(roleFullName); // members?
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .setCandidateGroups(Collections.singletonList(roleFullName))
                .setAssignees(delegates);
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);
        resetTaskOwner(workflowTasks);
	}

	@Override
	public void onRoleMemberUnavailable(String roleFullName, String memberName) {
		// role member is unavailable (new or old)
		// reset owner for all role tasks, that are assigned to him
		// delegates should be added to role by another delegate listener, 
		// so they should have access to role tasks
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .setCandidateGroups(Collections.singletonList(roleFullName))
                .setAssignee(memberName);
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);
        resetTaskOwner(workflowTasks);
	}

	@Override
	public void onRoleDelegateAvailable(String roleFullName, String delegateName) {
		// role delegate is available
		// delegates are added to role by another delegate listener,
		// so nothing else is necessary
	}

	@Override
	public void onRoleDelegateUnavailable(String roleFullName, String delegateName)	{
		// role delegate is not available
		// reset owner for all tasks, that are assigned to this delegate
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .setCandidateGroups(Collections.singletonList(roleFullName))
                .setAssignee(delegateName);
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);
        resetTaskOwner(workflowTasks);
	}

	@Override
	public void onUserDelegateAvailable(String userName, String delegateName) {
		// do nothing by default
    }

	@Override
	public void onUserDelegateUnavailable(String userName, String delegateName) {
		// reset owner for all delegated tasks
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .withoutGroupCandidates()
                .setAssignee(delegateName)
                .setCandidateUser(delegateName);
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
        // and set pooled actors of user and his delegates
        AdvancedTaskQuery taskQuery = new AdvancedTaskQuery()
                .setAssignee(userName)
                .withoutGroupCandidates();
        List<WorkflowTask> workflowTasks = advancedWorkflowService.queryTasks(taskQuery);
        resetTaskOwner(workflowTasks);
        addPooledActors(workflowTasks, getActorsList(userName));
    }

    public ArrayList<String> getActorsList(String userName) {
        List<String> delegates = this.delegateService.getUserDelegates(userName);
        ArrayList<String> actors = new ArrayList<String>(delegates);
        actors.add(userName);
        return actors;
    }

    private void addPooledActors(List<WorkflowTask> tasks, List<String> actors) {
        updatePooledActors(tasks, actors, true);
    }

    private void removePooledActors(List<WorkflowTask> tasks, List<String> actors) {
        updatePooledActors(tasks, actors, false);
    }

    private void updatePooledActors(List<WorkflowTask> tasks, List<String> actors, boolean add) {
        for (WorkflowTask task : tasks) {
            List<NodeRef> actorsList = new ArrayList<NodeRef>();
            for (String actor : actors) {
                NodeRef person = personService.getPerson(actor);
                actorsList.add(person);
            }
            Map<QName,List<NodeRef>> assocs = Collections.singletonMap(WorkflowModel.ASSOC_POOLED_ACTORS, actorsList);
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
            if (add) {
                workflowService.updateTask(task.getId(), properties, assocs, null);
            } else {
                workflowService.updateTask(task.getId(), properties, null, assocs);
            }
        }
    }

	private void resetTaskOwner(List<WorkflowTask> tasks) {
       resetTaskOwner(tasks, null);
	}

    private void resetTaskOwner(List<WorkflowTask> tasks, String owner) {
        for (WorkflowTask task : tasks) {
            workflowService.updateTask(task.getId(), Collections.singletonMap(ContentModel.PROP_OWNER, (Serializable) owner), null, null);
        }
    }
}
