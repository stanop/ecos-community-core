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
package ru.citeck.ecos.workflow.tasks;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.task.TaskQuery;

import java.util.List;

public class AdvancedTaskQuery extends TaskQueryImpl {

    protected List<String> owners;

    protected boolean withoutGroupCandidates = false;

    protected boolean withoutCandidates = false;

    protected String originalOwner;

    private List<String> assignees;

    private String claimOwner;

    public AdvancedTaskQuery setAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public AdvancedTaskQuery setCandidateGroups(List<String> candidateGroups) {
        taskCandidateGroupIn(candidateGroups);
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public AdvancedTaskQuery setOwner(String owner) {
        taskOwner(owner);
        return this;
    }

    public List<String> getOwners() {
        return owners;
    }

    public AdvancedTaskQuery setOwners(List<String> owners) {
        if (owners == null) {
            throw new ActivitiException("Owners list is null");
        }
        if (owners.isEmpty()) {
            throw new ActivitiException("Owners list is empty");
        }
        if (owner != null) {
            throw new ActivitiException("Invalid query usage: cannot set both owners and owner");
        }
        this.owners = owners;
        return this;
    }

    public boolean isWithoutGroupCandidates() {
        return withoutGroupCandidates;
    }

    public AdvancedTaskQuery withoutGroupCandidates() {
        if (withoutCandidates) {
            throw new ActivitiException("Invalid query usage: cannot set both withoutGroupCandidates and withoutCandidates");
        }
        this.withoutGroupCandidates = true;
        return this;
    }

    public boolean isWithoutCandidates() {
        return withoutCandidates;
    }

    public AdvancedTaskQuery withoutCandidates() {
        if (withoutGroupCandidates) {
            throw new ActivitiException("Invalid query usage: cannot set both withoutCandidates and withoutGroupCandidates");
        }
        this.withoutCandidates = true;
        return this;
    }

    @Override
    public TaskQueryImpl taskCandidateGroup(String candidateGroup) {
        if (withoutGroupCandidates) {
            throw new ActivitiException("Invalid query usage: cannot set both candidateGroup and withoutGroupCandidates");
        }
        if (withoutCandidates) {
            throw new ActivitiException("Invalid query usage: cannot set both candidateGroup and withoutCandidates");
        }
        return super.taskCandidateGroup(candidateGroup);
    }

    @Override
    public TaskQuery taskCandidateGroupIn(List<String> candidateGroups) {
        if (withoutGroupCandidates) {
            throw new ActivitiException("Invalid query usage: cannot set both candidateGroupIn and withoutGroupCandidates");
        }
        if (withoutCandidates) {
            throw new ActivitiException("Invalid query usage: cannot set both candidateGroupIn and withoutCandidates");
        }
        return super.taskCandidateGroupIn(candidateGroups);
    }

    public String getOriginalOwner() {
        return originalOwner;
    }

    public AdvancedTaskQuery setOriginalOwner(String originalOwner) {
        this.originalOwner = originalOwner;
        return this;
    }

    public AdvancedTaskQuery setCandidateUser(String candidateUser) {
        taskCandidateUser(candidateUser);
        return this;
    }

    @Override
    protected List<String> getGroupsForCandidateUser(String candidateUser) {
        return null;
    }

    @Override
    public TaskQueryImpl taskCandidateUser(String candidateUser) {
        if (withoutCandidates) {
            throw new ActivitiException("Invalid query usage: cannot set both candidateUser and withoutCandidates");
        }
        return super.taskCandidateUser(candidateUser);
    }

    public AdvancedTaskQuery setAssignees(List<String> assignees) {
        this.assignees = assignees;
        return this;
    }

    public List<String> getAssignees() {
        return assignees;
    }

    public String getClaimOwner() {
        return claimOwner;
    }

    public AdvancedTaskQuery setClaimOwner(String claimOwner) {
        this.claimOwner = claimOwner;
        return this;
    }
}
