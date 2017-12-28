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
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.workflow.AdvancedWorkflowService;
import ru.citeck.ecos.workflow.listeners.GrantWorkflowTaskPermissionExecutor;
import ru.citeck.ecos.workflow.mirror.WorkflowMirrorService;
import ru.citeck.ecos.workflow.tasks.AdvancedTaskQuery;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TaskDeputyListener extends AbstractDeputyListener {

    private static final Log logger = LogFactory.getLog(TaskDeputyListener.class);

    private static final String AVAILABLE_PROCESS_NAME   = "on-user-available-process";
    private static final String UNAVAILABLE_PROCESS_NAME = "on-user-unavailable-process";
    
    private static final int BATCH_SIZE       = 30;
    private static final int WORKER_THREADS   = 5;
    private static final int LOGGING_INTERVAL = 30;
    
    private WorkflowService workflowService;

    private AdvancedWorkflowService advancedWorkflowService;

    private PersonServiceImpl personService;

    private TransactionService transactionService;

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

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
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
//        AdvancedTaskQuery query = new AdvancedTaskQuery().setClaimOwner(userName);
        AdvancedTaskQuery query = new AdvancedTaskQuery().setOriginalOwner(userName).withoutGroupCandidates();
        List<WorkflowTask> tasks = advancedWorkflowService.queryTasks(query);
        
        if (tasks.size() > 0) {
            List<String> userDeputies = new CopyOnWriteArrayList<>(deputyService.getUserDeputies(userName));
            userDeputies.add(userName);

            RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();

            BatchProcessor<WorkflowTask> batchProcessor = new BatchProcessor<>(
                    AVAILABLE_PROCESS_NAME,
                    retryingTransactionHelper,
                    new TaskDeputyProvider(tasks),
                    WORKER_THREADS, BATCH_SIZE,
                    null, logger, LOGGING_INTERVAL
            );

            batchProcessor.process(new TaskDeputyWorker(new OnUserAvailableTaskDeputyStrategy(userName, userDeputies)), true);
        }
    }

    @Override
    public void onUserUnavailable(String userName) {
//        AdvancedTaskQuery query = new AdvancedTaskQuery().setClaimOwner(userName);
        AdvancedTaskQuery query = new AdvancedTaskQuery().setAssignee(userName).withoutGroupCandidates();
        List<WorkflowTask> tasks = advancedWorkflowService.queryTasks(query);

        if (tasks.size() > 0) {
            RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();

            BatchProcessor<WorkflowTask> batchProcessor = new BatchProcessor<>(
                    UNAVAILABLE_PROCESS_NAME,
                    retryingTransactionHelper,
                    new TaskDeputyProvider(tasks),
                    WORKER_THREADS, BATCH_SIZE,
                    null, logger, LOGGING_INTERVAL
            );

            batchProcessor.process(new TaskDeputyWorker(new OnUserUnavailableTaskDeputyStrategy(userName)), true);
        }
    }

    private static class TaskDeputyProvider implements BatchProcessWorkProvider<WorkflowTask> {

        private Collection<WorkflowTask> workflowTasks;
        private boolean hasMore = true;
        
        TaskDeputyProvider(Collection<WorkflowTask> workflowTasks) {
            this.workflowTasks = workflowTasks;
        }
        
        @Override
        public int getTotalEstimatedWorkSize() {
            return workflowTasks.size();
        }

        @Override
        public Collection<WorkflowTask> getNextWork() {
            if (hasMore) {
                hasMore = false;
                return workflowTasks;
            } else {
                return Collections.emptyList();
            }
        }
    }

    interface Strategy {
        void perform(WorkflowTask task);
    }

    private class OnUserAvailableTaskDeputyStrategy implements Strategy {

        private String       userName;
        private List<String> userDeputies;

        OnUserAvailableTaskDeputyStrategy(String userName, List<String> userDeputies) {
            this.userName     = userName;
            this.userDeputies = userDeputies;
        }
        
        @Override
        public void perform(WorkflowTask task) {
            @SuppressWarnings("unchecked")
            List<NodeRef> pooledActors = (List<NodeRef>) task.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS);
            if (pooledActors != null && userDeputies.size() == pooledActors.size()) {
                removePooledActors(Collections.singletonList(task), userDeputies);
                setTaskOwner(task, userName);
            } else {
                resetTaskOwner(task, userName);
                AuthenticationUtil.runAsSystem(() -> {
                    workflowMirrorService.mirrorTask(task);
                    return null;
                });
                grantWorkflowTaskPermissionExecutor.grantPermissions(task);
            }
        }
    }

    private class OnUserUnavailableTaskDeputyStrategy implements Strategy {
    
        private String userName;

        OnUserUnavailableTaskDeputyStrategy(String userName) {
            this.userName = userName;
        }

        @Override
        public void perform(WorkflowTask task) {
            resetTaskOwner(task, userName);

            @SuppressWarnings("unchecked")
            List<NodeRef> candidates = (List<NodeRef>) task.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS);
            if (candidates.size() == 0) {
                List<String> actors = deputyService.getUserDeputies(userName);
                actors.add(userName);
                addPooledActors(Collections.singletonList(task), actors);
                WorkflowTask updatedTask = workflowService.getTaskById(task.getId());
                AuthenticationUtil.runAsSystem(() -> {
                    workflowMirrorService.mirrorTask(updatedTask);
                    return null;
                });
                grantWorkflowTaskPermissionExecutor.grantPermissions(updatedTask);
            }
        }
    }

    private static class TaskDeputyWorker extends BatchProcessor.BatchProcessWorkerAdaptor<WorkflowTask> {

        private Strategy strategy;
        
        TaskDeputyWorker(Strategy strategy) {
            this.strategy = strategy;
        }
        
        @Override
        public void process(WorkflowTask task) throws Throwable {
            strategy.perform(task);
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
        changeTaskOwner(tasks, props);
    }

    private void resetTaskOwner(WorkflowTask task, String owner) {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(PROP_OWNER, null);
        changeTaskOwner(task, props);
    }

    private void setTaskOwner(List<WorkflowTask> tasks, String owner) {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(PROP_OWNER, owner);
        changeTaskOwner(tasks, props);
    }

    private void setTaskOwner(WorkflowTask task, String owner) {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(PROP_OWNER, owner);
        changeTaskOwner(task, props);
    }

    private void changeTaskOwner(List<WorkflowTask> tasks, Map<QName, Serializable> props) {
        for (final WorkflowTask task : tasks) {
            changeTaskOwner(task, props);
        }
    }

    private void changeTaskOwner(WorkflowTask task, Map<QName, Serializable> props) {
        AuthenticationUtil.runAsSystem((AuthenticationUtil.RunAsWork<Void>) () -> {
            workflowService.updateTask(task.getId(), props, null, null);
            return null;
        });
    }

    public void setWorkflowMirrorService(WorkflowMirrorService workflowMirrorService) {
        this.workflowMirrorService = workflowMirrorService;
    }

    public void setGrantWorkflowTaskPermissionExecutor(GrantWorkflowTaskPermissionExecutor grantWorkflowTaskPermissionExecutor) {
        this.grantWorkflowTaskPermissionExecutor = grantWorkflowTaskPermissionExecutor;
    }

    private static final QName PROP_OWNER = ContentModel.PROP_OWNER;
}
