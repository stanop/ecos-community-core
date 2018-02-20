package ru.citeck.ecos.flowable.listeners.global.impl.task.create;

import org.flowable.task.service.delegate.DelegateTask;
import ru.citeck.ecos.flowable.listeners.global.GlobalAssignmentTaskListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalCreateTaskListener;
import ru.citeck.ecos.flowable.utils.FlowableGrantWorkflowPackageHelper;

/**
 * Grant workflow package listener
 */
public class GrantWorkflowPackageListener implements GlobalCreateTaskListener, GlobalAssignmentTaskListener {

    /**
     * Constants
     */
    private static final String VAR_GRANTED_PERMISSION = "grantedPermission";

    /**
     * Granted permission
     */
    private String grantedPermission;

    /**
     * Grant workflow package helper
     */
    private FlowableGrantWorkflowPackageHelper helper;

    /**
     * Notify
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        helper.revoke(delegateTask);
        String permission = delegateTask.hasVariable(VAR_GRANTED_PERMISSION)
                ? (String) delegateTask.getVariable(VAR_GRANTED_PERMISSION)
                : this.grantedPermission;
        if(permission != null) {
            helper.grant(delegateTask, permission);
        }
    }

    /**
     * Set granted permission
     * @param grantedPermission Granted permission
     */
    public void setGrantedPermission(String grantedPermission) {
        this.grantedPermission = grantedPermission;
    }

    /**
     * Set grant workflow package helper
     * @param helper Grant workflow package helper
     */
    public void setHelper(FlowableGrantWorkflowPackageHelper helper) {
        this.helper = helper;
    }
}
