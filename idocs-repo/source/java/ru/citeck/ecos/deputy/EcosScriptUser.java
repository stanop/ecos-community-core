package ru.citeck.ecos.deputy;

import org.alfresco.repo.security.authority.script.ScriptUser;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.mozilla.javascript.Scriptable;

public class EcosScriptUser extends ScriptUser {

    protected Boolean isAssistant;

    /**
     * Constructs a scriptable object representing a user.
     *
     * @param userName        The username
     * @param personNodeRef   The NodeRef
     * @param serviceRegistry A ServiceRegistry instance
     * @param scope           Script scope
     * @since 4.0
     */
    public EcosScriptUser(String userName, NodeRef personNodeRef, ServiceRegistry serviceRegistry, Scriptable scope) {
        super(userName, personNodeRef, serviceRegistry, scope);
    }
}
