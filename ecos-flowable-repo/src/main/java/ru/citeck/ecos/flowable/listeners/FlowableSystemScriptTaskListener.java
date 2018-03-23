package ru.citeck.ecos.flowable.listeners;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * @author Roman Makarskiy
 */
public class FlowableSystemScriptTaskListener extends FlowableScriptTaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        super.setRunAs(new FlowableStringExpression(AuthenticationUtil.getAdminUserName()));
        super.notify(delegateTask);
    }
}
