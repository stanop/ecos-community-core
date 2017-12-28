package ru.citeck.ecos.flowable.listeners.global.impl.process.end;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.task.Task;
import ru.citeck.ecos.flowable.listeners.global.GlobalEndExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableGrantWorkflowPackageHelper;

import java.util.List;

/**
 * Revoke final workflow package listener
 */
public class RevokeFinalWorkflowPackageListener implements GlobalEndExecutionListener {

    /**
     * Constants
     */
    private static final String VAR_REVOKE_TASK_PERMISSIONS = "revokeTaskPermissions";
    private static final String VAR_REVOKE_PROCESS_PERMISSIONS = "revokeProcessPermissions";

    /**
     * Permissions
     */
    private boolean revokeTaskPermissions = true;
    private boolean revokeProcessPermissions = true;

    /**
     * Grant workflow package helper
     */
    private FlowableGrantWorkflowPackageHelper helper;

    /**
     * Notify
     * @param delegateExecution Execution
     */
    @Override
    public void notify(DelegateExecution delegateExecution) {
        List<Task> tasks = Context.getProcessEngineConfiguration().getTaskService().createTaskQuery().processInstanceId(delegateExecution.getProcessInstanceId()).list();
        Object revokeTaskPermissions = delegateExecution.getVariable(VAR_REVOKE_TASK_PERMISSIONS);
        if(revokeTaskPermissions == null) {
            revokeTaskPermissions = this.revokeTaskPermissions;
        }
        if(Boolean.TRUE.equals(revokeTaskPermissions)) {
            for(Task task : tasks) {
                helper.revoke(task, delegateExecution);
            }
        }
        Object revokeProcessPermissions = delegateExecution.getVariable(VAR_REVOKE_PROCESS_PERMISSIONS);
        if(revokeProcessPermissions == null) {
            revokeProcessPermissions = this.revokeProcessPermissions;
        }
        if(Boolean.TRUE.equals(revokeProcessPermissions)) {
            helper.revoke(delegateExecution);
        }
    }

    /**
     * Set task revoke permission
     * @param revokeTaskPermissions Task revoke permission
     */
    public void setRevokeTaskPermissions(boolean revokeTaskPermissions) {
        this.revokeTaskPermissions = revokeTaskPermissions;
    }

    /**
     * Set process revoke permission
     * @param revokeProcessPermissions Process revoke permission
     */
    public void setRevokeProcessPermissions(boolean revokeProcessPermissions) {
        this.revokeProcessPermissions = revokeProcessPermissions;
    }

    /**
     * Set grant workflow package helper
     * @param helper Grant workflow package helper
     */
    public void setHelper(FlowableGrantWorkflowPackageHelper helper) {
        this.helper = helper;
    }
}
