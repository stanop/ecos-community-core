package ru.citeck.ecos.flowable.scripts;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.flowable.engine.delegate.DelegateExecution;

import java.util.HashMap;
import java.util.Map;

/**
 * Flowable delegate execution script base
 */
public class FlowableDelegateExecutionScriptBase extends FlowableScriptBase {

    /**
     * Run the script that is configured, using the given execution
     * @param execution Delegate execution
     * @param scriptString Script string
     */
    protected Object runScript(DelegateExecution execution, String scriptString) throws Exception {
        /** Check authenticated user */
        String runAsUser = getStringValue(runAs, execution);
        boolean clearAuthenticationContext = checkFullyAuthenticatedUser(execution);

        /** Load script model */
        Map<String, Object> scriptModel = getInputMap(execution, runAsUser);
        getServiceRegistry().getScriptService().buildCoreModel(scriptModel);

        /** Run script */
        try {
            return executeScript(scriptString, scriptModel, getLanguage(), runAsUser);
        }
        finally {
            if (clearAuthenticationContext) {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
        }
    }

    /**
     * Get script input map model
     * @param execution Execution
     * @param runAsUser Run as user
     * @return Script model map
     */
    protected Map<String, Object> getInputMap(DelegateExecution execution, String runAsUser) {
        HashMap<String, Object> scriptModel = new HashMap<String, Object>(1);

        /** Run as user */
        NodeRef personNode = getPersonNode(runAsUser);
        if (personNode != null) {
            scriptModel.put(PERSON_BINDING_NAME, personNode);
        }

        scriptModel.put(EXECUTION_BINDING_NAME, execution);

        /** Workflow variables */
        Map<String, Object> variables = execution.getVariables();

        for (Map.Entry<String, Object> varEntry : variables.entrySet()) {
            scriptModel.put(varEntry.getKey(), varEntry.getValue());
        }
        return scriptModel;
    }

    /**
     * Checks a valid Fully Authenticated User is set.
     * If none is set then attempts to set the workflow owner
     * @param execution the execution
     * @return <code>true</code> if the Fully Authenticated User was changed, otherwise <code>false</code>.
     */
    private boolean checkFullyAuthenticatedUser(final DelegateExecution execution) {
        if (AuthenticationUtil.getFullyAuthenticatedUser() == null) {
            NamespaceService namespaceService = getServiceRegistry().getNamespaceService();
            WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(namespaceService);
            String ownerVariableName = qNameConverter.mapQNameToName(ContentModel.PROP_OWNER);

            String userName = (String) execution.getVariable(ownerVariableName);
            if (userName != null) {
                AuthenticationUtil.setFullyAuthenticatedUser(userName);
                return true;
            }
        }
        return false;
    }
}
