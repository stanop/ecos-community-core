package ru.citeck.ecos.flowable.utils;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.DelegateTask;
import org.flowable.engine.impl.persistence.entity.TaskEntity;
import org.flowable.engine.task.IdentityLink;
import org.flowable.engine.task.Task;
import ru.citeck.ecos.deputy.AuthorityHelper;
import ru.citeck.ecos.security.GrantPermissionService;
import ru.citeck.ecos.workflow.listeners.ListenerUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Flowable grant workflow package helper
 */
public class FlowableGrantWorkflowPackageHelper {

    /**
     * Constants
     */
    private static final String TASK_PROVIDER_PREFIX = "task-flowable-";
    private static final String PROCESS_PROVIDER_PREFIX = "process-flowable-";

    /**
     * Grant permission service
     */
    private GrantPermissionService grantPermissionService;
    private AuthorityHelper authorityHelper;

    /**
     * Set grant permission service
     * @param grantPermissionService Grant permission service
     */
    public void setGrantPermissionService(GrantPermissionService grantPermissionService) {
        this.grantPermissionService = grantPermissionService;
    }


    /**
     * Set authority helper
     * @param authorityHelper Authority helper
     */
    public void setAuthorityHelper(AuthorityHelper authorityHelper) {
        this.authorityHelper = authorityHelper;
    }

    /**
     * Grant specified permission to task assignees on a task scope.
     * @param task Task instance
     * @param permission Permission
     */
    public void grant(DelegateTask task, final String permission) {
        grant(task, permission, false);
    }

    /**
     * Grant specified permission to task assignees on a task scope.
     *
     * @param task Task instance
     * @param permission Permission
     */
    public void grant(WorkflowTask task, final String permission) {
        grant(task, permission, false);
    }

