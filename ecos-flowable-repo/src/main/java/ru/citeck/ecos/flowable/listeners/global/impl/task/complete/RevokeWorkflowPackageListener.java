package ru.citeck.ecos.flowable.listeners.global.impl.task.complete;

import org.flowable.engine.delegate.DelegateTask;
import ru.citeck.ecos.flowable.listeners.global.GlobalCompleteTaskListener;
import ru.citeck.ecos.flowable.utils.FlowableGrantWorkflowPackageHelper;

/**
 * Revoke workflow package listener
 */
public class RevokeWorkflowPackageListener implements GlobalCompleteTaskListener {

    /**
     * Constants
     */
    private static final String VAR_POST_REVOKE_PERMISSION = "postRevokePermission";

    /**
     * Grant workflow package helper
     */
    private FlowableGrantWorkflowPackageHelper helper;

    /**
     * Post revoke permission
     */
    private String postRevokePermission;

    /**
     * Notify
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        helper.revoke(delegateTask);

        String permission = delegateTask.hasVariable(VAR_POST_REVOKE_PERMISSION)
                ? (String) delegateTask.getVariable(VAR_POST_REVOKE_PERMISSION)
                : this.postRevokePermission;
        if(permission != null && !permission.isEmpty()) {
            helper.grant(delegateTask, permission, true);
        }
    }

    /**
     * Set grant workflow package helper
     * @param helper Grant workflow package helper
     */
    public void setHelper(FlowableGrantWorkflowPackageHelper helper) {
        this.helper = helper;
    }

    /**
     * Set pPost revoke permission
     * @param postRevokePermission Post revoke permission
     */
    public void setPostRevokePermission(String postRevokePermission) {
        this.postRevokePermission = postRevokePermission;
    }
}
