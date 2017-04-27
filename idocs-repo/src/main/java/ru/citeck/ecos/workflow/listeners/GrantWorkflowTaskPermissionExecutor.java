package ru.citeck.ecos.workflow.listeners;

import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;

public class GrantWorkflowTaskPermissionExecutor {
    private static final String VAR_GRANTED_PERMISSION = "grantedPermission";

    private GrantWorkflowPackageHelper helper;
    private String grantedPermission;

    public void setHelper(GrantWorkflowPackageHelper helper) {
        this.helper = helper;
    }

    /**
     * Set permission to be granted by task listener.
     * This can be overridden by task variable 'grantedPermission'.
     *
     * @param grantedPermission
     */
    public void setGrantedPermission(String grantedPermission) {
        this.grantedPermission = grantedPermission;
    }

    public void grantPermissions(WorkflowTask task) {

        // if it is assignment (not create) - first revoke all given permissions
        // UPD: assignment can be fired before create, so we need to revoke in create too
        helper.revoke(task);

        // grant permissions
        String permission = task.getProperties().get(QName.createQName("", VAR_GRANTED_PERMISSION)) != null
                ? (String) task.getProperties().get(QName.createQName("", VAR_GRANTED_PERMISSION))
                : this.grantedPermission;
        if (permission != null) {
            helper.grant(task, permission);
        }
    }
}
