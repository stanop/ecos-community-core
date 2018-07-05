package ru.citeck.ecos.notification.aop;

import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class IgnoreSendFailureInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object[] arguments = methodInvocation.getArguments();
        Action ruleAction = (Action) arguments[0];

        Boolean ignoreSendFailure = (Boolean) ruleAction.getParameterValue(MailActionExecuter.PARAM_IGNORE_SEND_FAILURE);
        if (ignoreSendFailure == null) {
            ruleAction.setParameterValue(MailActionExecuter.PARAM_IGNORE_SEND_FAILURE, true);
        }

        return methodInvocation.proceed();
    }
}
