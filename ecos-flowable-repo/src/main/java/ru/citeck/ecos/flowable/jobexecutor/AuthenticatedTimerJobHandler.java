package ru.citeck.ecos.flowable.jobexecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.flowable.engine.common.impl.interceptor.CommandContext;
import org.flowable.job.service.JobHandler;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.flowable.variable.api.delegate.VariableScope;

public class AuthenticatedTimerJobHandler implements JobHandler {

    private JobHandler wrappedHandler;
    private NodeService unprotectedNodeService;

    public AuthenticatedTimerJobHandler(JobHandler jobHandler, NodeService nodeService) {
        if (jobHandler == null) {
            throw new IllegalArgumentException("JobHandler to delegate to is required");
        }
        if (nodeService == null) {
            throw new IllegalArgumentException("NodeService is required");
        }
        this.unprotectedNodeService = nodeService;
        this.wrappedHandler = jobHandler;
    }

    @Override
    public String getType() {
        return wrappedHandler.getType();
    }

    @Override
    public void execute(JobEntity job, String configuration, VariableScope variableScope, CommandContext commandContext) {
        /*String userName;
        String tenantToRunIn = (String) variableScope.getVariable(ActivitiConstants.VAR_TENANT_DOMAIN);
        if (tenantToRunIn != null && tenantToRunIn.trim().length() == 0) {
            tenantToRunIn = null;
        }

        final Object initiatorNode = variableScope.getVariable(WorkflowConstants.PROP_INITIATOR);

        // Extracting the properties from the initiatornode should be done in correct tennant or as administrator, since we don't
        // know who started the workflow yet (We can't access node-properties when no valid authentication context is set up).
        if (tenantToRunIn != null) {
            userName = TenantUtil.runAsTenant(() -> getInitiator(initiatorNode), tenantToRunIn);
        } else {
            // No tenant on worklfow, run as admin in default tenant
            userName = AuthenticationUtil.runAs(() -> getInitiator(initiatorNode), AuthenticationUtil.getSystemUserName());
        }

        // When no task assignee is set, nor the initiator, use system user to run job
        if (userName == null) {
            userName = AuthenticationUtil.getSystemUserName();
            tenantToRunIn = null;
        }

        if (tenantToRunIn != null) {
            TenantUtil.runAsUserTenant((TenantUtil.TenantRunAsWork<Void>) () -> {
                wrappedHandler.execute(job, configuration, variableScope, commandContext);
                return null;
            }, userName, tenantToRunIn);
        } else {
            // Execute the timer without tenant
            AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () -> {
                wrappedHandler.execute(job, configuration, variableScope, commandContext);
                return null;
            }, AuthenticationUtil.getSystemUserName());
        }*/

        AuthenticationUtil.runAsSystem(() -> {
            wrappedHandler.execute(job, configuration, variableScope, commandContext);
            return null;
        });

    }

    private String getInitiator(Object initiatorNode) {
        if (initiatorNode == null) {
            return null;
        }

        NodeRef ref = null;
        if (initiatorNode instanceof NodeRef) {
            ref = (NodeRef) initiatorNode;
        } else if (initiatorNode instanceof ScriptNode) {
            ref = ((ScriptNode) initiatorNode).getNodeRef();
        }

        if (ref != null && unprotectedNodeService.exists(ref)) {
            return (String) unprotectedNodeService.getProperty(ref, ContentModel.PROP_USERNAME);
        }

        return null;
    }
}