    /**
     * Grant specified permission to task assignees.
     * @param task Task instance
     * @param permission Permission
     * @param processScope - true if permission should be set on process scope, false - if on task scope
     */
    public void grant(DelegateTask task, final String permission, boolean processScope) {
        final Set<String> authorities = getTaskActors(task);
        final NodeRef workflowPackage = FlowableListenerUtils.getWorkflowPackage(task);
        final String provider = processScope ?
                getProcessPermissionProvider(task.getExecution()) :
                getTaskPermissionProvider(task);

        if(authorities.size() == 0 || workflowPackage == null) {
            return;
        }
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                for(String authority : authorities) {
                    grantPermissionService.grantPermission(workflowPackage, authority, permission, provider);
                }
                return null;
            }
        });

    }


    /**
     * Grant specified permission to task assignees.
     * @param task Task instance
     * @param permission Permission
     * @param processScope - true if permission should be set on process scope, false - if on task scope
     */
    public void grant(WorkflowTask task, final String permission, boolean processScope) {

        final Set<String> authorities = getTaskActors(task);
        final NodeRef workflowPackage = ListenerUtils.getWorkflowPackage(task);
        final String provider = getTaskPermissionProvider(task);

        if (authorities.size() == 0 || workflowPackage == null) {
            return;
        }

        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                for (String authority : authorities) {
                    grantPermissionService.grantPermission(workflowPackage, authority, permission, provider);
                }
                return null;
            }
        });

    }

    /**
     * Grant specified permission to specified authority on process scope.
     * @param execution Execution
     * @param permission Permission
     */
    public void grant(DelegateExecution execution, final String authority, final String permission) {

        final NodeRef workflowPackage = FlowableListenerUtils.getWorkflowPackage(execution);
        final String provider = getProcessPermissionProvider(execution);

        if(workflowPackage == null) {
            return;
        }

        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                grantPermissionService.grantPermission(workflowPackage, authority, permission, provider);
                return null;
            }
        });
    }

    /**
     * Revoke all permissions, granted on task scope.
     * @param task Task instance
     */
    public void revoke(DelegateTask task) {

        final NodeRef workflowPackage = FlowableListenerUtils.getWorkflowPackage(task);
        final String provider = getTaskPermissionProvider(task);

        if(workflowPackage == null) {
            return;
        }

        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                grantPermissionService.revokePermission(workflowPackage, provider);
                return null;
            }
        });
    }

    /**
     * Revoke all permissions, granted on task scope.
     * @param task Task instance
     */
    public void revoke(WorkflowTask task) {

        final NodeRef workflowPackage = ListenerUtils.getWorkflowPackage(task);
        final String provider = getTaskPermissionProvider(task);

        if (workflowPackage == null) {
            return;
        }

        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                grantPermissionService.revokePermission(workflowPackage, provider);
                return null;
            }
        });
    }

    /**
     * Revoke all permissions granted on task scope.
     * @param task Task
     * @param execution Execution
     */
    public void revoke(Task task, DelegateExecution execution) {

        final NodeRef workflowPackage = FlowableListenerUtils.getWorkflowPackage(execution);
        final String provider = getTaskPermissionProvider(task);

        if(workflowPackage == null) {
            return;
        }

        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                grantPermissionService.revokePermission(workflowPackage, provider);
                return null;
            }
        });
    }

    /**
     * Revoke all permissions granted on process scope.
     * @param execution Execution
     */
    public void revoke(DelegateExecution execution) {

        final NodeRef workflowPackage = FlowableListenerUtils.getWorkflowPackage(execution);
        final String provider = getProcessPermissionProvider(execution);

        if(workflowPackage == null) {
            return;
        }

        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                grantPermissionService.revokePermission(workflowPackage, provider);
                return null;
            }
        });
    }

    /**
     * Get task actors
     * @param task Task instance
     * @return Set of actors username
     */
    private Set<String> getTaskActors(DelegateTask task) {
        Set<String> actors = new HashSet<>();

        String actor = task.getAssignee();
        if(actor != null) {
            actors.add(actor);
        }
        Set<IdentityLink> candidates = ((TaskEntity) task).getCandidates();
        if(candidates != null) {
            for(IdentityLink candidate : candidates) {
                if(candidate.getGroupId() != null) {
                    actors.add(candidate.getGroupId());
                }
                if(candidate.getUserId() != null) {
                    actors.add(candidate.getUserId());
                }
            }
        }
        return actors;
    }

    /**
     * Get task actors
     * @param task Task
     * @return Set of actors username
     */
    private Set<String> getTaskActors(WorkflowTask task) {
        Set<String> actors = new HashSet<String>();
        String actor = (String) task.getProperties().get(ContentModel.PROP_OWNER);
        if (actor != null) {
            actors.add(actor);
        }

        List<NodeRef> candidates = (List<NodeRef>) task.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS);
        if (candidates != null) {
            for (NodeRef candidate : candidates) {
                actors.add(authorityHelper.getAuthorityName(candidate));
            }
        }
        return actors;
    }

    /**
     * Get task permission provider
     * @param task Task instance
     * @return Provider name
     */
    private String getTaskPermissionProvider(DelegateTask task) {
        return TASK_PROVIDER_PREFIX + task.getId();
    }

    /**
     * Get task permission provider
     * @param task Task instance
     * @return Provider name
     */
    private String getTaskPermissionProvider(Task task) {
        return TASK_PROVIDER_PREFIX + task.getId();
    }

    /**
     * Get task permission provider
     * @param task Task instance
     * @return Provider name
     */
    private String getTaskPermissionProvider(WorkflowTask task) {
        return TASK_PROVIDER_PREFIX + task.getProperties().get(WorkflowModel.PROP_TASK_ID);
    }

    /**
     * Get task permission provider
     * @param execution Execution
     * @return Provider name
     */
    private String getProcessPermissionProvider(DelegateExecution execution) {
        return PROCESS_PROVIDER_PREFIX + execution.getId();
    }

}
