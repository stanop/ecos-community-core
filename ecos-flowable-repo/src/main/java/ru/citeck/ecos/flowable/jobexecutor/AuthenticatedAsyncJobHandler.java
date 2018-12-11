package ru.citeck.ecos.flowable.jobexecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.flowable.engine.common.impl.interceptor.CommandContext;
import org.flowable.job.service.JobHandler;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.flowable.variable.api.delegate.VariableScope;
import ru.citeck.ecos.flowable.variable.FlowableScriptNode;

public class AuthenticatedAsyncJobHandler implements JobHandler {

    private JobHandler wrappedHandler;

    public AuthenticatedAsyncJobHandler(JobHandler jobHandler) {
        if (jobHandler == null) {
            throw new IllegalArgumentException("JobHandler to delegate to is required");
        }
        this.wrappedHandler = jobHandler;
    }

    @Override
    public String getType() {
        return wrappedHandler.getType();
    }

    @Override
    public void execute(JobEntity job, String configuration, VariableScope variableScope, CommandContext commandContext) {
        // Get initiator
        String userName = AuthenticationUtil.runAsSystem(() -> {
            FlowableScriptNode ownerNode = (FlowableScriptNode) variableScope.getVariable(WorkflowConstants.PROP_INITIATOR);
            if (ownerNode != null && ownerNode.exists()) {
                return (String) ownerNode.getProperties().get(ContentModel.PROP_USERNAME);
            }
            return null;
        });


        // When no initiator is set, use system user to run job
        if (userName == null) {
            userName = AuthenticationUtil.getSystemUserName();
        }

        // Execute job
        AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () -> {
            wrappedHandler.execute(job, configuration, variableScope, commandContext);
            return null;
        }, userName);
    }
}
