package ru.citeck.ecos.flowable.listeners;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import ru.citeck.ecos.flowable.scripts.FlowableScriptBase;
import java.util.HashMap;
import java.util.Map;

/**
 * Flowable script task listener
 */
public class FlowableScriptTaskListener extends FlowableScriptBase implements TaskListener {

    /**
     * Constants
     */
    private static final String TASK_BINDING_NAME = "task";

    /**
     * Notify
     * @param delegateTask Delegate task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        validateParameters();

        String runAsUser = getStringValue(runAs, delegateTask);
        boolean clearAuthenticationContext = checkFullyAuthenticatedUser(delegateTask);

        Map<String, Object> scriptModel = getInputMap(delegateTask, runAsUser);
        getServiceRegistry().getScriptService().buildCoreModel(scriptModel);

        Object result = null;
        try {
            result = executeScript(script.getExpressionText(), scriptModel, getLanguage(), runAsUser);
        }
        finally {
            if (clearAuthenticationContext) {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
        }

        /** Set result value */
        if(this.resultVariable != null) {
            delegateTask.setVariable(this.resultVariable.getExpressionText(), result);
        }
    }

    /**
     * Get script input map model
     * @param delegateTask Delegate task
     * @param runAsUser Run as user
     * @return Script model map
     */
    protected Map<String, Object> getInputMap(DelegateTask delegateTask, String runAsUser) {
        HashMap<String, Object> scriptModel = new HashMap<String, Object>(1);

        NodeRef personNode = getPersonNode(runAsUser);
        if (personNode != null) {
            scriptModel.put(PERSON_BINDING_NAME, personNode);
        }

        scriptModel.put(TASK_BINDING_NAME, delegateTask);
        scriptModel.put(EXECUTION_BINDING_NAME, delegateTask);

        /** Variables */
        Map<String, Object> variables = delegateTask.getVariables();

        for (Map.Entry<String, Object> varEntry : variables.entrySet()) {
            scriptModel.put(varEntry.getKey(), varEntry.getValue());
        }
        return scriptModel;
    }

    /**
     * Checks a valid Fully Authenticated User is set.
     * If none is set then attempts to set the task assignee as the Fully Authenticated User.
     * @param delegateTask the delegate task
     * @return <code>true</code> if the Fully Authenticated User was changed, otherwise <code>false</code>.
     */
    private boolean checkFullyAuthenticatedUser(final DelegateTask delegateTask) {
        if (AuthenticationUtil.getFullyAuthenticatedUser() == null) {
            String userName = delegateTask.getAssignee();
            if (userName != null) {
                AuthenticationUtil.setFullyAuthenticatedUser(userName);
                return true;
            }
        }
        return false;
    }
}
