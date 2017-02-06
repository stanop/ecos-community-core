package ru.citeck.ecos.spring.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import ru.citeck.ecos.deputy.DeputyService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Pavel Simonov
 */
public class UserAuthoritiesInterceptor implements MethodInterceptor {

    private static final String METHOD_NAME = "getAuthoritiesForUser";

    private DeputyService deputyService;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();

        if (METHOD_NAME.equals(methodName)) {
            Object result = invocation.proceed();
            if (result instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<String> authorities = new HashSet<>((Set<String>) result);
                String userName = getUserName(invocation.getArguments());
                authorities.addAll(getAuthorities(userName));
                return authorities;
            } else {
                return result;
            }
        } else {
            return invocation.proceed();
        }
    }

    private String getUserName(Object[] args) {
        if (args != null && args.length > 0 && args[0] instanceof String) {
            return (String) args[0];
        }
        return null;
    }

    private List<String> getAuthorities(String userName) {
        if (userName == null) {
            return Collections.emptyList();
        }
        return deputyService.getUsersDeputedTo(userName);
    }

    public void setDeputyService(DeputyService deputyService) {
        this.deputyService = deputyService;
    }
}
